/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.doperations

import java.sql.Timestamp

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.{DOperable, SparkIntegTestSupport}

class ReadDataFrameIntegSpec extends SparkIntegTestSupport with BeforeAndAfter with LazyLogging {

  val timestamp: Timestamp = new Timestamp(new DateTime(2007, 12, 2, 3, 10, 11).getMillis)
  val testDir = "/tests/ReadDataFrameTest"

  before {
    hdfsClient.delete(testDir, true)
  }

  "ReadDataFrame" should "read locally saved DataFrame" in {
    val context = executionContext
    val dataFrame: DataFrame = testDataFrame(context.dataFrameBuilder)
    dataFrame.sparkDataFrame.saveAsParquetFile(testDir)

    val operation = new ReadDataFrame
    val pathParameter = operation.parameters.getStringParameter("path")
    pathParameter.value = Some(testDir)
    logger.info("Reading dataframe from hdfs: {}", pathParameter)
    val operationResult = operation.execute(context)(Vector.empty[DOperable])
    val operationDataFrame = operationResult(0).asInstanceOf[DataFrame]

    assertDataFramesEqual(dataFrame, operationDataFrame)
  }

  def testDataFrame(builder: DataFrameBuilder): DataFrame = {
    val schema: StructType = StructType(List(
      StructField("column1", StringType, true),
      StructField("column2", LongType, true),
      StructField("column3", DoubleType, true),
      StructField("column4", TimestampType, true)))
    val manualRowsSeq: Seq[Row] = Seq(
      Row("aaa", 1L, 1.2, timestamp),
      Row("bbb", 2L, 2.2, timestamp),
      Row("ccc", 3L, 3.4, timestamp))
    val manualRDD: RDD[Row] = sparkContext.parallelize(manualRowsSeq)
    builder.buildDataFrame(schema, manualRDD)
  }
}
