/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.doperations

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.{DOperable, ExecutionContext, SparkIntegTestSupport}

/**
 * This test requirements:
 * - deepsense dev environment
 * - /tests directory with read,write privileges to Your user on HDFS
 * - /etc/hosts entry: 172.28.128.100 ds-dev-env-master
 */
class ReadWriteDataFrameIntegSpec extends SparkIntegTestSupport with BeforeAndAfter {

  val testDir = "/tests/readWriteDataFrameTest"

  before {
    hdfsClient.delete(testDir, true)
  }

  "Write and Read DataFrame operations" should "correctly write and read dataFrame from HDFS" in {
    val context = executionContext
    val dataFrame: DataFrame = createDataFrame

    writeDataFrame(context, testDir, dataFrame)
    val retrievedDataFrame = readDataFrame(context, testDir)

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
      path: String,
      dataFrame: DataFrame): Unit = {
    val writeDataFrameOperation = new WriteDataFrame
    val pathParameter = writeDataFrameOperation.parameters.getStringParameter("path")
    pathParameter.value = Some(path)

    writeDataFrameOperation.execute(context)(Vector[DOperable](dataFrame))
  }

  private def readDataFrame(context: ExecutionContext, path: String): DataFrame = {
    val readDataFrameOperation = new ReadDataFrame
    val pathParameter = readDataFrameOperation.parameters.getStringParameter("path")
    pathParameter.value = Some(path)

    val operationResult = readDataFrameOperation.execute(context)(Vector.empty[DOperable])
    operationResult.head.asInstanceOf[DataFrame]
  }
}
