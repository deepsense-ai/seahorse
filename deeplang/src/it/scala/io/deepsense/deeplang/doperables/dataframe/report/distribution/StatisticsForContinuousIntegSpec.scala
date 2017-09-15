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

import java.sql.Timestamp

import org.apache.spark.rdd.RDD
import org.apache.spark.sql
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.doperables.dataframe.report.distribution.continuous.ContinuousDistributionBuilderFactory
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameTestFactory}
import io.deepsense.deeplang.doperables.dataframe.report.DataFrameReportGenerator
import io.deepsense.reportlib.model._
import io.deepsense.reportlib.model.Distribution

class StatisticsForContinuousIntegSpec extends DeeplangIntegTestSupport with DataFrameTestFactory {

  "Statistics (Min, max and mean values)" should {
    "be calculated for each continuous column in distribution" when {
      "data is of type int" in {
        val distribution = distributionForInt(1, 2, 3, 4, 5)
        distribution.statistics.min shouldEqual Some("1")
        distribution.statistics.max shouldEqual Some("5")
        distribution.statistics.mean shouldEqual Some("3")
      }
      "data is of type Timestamp" in {
        val distribution =
          distributionForTimestamps(new Timestamp(1000), new Timestamp(2000), new Timestamp(3000))
        distribution.statistics.min shouldEqual Some(formatDate(1000))
        distribution.statistics.max shouldEqual Some(formatDate(3000))
        distribution.statistics.mean shouldEqual Some(formatDate(2000))
      }
    }
  }
  "Null value in data" should {
    val distribution = distributionForDouble(1, 2, 3, 4, Double.NaN, 5)
    "not be skipped in calculating min and max" in {
      distribution.statistics.min shouldEqual Some("1")
      distribution.statistics.max shouldEqual Some("5")
    }
    "result in mean value NaN" in {
      distribution.statistics.mean shouldEqual Some("NaN")
    }
  }

  val columnName = "column_name"

  private def distributionForDouble(data: Double*): ContinuousDistribution = {
    distributionFor(data, DoubleType)
  }

  private def distributionForInt(data: Int*): ContinuousDistribution = {
    distributionFor(data, IntegerType)
  }

  private def distributionForTimestamps(data: Timestamp*): ContinuousDistribution = {
    distributionFor(data, TimestampType)
  }

  private def distributionFor(data: Seq[Any], dataType: DataType): ContinuousDistribution = {
    val schema = StructType(Array(
      StructField(columnName, dataType)
    ))

    val rows = data.map(v => Row(v))
    val rdd = sparkContext.parallelize(rows)

    val sparkDataFrame: sql.DataFrame = sqlContext.createDataFrame(rdd, schema)
    val dataFrame = DataFrame.fromSparkDataFrame(sparkDataFrame)

    val report = dataFrame.report(executionContext)
    report.content.distributions(columnName).asInstanceOf[ContinuousDistribution]
  }

  def buildDataFrame(schema: StructType, data: RDD[Row]): DataFrame = {
    val dataFrame: sql.DataFrame = sqlContext.createDataFrame(data, schema)
    DataFrame.fromSparkDataFrame(dataFrame)
  }

  def formatDate(millis: Long): String = {
    DateTimeConverter.toString(DateTimeConverter.fromMillis(millis))
  }

}
