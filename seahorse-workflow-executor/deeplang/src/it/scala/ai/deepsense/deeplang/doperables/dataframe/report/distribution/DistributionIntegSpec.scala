/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables.dataframe.report.distribution

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import ai.deepsense.commons.datetime.DateTimeConverter.{dateTime, dateTimeFromUTC, toString => dateToString}
import ai.deepsense.commons.utils.DoubleUtils
import ai.deepsense.deeplang.DeeplangIntegTestSupport
import ai.deepsense.deeplang.doperables.dataframe.report.DataFrameReportGenerator
import ai.deepsense.deeplang.doperables.dataframe.report.distribution.discrete.DiscreteDistributionBuilderFactory
import ai.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameTestFactory}
import ai.deepsense.deeplang.doperables.report.Report
import ai.deepsense.reportlib.model._

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
          dateToString(dateTimeFromUTC(2010, 1, 7, 0, 0, 0)),
          dateToString(dateTimeFromUTC(1954, 12, 18, 0, 43, 0)),
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
          (0 to 20).map(x => x * 0.05).map(DoubleUtils.double2String),
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
        timestampBucketsForSparkTypesTestFromUTC,
        Seq(1L) ++ Seq.fill(18)(0L) ++ Seq(1L),
        Statistics(
          dateToString(dateTimeFromUTC(1970, 1, 22, 0, 0, 0)),
          dateToString(dateTimeFromUTC(1970, 1, 20, 0, 0, 0)),
          dateToString(dateTimeFromUTC(1970, 1, 21, 0, 0, 0))))

      testContinuousDistribution(
        distributions,
        dateColumnName,
        0L,
        timestampBucketsForSparkTypesTestFromDefault,
        Seq(1L) ++ Seq.fill(18)(0L) ++ Seq(1L),
        Statistics(
          dateToString(dateTime(1970, 1, 22, 0, 0, 0)),
          dateToString(dateTime(1970, 1, 20, 0, 0, 0)),
          dateToString(dateTime(1970, 1, 21, 0, 0, 0))))

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
        Seq(dateTimeFromUTC(1970, 1, 20, 0, 43, 0), dateTimeFromUTC(1970, 1, 20, 0, 43, 0))
          .map(dateToString),
        Seq(10),
        Statistics(
          dateToString(dateTimeFromUTC(1970, 1, 20, 0, 43, 0)),
          dateToString(dateTimeFromUTC(1970, 1, 20, 0, 43, 0)),
          dateToString(dateTimeFromUTC(1970, 1, 20, 0, 43, 0)))
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
            (1 to rowCount).map(x => Row(x.toString))
          )
        )
        val report = dataFrame.report()
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
    val report = data.report()
    report.content.distributions
  }

  val doubleTypeBuckets: Array[String] = Array(
    "1.307", "1.34825", "1.3895", "1.43075", "1.472", "1.51325", "1.5545", "1.59575", "1.637",
    "1.67825", "1.7195", "1.76075", "1.802", "1.84325", "1.8845", "1.92575", "1.967", "2.00825",
    "2.0495", "2.09075", "2.132")

  val integerTypeBuckets: Array[String] = Array("0", "0", "1", "2", "3")

  val timestampTypeBuckets: Array[String] = Array(
    dateTimeFromUTC(1954, 12, 18, 0, 43, 0), dateTimeFromUTC(1957, 9, 18, 11, 28, 51),
    dateTimeFromUTC(1960, 6, 19, 22, 14, 42), dateTimeFromUTC(1963, 3, 22, 9, 0, 33),
    dateTimeFromUTC(1965, 12, 21, 19, 46, 24), dateTimeFromUTC(1968, 9, 22, 6, 32, 15),
    dateTimeFromUTC(1971, 6, 24, 17, 18, 6), dateTimeFromUTC(1974, 3, 26, 4, 3, 57),
    dateTimeFromUTC(1976, 12, 25, 14, 49, 48), dateTimeFromUTC(1979, 9, 27, 1, 35, 39),
    dateTimeFromUTC(1982, 6, 28, 12, 21, 30), dateTimeFromUTC(1985, 3, 29, 23, 7, 21),
    dateTimeFromUTC(1987, 12, 30, 9, 53, 12), dateTimeFromUTC(1990, 9, 30, 20, 39, 3),
    dateTimeFromUTC(1993, 7, 2, 7, 24, 54), dateTimeFromUTC(1996, 4, 2, 18, 10, 45),
    dateTimeFromUTC(1999, 1, 3, 4, 56, 36), dateTimeFromUTC(2001, 10, 4, 15, 42, 27),
    dateTimeFromUTC(2004, 7, 6, 2, 28, 18), dateTimeFromUTC(2007, 4, 7, 13, 14, 9),
    dateTimeFromUTC(2010, 1, 7, 0, 0, 0)).map(dateToString)

  val timestampBucketsForSparkTypesTestFromUTC: Array[String] =
    Array(
      dateTimeFromUTC(1970, 1, 20, 0, 0, 0), dateTimeFromUTC(1970, 1, 20, 2, 24, 0),
      dateTimeFromUTC(1970, 1, 20, 4, 48, 0), dateTimeFromUTC(1970, 1, 20, 7, 12, 0),
      dateTimeFromUTC(1970, 1, 20, 9, 36, 0), dateTimeFromUTC(1970, 1, 20, 12, 0, 0),
      dateTimeFromUTC(1970, 1, 20, 14, 24, 0), dateTimeFromUTC(1970, 1, 20, 16, 48, 0),
      dateTimeFromUTC(1970, 1, 20, 19, 12, 0), dateTimeFromUTC(1970, 1, 20, 21, 36, 0),
      dateTimeFromUTC(1970, 1, 21, 0, 0, 0), dateTimeFromUTC(1970, 1, 21, 2, 24, 0),
      dateTimeFromUTC(1970, 1, 21, 4, 48, 0), dateTimeFromUTC(1970, 1, 21, 7, 12, 0),
      dateTimeFromUTC(1970, 1, 21, 9, 36, 0), dateTimeFromUTC(1970, 1, 21, 12, 0, 0),
      dateTimeFromUTC(1970, 1, 21, 14, 24, 0), dateTimeFromUTC(1970, 1, 21, 16, 48, 0),
      dateTimeFromUTC(1970, 1, 21, 19, 12, 0), dateTimeFromUTC(1970, 1, 21, 21, 36, 0),
      dateTimeFromUTC(1970, 1, 22, 0, 0, 0)).map(dateToString)

  val timestampBucketsForSparkTypesTestFromDefault: Array[String] =
    Array(
      dateTime(1970, 1, 20, 0, 0, 0), dateTime(1970, 1, 20, 2, 24, 0),
      dateTime(1970, 1, 20, 4, 48, 0), dateTime(1970, 1, 20, 7, 12, 0),
      dateTime(1970, 1, 20, 9, 36, 0), dateTime(1970, 1, 20, 12, 0, 0),
      dateTime(1970, 1, 20, 14, 24, 0), dateTime(1970, 1, 20, 16, 48, 0),
      dateTime(1970, 1, 20, 19, 12, 0), dateTime(1970, 1, 20, 21, 36, 0),
      dateTime(1970, 1, 21, 0, 0, 0), dateTime(1970, 1, 21, 2, 24, 0),
      dateTime(1970, 1, 21, 4, 48, 0), dateTime(1970, 1, 21, 7, 12, 0),
      dateTime(1970, 1, 21, 9, 36, 0), dateTime(1970, 1, 21, 12, 0, 0),
      dateTime(1970, 1, 21, 14, 24, 0), dateTime(1970, 1, 21, 16, 48, 0),
      dateTime(1970, 1, 21, 19, 12, 0), dateTime(1970, 1, 21, 21, 36, 0),
      dateTime(1970, 1, 22, 0, 0, 0)).map(dateToString)

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
      numberOfRows: Long) = {
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
