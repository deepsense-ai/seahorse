/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 */

package io.deepsense.deeplang.doperations

import java.sql.Timestamp

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.{DOperationIntegTestSupport, DOperable}

class WriteDataFrameIntegSpec
  extends DOperationIntegTestSupport
  with BeforeAndAfter
  with DOperationsFactory {

  val timestamp: Timestamp = new Timestamp(new DateTime(2007, 12, 2, 3, 10, 11).getMillis)
  val testDir = "/tests/WriteDataFrameTest"

  before {
    hdfsClient.delete(testDir, true)
  }

  "WriteDataFrame" should "write created DataFrame" in {
    val rowsSeq: Seq[Row] = Seq(
      Row("aaa", 1L, 1.2, timestamp),
      Row("bbb", 2L, 2.2, timestamp),
      Row("ccc", 3L, 3.4, timestamp))


    testSimpleDataFrameSchemaWithRowsSeq(rowsSeq)
  }

  "WriteDataFrame" should "write created DataFrame with missing values" in {
    val rowsSeq: Seq[Row] = Seq(
      Row("aaa", 1L, 1.2, null),
      Row("bbb", 2L, null, timestamp),
      Row("ccc", null, 3.4, timestamp))

    testSimpleDataFrameSchemaWithRowsSeq(rowsSeq)
  }

  def testSimpleDataFrameSchemaWithRowsSeq(rowsSeq: Seq[Row]): Unit = {
    val context = executionContext
    val operation: WriteDataFrame = createWriteDataFrameOperation("testName", "test description")
    val dataFrameToSave: DataFrame = createDataFrameToSave(rowsSeq)

    operation.execute(context)(Vector[DOperable](dataFrameToSave))

    val loadedSparkDataFrame = sqlContext.parquetFile(testDir)
    val loadedDataFrame = context.dataFrameBuilder.buildDataFrame(loadedSparkDataFrame)
    assertDataFramesEqual(dataFrameToSave, loadedDataFrame)
  }

  private def createDataFrameToSave(rowsSeq: Seq[Row]): DataFrame = {
    val schema: StructType = createSchema
    val manualRDD: RDD[Row] = sparkContext.parallelize(rowsSeq)
    val sparkDataFrame = sqlContext.createDataFrame(manualRDD, schema)
    val builder = DataFrameBuilder(sqlContext)
    builder.buildDataFrame(sparkDataFrame)
  }

  private def createSchema: StructType = {
    StructType(List(
      StructField("column1", StringType),
      StructField("column2", LongType),
      StructField("column3", DoubleType),
      StructField("column4", TimestampType)))
  }
}
