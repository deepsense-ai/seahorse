/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.doperations

import java.sql.Timestamp

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.doperables.dataframe.DataFrameBuilder
import io.deepsense.deeplang.{DOperable, SparkIntegTestSupport}

class WriteDataFrameIntegSpec extends SparkIntegTestSupport with BeforeAndAfter {

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
    val operation = new WriteDataFrame
    val pathParameter = operation.parameters.getStringParameter("path")
    pathParameter.value = Some(testDir)

    val schema: StructType = StructType(List(
      StructField("column1", StringType, true),
      StructField("column2", LongType, true),
      StructField("column3", DoubleType, true),
      StructField("column4", TimestampType, true)))

    val manualRDD: RDD[Row] = sparkContext.parallelize(rowsSeq)

    val sparkDataFrame = sqlContext.createDataFrame(manualRDD, schema)
    val builder = DataFrameBuilder(sqlContext)
    val dataFrameToSave = builder.buildDataFrame(sparkDataFrame)
    operation.execute(context)(Vector[DOperable](dataFrameToSave))

    val loadedDataFrame = sqlContext.parquetFile(testDir)

    val loadedDataFrameRows = loadedDataFrame.orderBy("column1").collect()
    val savedDataFrameRows = dataFrameToSave.sparkDataFrame.collect()

    assert(loadedDataFrameRows.length == savedDataFrameRows.length)

    val zipped = loadedDataFrameRows zip savedDataFrameRows
    assert(zipped.forall(rowPair => rowPair._1 == rowPair._2))
    assert(zipped.forall(rowPair => rowPair._1.schema == rowPair._2.schema))
  }
}
