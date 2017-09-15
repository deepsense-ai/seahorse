/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang.doperations

import java.sql.Timestamp

import scala.concurrent.Await

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.{DOperable, DeeplangIntegTestSupport, ExecutionContext}
import io.deepsense.models.entities.{CreateEntityRequest, DataObjectReference, DataObjectReport, Entity}

class LoadDataFrameIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter
  with Logging {

  val timestamp: Timestamp = new Timestamp(new DateTime(2007, 12, 2, 3, 10, 11).getMillis)

  val testDataDir = testsDir + "/LoadDataFrameTest"

  before {
    fileSystemClient.delete(testsDir)
  }

  "LoadDataFrame" should {
    "load saved DataFrame" in {
      val context = executionContext
      val dataFrame: DataFrame = testDataFrame(context.dataFrameBuilder)
      dataFrame.sparkDataFrame.write.parquet(testsDir)
      val entityId = registerDataFrame(context)

      val operation = LoadDataFrame(entityId.toString())

      logger.debug("Loading dataframe from entity id: {}", entityId)
      val operationResult = operation.execute(context)(Vector.empty[DOperable])
      val operationDataFrame = operationResult.head.asInstanceOf[DataFrame]
      // We cannot guarantee order of rows in loaded DataFrame
      assertDataFramesEqual(dataFrame, operationDataFrame, checkRowOrder = false)
    }
  }

  def registerDataFrame(context: ExecutionContext, serializedMetadata: String = "{}"): Entity.Id = {
    import scala.concurrent.duration._
    implicit val timeout = 5.seconds
    val future = context.entityStorageClient.createEntity(CreateEntityRequest(
      context.tenantId,
      "testEntity name",
      "testEntity description",
      "DataFrame",
      Some(DataObjectReference(testsDir, serializedMetadata)),
      DataObjectReport("testEntity Report"),
      saved = true))
    Await.result(future, timeout)
  }

  def testDataFrame(builder: DataFrameBuilder): DataFrame = {
    val schema: StructType = StructType(List(
      StructField("column1", StringType),
      StructField("column2", LongType),
      StructField("column3", DoubleType),
      StructField("column4", TimestampType)))
    val manualRowsSeq: Seq[Row] = Seq(
      Row("aaa", 1L, 1.2, timestamp),
      Row("bbb", 2L, 2.2, timestamp),
      Row("ccc", 3L, 3.4, timestamp))
    createDataFrame(manualRowsSeq, schema)
  }
}
