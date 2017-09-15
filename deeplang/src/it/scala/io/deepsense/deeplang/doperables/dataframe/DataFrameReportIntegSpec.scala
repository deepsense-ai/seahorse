/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang.doperables.dataframe

import java.sql.Timestamp

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{Metadata => SparkMetadata, _}
import org.joda.time.DateTime

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoriesMapping, MappingMetadataConverter}
import io.deepsense.deeplang.doperables.{Report, ReportLevel}
import io.deepsense.reportlib.model.{CategoricalDistribution, ContinuousDistribution, Statistics, Table}


class DataFrameReportIntegSpec extends DeeplangIntegTestSupport with DataFrameTestFactory {

  override def beforeAll(): Unit = {
    super.beforeAll()
    // Tests verify correctness of reports at HIGH level of report details
    executionContext.reportLevel = ReportLevel.HIGH
  }

  "DataFrame" should {
    "generate report with data sample table" when {
      val exampleString = "DeepSense.io"
      val columnNameBase = "stringColumn"
      "number of columns and rows exceeds max" in {
        val columnsNumber = DataFrameReportGenerator.maxColumnsNumberInReport + 10
        val rowsNumber = DataFrameReportGenerator.maxColumnsNumberInReport + 10
        testReportTables(Some(exampleString), columnNameBase, columnsNumber, rowsNumber)
      }
      "number of columns and rows is minimal" in {
         val columnsNumber = 1
         val rowsNumber = 1
         testReportTables(Some(exampleString), columnNameBase, columnsNumber, rowsNumber)
       }
      "DataFrame is empty" in {
         val columnsNumber = 0
         val rowsNumber = 0
         testReportTables(Some(exampleString), columnNameBase, columnsNumber, rowsNumber)
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
        val dataFrame =
          executionContext.dataFrameBuilder.buildDataFrame(schema, rdd, Seq(nameColumnName))

        val report = dataFrame.report(executionContext)

        val tables: Map[String, Table] = report.content.tables
        val dataSampleTable = tables.get(DataFrameReportGenerator.dataSampleTableName).get
        dataSampleTable.columnNames shouldBe Some(List(nameColumnName, birthDateColumnName))
        dataSampleTable.rowNames shouldBe None
        dataSampleTable.values shouldBe
          List(List(None, Some(DateTimeConverter.toString(now))), List(Some(exampleString), None))
       }
      "there is timestamp column" in {
        val now: DateTime = DateTimeConverter.now
        val timestampColumnName: String = "timestampColumn"
        val dataFrame = executionContext.dataFrameBuilder.buildDataFrame(
          StructType(List(StructField(timestampColumnName, TimestampType))),
          sparkContext.parallelize(List(Row(new Timestamp(now.getMillis)))))

        val report = dataFrame.report(executionContext)

        val tables: Map[String, Table] = report.content.tables
        val dataSampleTable = tables.get(DataFrameReportGenerator.dataSampleTableName).get
        dataSampleTable.columnNames shouldBe Some(List(timestampColumnName))
        dataSampleTable.rowNames shouldBe None
        dataSampleTable.values shouldBe List(List(Some(DateTimeConverter.toString(now))))
      }
    }
    "generate report with correct column types" in {
      val dataFrame = testDataFrame(executionContext.dataFrameBuilder, sparkContext)

      val report = dataFrame.report(executionContext)
      val tables: Map[String, Table] = report.content.tables
      val dataSampleTable = tables.get(DataFrameReportGenerator.dataSampleTableName).get

      dataSampleTable.columnTypes shouldBe List(
        ColumnType.string,
        ColumnType.boolean,
        ColumnType.numeric,
        ColumnType.timestamp,
        ColumnType.categorical)
    }
    "generate report with correct column Distribution" in {
      val dataFrame = testDataFrame(executionContext.dataFrameBuilder, sparkContext)

      val report = dataFrame.report(executionContext)

      testEmptyDistribution(report, DataFrameTestFactory.stringColumnName)
      testCategoricalDistribution(
        report,
        DataFrameTestFactory.booleanColumnName,
        1L,
        Seq("false", "true"),
        Seq(5, 4))
      testCategoricalDistribution(
        report,
        DataFrameTestFactory.categoricalColumnName,
        1L,
        Seq("autumn", "spring", "summer", "winter"),
        Seq(1, 2, 5, 1))
      testContinuousDistribution(
        report,
        DataFrameTestFactory.doubleColumnName,
        1L,
        doubleTypeBuckets,
        Seq(1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 1, 0, 1, 1, 0 , 1, 1),
        Statistics("1.87", "2.132", "1.307", "1.829556", "1.685", "2.03", Seq()))
      testContinuousDistribution(
        report,
        DataFrameTestFactory.timestampColumnName,
        1L,
        timestampTypeBuckets,
        Seq(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 3, 0, 0, 1),
        Statistics(
          "1996-10-24T00:43:00.000Z",
          "2010-01-07T00:00:00.000Z",
          "1954-12-18T00:43:00.000Z",
          "1989-10-15T21:53:26.666Z",
          "1978-09-08T00:43:00.000Z",
          "1999-11-29T00:43:00.000Z",
          Seq())
      )
    }
    "generate column Distribution for one value DataFrame" in {
      val dataFrame = oneValueDataFrame(executionContext.dataFrameBuilder, sparkContext)

      val report = dataFrame.report(executionContext)

      testEmptyDistribution(report, DataFrameTestFactory.stringColumnName)
      testCategoricalDistribution(
        report,
        DataFrameTestFactory.booleanColumnName,
        0L,
        Seq("false", "true"),
        Seq(10, 0))
      testCategoricalDistribution(
        report,
        DataFrameTestFactory.categoricalColumnName,
        0L,
        Seq("summer"),
        Seq(10))
      testContinuousDistribution(
        report,
        DataFrameTestFactory.doubleColumnName,
        0L,
        Seq("1.67"),
        Seq(10),
        Statistics("1.67", "1.67", "1.67", "1.67", "1.67", "1.67", Seq()))
      testContinuousDistribution(
        report,
        DataFrameTestFactory.timestampColumnName,
        0L,
        Seq("1970-01-20T00:43:00.000Z"),
        Seq(10),
        Statistics(
          "1970-01-20T00:43:00.000Z",
          "1970-01-20T00:43:00.000Z",
          "1970-01-20T00:43:00.000Z",
          "1970-01-20T00:43:00.000Z",
          "1970-01-20T00:43:00.000Z",
          "1970-01-20T00:43:00.000Z",
          Seq())
      )
    }
    "generate correct report" when {
      "DataFrame is empty" in {
        val categories = Seq("red", "blue", "green")
        val mapping = CategoriesMapping(categories)
        val metadata = MappingMetadataConverter.mappingToMetadata(mapping, SparkMetadata.empty)
        val schema = StructType(Seq(
          StructField("string", StringType),
          StructField("numeric", DoubleType),
          StructField("categorical", IntegerType, metadata = metadata),
          StructField("timestamp", TimestampType),
          StructField("boolean", BooleanType)))
        val emptyDataFrame = executionContext.dataFrameBuilder.buildDataFrame(
            schema,
            sparkContext.parallelize(Seq.empty[Row]))

        val report = emptyDataFrame.report(executionContext)

        val tables = report.content.tables
        val dataSampleTable = tables.get(DataFrameReportGenerator.dataSampleTableName).get
        dataSampleTable.columnNames shouldBe
          Some(List("string", "numeric", "categorical", "timestamp", "boolean"))
        dataSampleTable.rowNames shouldBe None
        dataSampleTable.values shouldBe List.empty
        testDataFrameSizeTable(tables, 5, 0)
        testEmptyDistribution(report, "string")
        testContinuousDistribution(report, "numeric", 0L, Seq.empty, Seq.empty, Statistics())
        testCategoricalDistribution(report, "categorical", 0L, categories, Seq(0, 0, 0))
        testContinuousDistribution(report, "timestamp", 0L, Seq.empty, Seq.empty, Statistics())
        testCategoricalDistribution(report, "boolean", 0L, Seq("false", "true"), Seq(0, 0))
      }
    }
  }

