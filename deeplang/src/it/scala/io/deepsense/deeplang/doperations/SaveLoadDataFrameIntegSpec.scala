/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DOperable, DeeplangIntegTestSupport, ExecutionContext}
import io.deepsense.entitystorage.EntityStorageClientTestInMemoryImpl

/**
 * This test requirements:
 * - deepsense dev environment
 * - /tests directory with read,write privileges to Your user on HDFS
 * - /etc/hosts entry: 172.28.128.100 ds-dev-env-master
 */
class SaveLoadDataFrameIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter
  with DOperationsFactory {

  "Save and Load DataFrame operations" should {
    "correctly save and load dataFrame from HDFS" in {
      val context = executionContext
      val dataFrame: DataFrame = createDataFrame

      val dataFrameId = saveDataFrame(context, dataFrame, "test name", "test description")

      val retrievedDataFrame = loadDataFrame(context, dataFrameId)
      assertDataFramesEqual(dataFrame, retrievedDataFrame)
    }
  }

  private def createDataFrame: DataFrame = {
    val rowsSeq: Seq[Row] = Seq(
      Row("aaa", 1L, 1.2, null),
      Row("bbb", 2L, null, "text"),
      Row("ccc", null, 3.4, "when the music is over turn off the lights.")
    )
    val schema: StructType = StructType(List(
      StructField("column1", StringType),
      StructField("column2", LongType),
      StructField("column3", DoubleType),
      StructField("column4", StringType))
    )
    createDataFrame(rowsSeq, schema)
  }

  private def saveDataFrame(
      context: ExecutionContext,
      dataFrame: DataFrame,
      name: String,
      description: String): String = {
    val saveDataFrameOperation = createSaveDataFrameOperation(name, description)
    saveDataFrameOperation.execute(context)(Vector[DOperable](dataFrame))
    val entities =
      context.entityStorageClient.asInstanceOf[EntityStorageClientTestInMemoryImpl].getAllEntities
    assert(entities.length == 1, "For this test one entity should be created.")
    entities.head.id.toString
  }

  private def loadDataFrame(context: ExecutionContext, entityId: String): DataFrame = {
    val loadDataFrameOperation = createLoadDataFrameOperation(entityId)
    val operationResult = loadDataFrameOperation.execute(context)(Vector.empty[DOperable])
    operationResult.head.asInstanceOf[DataFrame]
  }
}
