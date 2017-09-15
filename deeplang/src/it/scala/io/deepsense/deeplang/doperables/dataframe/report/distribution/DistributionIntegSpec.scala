/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang.doperables.dataframe.report.distribution

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.report.DataFrameReportGenerator
import io.deepsense.deeplang.doperables.dataframe.report.distribution.discrete.DiscreteDistributionBuilderFactory
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameTestFactory}
import io.deepsense.deeplang.doperables.report.Report
import io.deepsense.reportlib.model._

class DistributionIntegSpec extends DeeplangIntegTestSupport with DataFrameTestFactory {

  import DataFrameTestFactory._

  "DataFrame" should {
    "generate report with correct column Distribution" in {
      val data = testDataFrame(executionContext.dataFrameBuilder, sparkContext)
      val distributions = getDistributions(data)

      testDiscreteDistribution(
        distributions,
        stringColumnName,
        1L,
        Seq("NULL", "Name1", "Name10", "Name2", "Name3", "Name4",
          "Name5", "Name7", "Name8", "Name9"),
        Seq.fill(10)(1))
      testDiscreteDistribution(
        distributions,
        booleanColumnName,
        1L,
        Seq("false", "true"),
        Seq(5, 4))
      testContinuousDistribution(
        distributions,
        integerColumnName,
        1L,
        integerTypeBuckets,
        Seq(3, 4, 1, 1),
        Statistics("3", "0", "NaN"))
      testContinuousDistribution(
        distributions,
        doubleColumnName,
        1L,
        doubleTypeBuckets,
        Seq(1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 1),
        Statistics("2.132", "1.307", "NaN"))
      testContinuousDistribution(
        distributions,
        timestampColumnName,
        1L,
        timestampTypeBuckets,
        Seq(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 3, 0, 0, 1),
        Statistics(
          "2010-01-07T00:00:00.000Z",
          "1954-12-18T00:43:00.000Z",
          "NaN")
      )
    }
    "generate distributions for all column types" in {
      val data = allTypesDataFrame(executionContext.dataFrameBuilder, sparkContext)
      val distributions = getDistributions(data)

      Seq(byteColumnName, shortColumnName, integerColumnName, longColumnName).foreach(colName => {
        testContinuousDistribution(
          distributions,
          colName,
          0L,
          Seq("0", "0", "1"),
          Seq(1, 1),
          Statistics("1", "0", "0.5"))
      })

      Seq(floatColumnName, doubleColumnName, decimalColumnName).foreach(colName => {
        testContinuousDistribution(
          distributions,
          colName,
          0L,
          (0 to 20).map(x => x * 0.05).toSeq.map(DoubleUtils.double2String(_)),
          Seq(1L) ++ Seq.fill(18)(0L) ++ Seq(1L),
          Statistics("1", "0", "0.5"))
      })

      testDiscreteDistribution(
        distributions,
        stringColumnName,
        0L,
        Seq("x", "y"),
        Seq(1, 1))

      testDiscreteDistribution(
        distributions, booleanColumnName, 0L, Seq("false", "true"), Seq(1, 1)
      )

      testContinuousDistribution(
        distributions,
        timestampColumnName,
        0L,
        timestampBucketsForSparkTypesTest,
        Seq(1L) ++ Seq.fill(18)(0L) ++ Seq(1L),
        Statistics(
          "1970-01-22T00:00:00.000Z",
          "1970-01-20T00:00:00.000Z",
          "1970-01-21T00:00:00.000Z"))

      testContinuousDistribution(
        distributions,
        dateColumnName,
        0L,
        timestampBucketsForSparkTypesTest,
        Seq(1L) ++ Seq.fill(18)(0L) ++ Seq(1L),
        Statistics(
          "1970-01-22T00:00:00.000Z",
          "1970-01-20T00:00:00.000Z",
          "1970-01-21T00:00:00.000Z"))

      Seq(binaryColumnName, arrayColumnName, mapColumnName, structColumnName, vectorColumnName)
        .foreach(colName =>
          distributions(colName) shouldBe a[NoDistribution]
        )
    }
    "generate column Distribution for one value DataFrame" in {
      val data = oneValueDataFrame(executionContext.dataFrameBuilder, sparkContext)
      val distributions = getDistributions(data)

      testDiscreteDistribution(
        distributions,
        stringColumnName,
        0L,
        Seq("Name1"),
        Seq(10))
      testDiscreteDistribution(
        distributions,
        booleanColumnName,
        0L,
        Seq("false", "true"),
        Seq(10, 0))
      testContinuousDistribution(
        distributions,
        integerColumnName,
        0L,
        Seq("1", "1"),
        Seq(10),
        Statistics("1", "1", "1"))
      testContinuousDistribution(
        distributions,
        doubleColumnName,
        0L,
        Seq("1.67", "1.67"),
        Seq(10),
        Statistics("1.67", "1.67", "1.67"))
      testContinuousDistribution(
        distributions,
        timestampColumnName,
        0L,
        Seq("1970-01-20T00:43:00.000Z", "1970-01-20T00:43:00.000Z"),
        Seq(10),
        Statistics(
          "1970-01-20T00:43:00.000Z",
          "1970-01-20T00:43:00.000Z",
          "1970-01-20T00:43:00.000Z")
      )
    }
    "not generate string column distribution" when {
      "there are too many distinct values in the DataFrame" in {
        val schema = StructType(Seq(StructField("string", StringType)))
        val rowCount =
          DiscreteDistributionBuilderFactory.MaxDistinctValuesToCalculateDistribution + 1
        val dataFrame = executionContext.dataFrameBuilder.buildDataFrame(
          schema,
          sparkContext.parallelize(
            (1 to rowCount).toSeq.map(x => Row(x.toString))
          )
        )
        val report = dataFrame.report
        val distribution = report.content.distributions("string")
        inside(distribution) {
          case d: NoDistribution =>
            d.description shouldEqual NoDistributionReasons.TooManyDistinctCategoricalValues
        }
      }
    }
  }