  val longTypeBuckets: Array[String] = Array("1", "4939940.8", "9879880.6", "14819820.4",
    "19759760.2", "24699700", "29639639.8", "34579579.6", "39519519.4", "44459459.2", "49399399",
    "54339338.8", "59279278.6", "64219218.4", "69159158.2", "74099098", "79039037.8", "83978977.6",
    "88918917.4", "93858857.2")

  val doubleTypeBuckets: Array[String] = Array("1.307", "1.34825", "1.3895", "1.43075", "1.472",
    "1.51325", "1.5545", "1.59575", "1.637", "1.67825", "1.7195", "1.76075", "1.802", "1.84325",
    "1.8845", "1.92575", "1.967", "2.00825", "2.0495", "2.09075")

  val timestampTypeBuckets: Array[String] = Array("1954-12-18T00:43:00.000Z",
    "1957-09-18T11:28:51.000Z", "1960-06-19T22:14:42.000Z", "1963-03-22T09:00:33.000Z",
    "1965-12-21T19:46:24.000Z", "1968-09-22T06:32:15.000Z", "1971-06-24T17:18:06.000Z",
    "1974-03-26T04:03:57.000Z", "1976-12-25T14:49:48.000Z", "1979-09-27T01:35:39.000Z",
    "1982-06-28T12:21:30.000Z", "1985-03-29T23:07:21.000Z", "1987-12-30T09:53:12.000Z",
    "1990-09-30T20:39:03.000Z", "1993-07-02T07:24:54.000Z", "1996-04-02T18:10:45.000Z",
    "1999-01-03T04:56:36.000Z", "2001-10-04T15:42:27.000Z", "2004-07-06T02:28:18.000Z",
    "2007-04-07T13:14:09.000Z")

