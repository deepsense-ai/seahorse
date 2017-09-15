/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.doperations

import scala.reflect.io.Path

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.dataframe.DataFrameBuilder
import io.deepsense.deeplang.{DOperable, SparkIntegTestSupport}

class WriteDataFrameIntegSpec extends SparkIntegTestSupport with BeforeAndAfter {
  val localDataFrameWritePath = "target/test/resources/localDataFrameWrite"

  "WriteDataFrame" should "write created DataFrame" in {
    val rowsSeq: Seq[Row] = Seq(
      Row("aaa", 1L, 1.2, "2007-12-02 03:10:11.0000001"),
      Row("bbb", 2L, 2.2, "2007-12-02 03:10:11.0000001"),
      Row("ccc", 3L, 3.4, "2007-12-02 03:10:11.0000001"))

    testSimpleDataFrameSchemaWithRowsSeq(rowsSeq)
  }

  "WriteDataFrame" should "write created DataFrame with missing values" in {
    val rowsSeq: Seq[Row] = Seq(
      Row("aaa", 1L, 1.2, null),
      Row("bbb", 2L, null, "2007-12-02 03:10:11.0000001"),
      Row("ccc", null, 3.4, "2007-12-02 03:10:11.0000001"))

    testSimpleDataFrameSchemaWithRowsSeq(rowsSeq)
  }

  def testSimpleDataFrameSchemaWithRowsSeq(rowsSeq: Seq[Row]): Unit = {
    val context = executionContext
    val operation = new WriteDataFrame
    val pathParameter = operation.parameters.getStringParameter("path")
    pathParameter.value = Some(localDataFrameWritePath)

    val schema: StructType = StructType(List(
      StructField("column1", StringType, true),
      StructField("column2", LongType, true),
      StructField("column3", DoubleType, true),
      StructField("column4", StringType, true)))

    val manualRDD: RDD[Row] = sparkContext.parallelize(rowsSeq)

    val sparkDataFrame = sqlContext.createDataFrame(manualRDD, schema)
    val builder = DataFrameBuilder(sqlContext)
    val dataFrameToSave = builder.buildDataFrame(sparkDataFrame)
    operation.execute(context)(Vector[DOperable](dataFrameToSave))

    val loadedDataFrame = sqlContext.jsonFile(localDataFrameWritePath)

    val loadedDataFrameRows = loadedDataFrame.orderBy("column1").collect()
    val savedDataFrameRows = dataFrameToSave.sparkDataFrame.collect()

    assert(loadedDataFrameRows.length == savedDataFrameRows.length)

    val zipped = loadedDataFrameRows zip savedDataFrameRows
    assert(zipped.forall(rowPair => rowPair._1 == rowPair._2))
    assert(zipped.forall(rowPair => rowPair._1.schema == rowPair._2.schema))
  }

  /** Delete local write path after every test */
  after {
    val path: Path = Path (localDataFrameWritePath)
    path.deleteRecursively()
  }
}