  def checkAllDistributionsEmptyBecauseOfNoData(schema: StructType, report: Report): Unit = {
    for (fieldName <- schema.fieldNames) {
      inside(report.content.distributions(fieldName)) {
        case d: NoDistribution => d.description shouldEqual NoDistributionReasons.NoData
      }
    }
  }

  private def getDistributions(data: DataFrame) = {
    val report = data.report
    report.content.distributions
  }

  val doubleTypeBuckets: Array[String] = Array("1.307", "1.34825", "1.3895", "1.43075", "1.472",
    "1.51325", "1.5545", "1.59575", "1.637", "1.67825", "1.7195", "1.76075", "1.802", "1.84325",
    "1.8845", "1.92575", "1.967", "2.00825", "2.0495", "2.09075", "2.132")

  val integerTypeBuckets: Array[String] = Array("0", "0", "1", "2", "3")

  val timestampTypeBuckets: Array[String] = Array("1954-12-18T00:43:00.000Z",
    "1957-09-18T11:28:51.000Z", "1960-06-19T22:14:42.000Z", "1963-03-22T09:00:33.000Z",
    "1965-12-21T19:46:24.000Z", "1968-09-22T06:32:15.000Z", "1971-06-24T17:18:06.000Z",
    "1974-03-26T04:03:57.000Z", "1976-12-25T14:49:48.000Z", "1979-09-27T01:35:39.000Z",
    "1982-06-28T12:21:30.000Z", "1985-03-29T23:07:21.000Z", "1987-12-30T09:53:12.000Z",
    "1990-09-30T20:39:03.000Z", "1993-07-02T07:24:54.000Z", "1996-04-02T18:10:45.000Z",
    "1999-01-03T04:56:36.000Z", "2001-10-04T15:42:27.000Z", "2004-07-06T02:28:18.000Z",
    "2007-04-07T13:14:09.000Z", "2010-01-07T00:00:00.000Z")

  val timestampBucketsForSparkTypesTest: Array[String] = Array("1970-01-20T00:00:00.000Z",
    "1970-01-20T02:24:00.000Z", "1970-01-20T04:48:00.000Z", "1970-01-20T07:12:00.000Z",
    "1970-01-20T09:36:00.000Z", "1970-01-20T12:00:00.000Z", "1970-01-20T14:24:00.000Z",
    "1970-01-20T16:48:00.000Z", "1970-01-20T19:12:00.000Z", "1970-01-20T21:36:00.000Z",
    "1970-01-21T00:00:00.000Z", "1970-01-21T02:24:00.000Z", "1970-01-21T04:48:00.000Z",
    "1970-01-21T07:12:00.000Z", "1970-01-21T09:36:00.000Z", "1970-01-21T12:00:00.000Z",
    "1970-01-21T14:24:00.000Z", "1970-01-21T16:48:00.000Z", "1970-01-21T19:12:00.000Z",
    "1970-01-21T21:36:00.000Z", "1970-01-22T00:00:00.000Z")

  private def testDiscreteDistribution(
      distributions: Map[String, Distribution],
      columnName: String,
      missingValues: Long,
      categories: Seq[String],
      counts: Seq[Long]): Unit = {
    distributions(columnName) shouldBe a[DiscreteDistribution]
    val discreteColumnDistribution: DiscreteDistribution =
      distributions(columnName).asInstanceOf[DiscreteDistribution]
    discreteColumnDistribution.missingValues shouldBe missingValues
    discreteColumnDistribution.name shouldBe columnName
    discreteColumnDistribution.categories shouldBe categories
    discreteColumnDistribution.counts shouldBe counts
  }

  private def testContinuousDistribution(
      distributions: Map[String, Distribution],
      columnName: String,
      missingValues: Long,
      expectedBuckets: Seq[String],
      expectedCounts: Seq[Long],
      expectedStatistics: Statistics): Unit = {
    val continuousColumnDistribution: ContinuousDistribution =
      distributions(columnName).asInstanceOf[ContinuousDistribution]
    continuousColumnDistribution.missingValues shouldBe missingValues
    continuousColumnDistribution.name shouldBe columnName
    continuousColumnDistribution.buckets shouldBe expectedBuckets
    continuousColumnDistribution.counts shouldBe expectedCounts
    continuousColumnDistribution.statistics shouldBe expectedStatistics
  }

  private def testDataFrameSizeTable(
      tables: Map[String, Table],
      numberOfColumns: Int,
      numberOfRows: Long): Registration = {
    val dataFrameSizeTable = tables.get(DataFrameReportGenerator.DataFrameSizeTableName).get
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
