package io.deepsense.deeplang.doperables.dataframe

import java.sql.Timestamp

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{TimestampType, StringType, StructField, StructType}
import org.joda.time.DateTime

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.Report
import io.deepsense.reportlib.model.Table

class DataFrameReportIntegSpec extends DeeplangIntegTestSupport {

  "DataFrame" should {
    "generate report with data sample table" when {
      val exampleString = "DeepSense.io"
      val columnNameBase = "stringColumn"
      "number of columns and rows exceeds max" in {
        val columnsNumber = DataFrame.maxColumnsNumberInReport + 10
        val rowsNumber = DataFrame.maxColumnsNumberInReport + 10
        testReportDataSampleTable(exampleString, columnNameBase, columnsNumber, rowsNumber)
      }
      "number of columns and rows is minimal" in {
         val columnsNumber = 1
         val rowsNumber = 1
         testReportDataSampleTable(exampleString, columnNameBase, columnsNumber, rowsNumber)
       }
      "DataFrame is empty" in {
         val columnsNumber = 0
         val rowsNumber = 0
         testReportDataSampleTable(exampleString, columnNameBase, columnsNumber, rowsNumber)
       }
      "DataFrame has missing values" in {
        val now = DateTimeConverter.now
        val nameColumnName = "name"
        val birthDateColumnName = "birthdate"
        val rdd: RDD[Row] = sparkContext.parallelize(
          List(Row(null, new Timestamp(now.getMillis)), Row(exampleString, null)))
        val schema: StructType = StructType(List(
          StructField(nameColumnName, StringType),
          StructField(birthDateColumnName, TimestampType)))
        val dataFrame = executionContext.dataFrameBuilder.buildDataFrame(schema, rdd)

        val report = dataFrame.report

        val tables: Map[String, Table] = report.content.tables
        val dataSampleTable = tables.get(DataFrame.dataSampleTableName).get
        dataSampleTable.columnNames shouldBe Some(List(nameColumnName, birthDateColumnName))
        dataSampleTable.rowNames shouldBe None
        dataSampleTable.values shouldBe
          List(List(null, DateTimeConverter.convertToString(now)), List(exampleString, null))
       }
      "there is timestamp column" in {
        val now: DateTime = DateTimeConverter.now
        val timestampColumnName: String = "timestampColumn"
        val dataFrame = executionContext.dataFrameBuilder.buildDataFrame(
          StructType(List(StructField(timestampColumnName, TimestampType))),
          sparkContext.parallelize(List(Row(new Timestamp(now.getMillis)))))

        val report = dataFrame.report

        val tables: Map[String, Table] = report.content.tables
        val dataSampleTable = tables.get(DataFrame.dataSampleTableName).get
        dataSampleTable.columnNames shouldBe Some(List(timestampColumnName))
        dataSampleTable.rowNames shouldBe None
        dataSampleTable.values shouldBe List(List(DateTimeConverter.convertToString(now)))
      }
    }
  }

  private def testReportDataSampleTable(
    cellValue: String,
    columnNameBase: String,
    dataFrameColumnsNumber: Int,
    dataFrameRowsNumber: Int): Registration = {
    val dataFrame = executionContext.dataFrameBuilder.buildDataFrame(
      buildSchema(dataFrameColumnsNumber, columnNameBase),
      buildRDDOfStrings(dataFrameColumnsNumber, dataFrameRowsNumber, cellValue))

    val report = dataFrame.report

    val tables: Map[String, Table] = report.content.tables
    val dataSampleTable = tables.get(DataFrame.dataSampleTableName).get
    val expectedColumnsNumber: Int =
      Math.min(DataFrame.maxColumnsNumberInReport, dataFrameColumnsNumber)
    val expectedRowsNumber: Int = Math.min(DataFrame.maxRowsNumberInReport, dataFrameRowsNumber)
    dataSampleTable.columnNames shouldBe
    Some((0 until expectedColumnsNumber).map(columnNameBase + _))
    dataSampleTable.rowNames shouldBe None
    dataSampleTable.values shouldBe
      List.fill(expectedRowsNumber)(List.fill(expectedColumnsNumber)(cellValue))
  }

  private def buildSchema(numberOfColumns: Int, columnNameBase: String): StructType = {
    StructType((0 until numberOfColumns).map(i => StructField(columnNameBase + i, StringType)))
  }

  private def buildRDDOfStrings(
    numberOfColumns: Int,
    numberOfRows: Int,
    value: String): RDD[Row] = {
    sparkContext.parallelize(List.fill(numberOfRows)(Row(List.fill(numberOfColumns)(value):_*)))
  }
}