  private def testEmptyDistribution(
      report: Report,
      columnName: String): Unit = {
    report.content.distributions.contains(columnName) shouldBe false
  }

  private def testCategoricalDistribution(
      report: Report,
      columnName: String,
      missingValues: Long,
      buckets: Seq[String],
      counts: Seq[Long]): Unit = {
    val categoricalColumnDistribution: CategoricalDistribution =
      report.content.distributions(columnName).asInstanceOf[CategoricalDistribution]
    categoricalColumnDistribution.missingValues shouldBe missingValues
    categoricalColumnDistribution.name shouldBe columnName
    categoricalColumnDistribution.buckets shouldBe buckets
    categoricalColumnDistribution.counts shouldBe counts
  }

  private def testContinuousDistribution(
      report: Report,
      columnName: String,
      missingValues: Long,
      expectedBuckets: Seq[String],
      expectedCounts: Seq[Long],
      expectedStatistics: Statistics): Unit = {
    val continuousColumnDistribution: ContinuousDistribution =
      report.content.distributions(columnName).asInstanceOf[ContinuousDistribution]
    continuousColumnDistribution.missingValues shouldBe missingValues
    continuousColumnDistribution.name shouldBe columnName
    continuousColumnDistribution.buckets shouldBe expectedBuckets
    continuousColumnDistribution.counts shouldBe expectedCounts
    continuousColumnDistribution.statistics shouldBe expectedStatistics
  }

  private def testReportTables(
      cellValue: Option[String],
      columnNameBase: String,
      dataFrameColumnsNumber: Int,
      dataFrameRowsNumber: Int): Registration = {
    val dataFrame = executionContext.dataFrameBuilder.buildDataFrame(
      buildSchema(dataFrameColumnsNumber, columnNameBase),
      buildRDDWithStringValues(dataFrameColumnsNumber, dataFrameRowsNumber, cellValue))

    val report = dataFrame.report(executionContext)

    val tables: Map[String, Table] = report.content.tables
    testDataSampleTable(
      cellValue,
      columnNameBase,
      dataFrameColumnsNumber,
      dataFrameRowsNumber,
      tables)
    testDataFrameSizeTable(tables, dataFrameColumnsNumber, dataFrameRowsNumber)
  }

  private def testDataSampleTable(
      cellValue: Option[String],
      columnNameBase: String,
      dataFrameColumnsNumber: Int,
      dataFrameRowsNumber: Int,
      tables: Map[String, Table]): Registration = {
    val dataSampleTable = tables.get(DataFrameReportGenerator.dataSampleTableName).get
    val expectedColumnsNumber: Int =
      Math.min(DataFrameReportGenerator.maxColumnsNumberInReport, dataFrameColumnsNumber)
    val expectedRowsNumber: Int =
      Math.min(DataFrameReportGenerator.maxRowsNumberInReport, dataFrameRowsNumber)
    dataSampleTable.columnNames shouldBe
      Some((0 until expectedColumnsNumber).map(columnNameBase + _))
    dataSampleTable.rowNames shouldBe None
    dataSampleTable.values shouldBe
      List.fill(expectedRowsNumber)(List.fill(expectedColumnsNumber)(cellValue))
  }

  private def testDataFrameSizeTable(
      tables: Map[String, Table],
      numberOfColumns: Int,
      numberOfRows: Long): Registration = {
    val dataFrameSizeTable = tables.get(DataFrameReportGenerator.dataFrameSizeTableName).get
    dataFrameSizeTable.columnNames shouldBe Some(List("Number of columns", "Number of rows"))
    dataFrameSizeTable.rowNames shouldBe None
    dataFrameSizeTable.values shouldBe
      List(List(Some(numberOfColumns.toString), Some(numberOfRows.toString)))
  }

  private def buildSchema(numberOfColumns: Int, columnNameBase: String): StructType = {
    StructType((0 until numberOfColumns).map(i => StructField(columnNameBase + i, StringType)))
  }

  private def buildRDDWithStringValues(
      numberOfColumns: Int,
      numberOfRows: Int,
      value: Option[String]): RDD[Row] =
    sparkContext.parallelize(
      List.fill(numberOfRows)(Row(List.fill(numberOfColumns)(value.orNull): _*)))
}
