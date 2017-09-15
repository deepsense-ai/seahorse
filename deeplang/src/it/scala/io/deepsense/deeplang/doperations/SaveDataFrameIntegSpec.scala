/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import java.sql.Timestamp

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.{DeeplangIntegTestSupport, DOperable}
import io.deepsense.entitystorage.EntityStorageClientTestInMemoryImpl

class SaveDataFrameIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter
  with DOperationsFactory {

  val timestamp: Timestamp = new Timestamp(new DateTime(2007, 12, 2, 3, 10, 11).getMillis)

  "SaveDataFrame" should {
    "save created DataFrame" in {
      val rows: Seq[Row] = Seq(
        Row("aaa", 1L, 1.2, timestamp),
        Row("bbb", 2L, 2.2, timestamp),
        Row("ccc", 3L, 3.4, timestamp))
      testSimpleDataFrameSchemaWithRowsSeq(rows)
    }
  }

  "SaveDataFrame" should {
    "save created DataFrame with missing values" in {
      val rowsSeq: Seq[Row] = Seq(
        Row("aaa", 1L, 1.2, null),
        Row("bbb", 2L, null, timestamp),
        Row("ccc", null, 3.4, timestamp))
      testSimpleDataFrameSchemaWithRowsSeq(rowsSeq)
    }
  }

  def testSimpleDataFrameSchemaWithRowsSeq(rowsSeq: Seq[Row]): Unit = {
    val context = executionContext
    // NOTE: In this test suite, description should uniquely identify DataFrame
    val dataFrameDescription = rowsSeq.toString()
    val operation: SaveDataFrame = createSaveDataFrameOperation("testName", dataFrameDescription)
    val dataFrameToSave: DataFrame = createDataFrameToSave(rowsSeq)

    operation.execute(context)(Vector[DOperable](dataFrameToSave))

    // NOTE: Using ES-client-mock internal methods to get location of recently written DataFrame
    val filteredEntities =
      context
        .entityStorageClient
        .asInstanceOf[EntityStorageClientTestInMemoryImpl]
        .getAllEntities
        .filter(e => e.info.description == dataFrameDescription)
    filteredEntities.length shouldBe 1

    val loadedSparkDataFrame = sqlContext.read.parquet(filteredEntities.head.dataReference.url)
    val loadedDataFrame = context.dataFrameBuilder.buildDataFrame(loadedSparkDataFrame)
    // We cannot guarantee order of rows in loaded DataFrame
    assertDataFramesEqual(dataFrameToSave, loadedDataFrame, checkRowOrder = false)
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
