/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import java.sql.Timestamp

import scala.concurrent.Await

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.{DOperable, DeeplangIntegTestSupport, ExecutionContext}
import io.deepsense.models.entities.{DataObjectReference, DataObjectReport, Entity, CreateEntityRequest}

class LoadDataFrameIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter
  with LazyLogging {

  val timestamp: Timestamp = new Timestamp(new DateTime(2007, 12, 2, 3, 10, 11).getMillis)

  val testDir = "/tests/LoadDataFrameTest"

  before {
    rawHdfsClient.delete(testDir, true)
  }

  "LoadDataFrame" should {
    "load saved DataFrame" in {
      val context = executionContext
      val dataFrame: DataFrame = testDataFrame(context.dataFrameBuilder)
      dataFrame.sparkDataFrame.write.parquet(testDir)
      val entityId = registerDataFrame(context)

      val operation = LoadDataFrame(entityId.toString)

      logger.info("Loading dataframe from entity id: {}", entityId)
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
      Some(DataObjectReference(testDir, serializedMetadata)),
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
