/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperables.dataframe.{DataFrameMetadata, DataFrame}
import io.deepsense.deeplang._
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.entitystorage.EntityStorageClientTestInMemoryImpl

/**
 * This test requirements:
 * - deepsense dev environment
 * - /tests directory with read,write privileges to Your user on HDFS
 * - /etc/hosts entry: 172.28.128.100 ds-dev-env-master
 */
class SaveLoadDataFrameIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter {

  val schema: StructType = StructType(List(
    StructField("column1", StringType),
    StructField("column2", LongType),
    StructField("column3", DoubleType),
    StructField("column4", StringType))
  )

  before {
    // Reset entity storage client state
    prepareEntityStorageClient
  }

  "Save and Load DataFrame operations" should {
    "correctly save and load dataFrame from HDFS" in {
      val context = executionContext
      val dataFrame: DataFrame = createDataFrame
      val dataFrameId = saveDataFrame(context, dataFrame, "test name", "test description")

      val retrievedDataFrame = loadDataFrame(context, dataFrameId)
      assertDataFramesEqual(dataFrame, retrievedDataFrame, checkRowOrder = false)
    }

    "correctly return metadata about saved dataFrame" in {
      val context = executionContext
      val dataFrame: DataFrame = createDataFrame
      val dataFrameId = saveDataFrame(context, dataFrame, "test name", "test description")
      val doperableCatalog = mock[DOperableCatalog]
      val loadDF = LoadDataFrame(dataFrameId)
      val (knowledge, warnings) = loadDF.inferKnowledge(context)(Vector.empty)

      warnings.warnings should have size 0
      knowledge should have size 1
      knowledge.head.types should have size 1

      val metadata = knowledge.head.types.head.inferredMetadata.get.asInstanceOf[DataFrameMetadata]
      metadata.isExact shouldBe true
      metadata.isColumnCountExact shouldBe true
      metadata.columns should have size schema.length
      metadata.toSchema shouldBe schema
    }
  }

  private def createDataFrame: DataFrame = {
    val rowsSeq: Seq[Row] = Seq(
      Row("aaa", 1L, 1.2, null),
      Row("bbb", 2L, null, "text"),
      Row("ccc", null, 3.4, "when the music is over turn off the lights.")
    )
    createDataFrame(rowsSeq, schema)
  }

  private def saveDataFrame(
      context: ExecutionContext,
      dataFrame: DataFrame,
      name: String,
      description: String): String = {
    val saveDataFrameOperation = SaveDataFrame(name, description)
    saveDataFrameOperation.execute(context)(Vector[DOperable](dataFrame))

    val entities =
      context.entityStorageClient.asInstanceOf[EntityStorageClientTestInMemoryImpl].getAllEntities
    assert(entities.length == 1, "For this test one entity should be created.")
    entities.head.info.entityId.toString
  }

  private def loadDataFrame(context: ExecutionContext, entityId: String): DataFrame = {
    val loadDataFrameOperation = LoadDataFrame(entityId)
    val operationResult = loadDataFrameOperation.execute(context)(Vector.empty[DOperable])
    operationResult.head.asInstanceOf[DataFrame]
  }
}
