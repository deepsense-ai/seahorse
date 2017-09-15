/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 */

package io.deepsense.deeplang.doperations

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.{DOperable, DOperationIntegTestSupport, ExecutionContext}
import io.deepsense.entitystorage.EntityStorageClientTestInMemoryImpl

/**
 * This test requirements:
 * - deepsense dev environment
 * - /tests directory with read,write privileges to Your user on HDFS
 * - /etc/hosts entry: 172.28.128.100 ds-dev-env-master
 */
class WriteReadDataFrameIntegSpec
  extends DOperationIntegTestSupport
  with BeforeAndAfter
  with DOperationsFactory {

  val testDir = "/tests/readWriteDataFrameTest"

  before {
    hdfsClient.delete(testDir, true)
  }

  "Write and Read DataFrame operations" should "correctly write and read dataFrame from HDFS" in {
    val context = executionContext
    val dataFrame: DataFrame = createDataFrame

    val dataFrameId = writeDataFrame(context, dataFrame, "test name", "test description")

    val retrievedDataFrame = readDataFrame(context, dataFrameId)
    assertDataFramesEqual(dataFrame, retrievedDataFrame)
  }

  private def createDataFrame: DataFrame = {
    val rowsSeq: Seq[Row] = Seq(
      Row("aaa", 1L, 1.2, null),
      Row("bbb", 2L, null, "text"),
      Row("ccc", null, 3.4, "when the music is over turn off the lights.")
    )
    val schema: StructType = StructType(List(
      StructField("column1", StringType, nullable = true),
      StructField("column2", LongType, nullable = true),
      StructField("column3", DoubleType, nullable = true),
      StructField("column4", StringType, nullable = true))
    )
    val manualRDD: RDD[Row] = sparkContext.parallelize(rowsSeq)
    val sparkDataFrame = sqlContext.createDataFrame(manualRDD, schema)
    val builder = DataFrameBuilder(sqlContext)
    builder.buildDataFrame(sparkDataFrame)
  }

  private def writeDataFrame(
      context: ExecutionContext,
      dataFrame: DataFrame,
      name: String,
      description: String): String = {
    val writeDataFrameOperation = createWriteDataFrameOperation(name, description)
    writeDataFrameOperation.execute(context)(Vector[DOperable](dataFrame))
    val entities =
      context.entityStorageClient.asInstanceOf[EntityStorageClientTestInMemoryImpl].getAllEntities
    assert(entities.length == 1, "For this test one entity should be created.")
    entities.head.id.toString
  }

  private def readDataFrame(context: ExecutionContext, entityId: String): DataFrame = {
    val readDataFrameOperation = createReadDataFrameOperation(entityId)
    val operationResult = readDataFrameOperation.execute(context)(Vector.empty[DOperable])
    operationResult.head.asInstanceOf[DataFrame]
  }
}
