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

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime

import io.deepsense.commons.datetime.DateTimeConverter

trait DataFrameTestFactory {

  def testDataFrame(dataFrameBuilder: DataFrameBuilder, sparkContext: SparkContext): DataFrame =
    dataFrameBuilder.buildDataFrame(
      testSchema,
      testRDD(sparkContext),
      Seq(DataFrameTestFactory.categoricalColumnName))

  def oneValueDataFrame(
      dataFrameBuilder: DataFrameBuilder,
      sparkContext: SparkContext): DataFrame =
    dataFrameBuilder.buildDataFrame(
      testSchema,
      sameValueRDD(sparkContext),
      Seq(DataFrameTestFactory.categoricalColumnName))

  val testSchema: StructType = StructType(Array(
    StructField(DataFrameTestFactory.stringColumnName, StringType),
    StructField(DataFrameTestFactory.booleanColumnName, BooleanType),
    StructField(DataFrameTestFactory.doubleColumnName, DoubleType),
    StructField(DataFrameTestFactory.timestampColumnName, TimestampType),
    StructField(DataFrameTestFactory.categoricalColumnName, StringType)
  ))

  def testRDD(sparkContext: SparkContext): RDD[Row] = sparkContext.parallelize(Seq(
    Row("Name1", false, 1.67, timestamp(1970, 1, 20, 0, 43), "summer"),
    Row("Name2", false, 1.95, timestamp(1990, 2, 11, 0, 43), "summer"),
    Row("Name3", false, 1.87, timestamp(1999, 7, 2, 0, 43), "winter"),
    Row("Name4", false, 1.7, timestamp(1954, 12, 18, 0, 43), "spring"),
    Row("Name5", false, 2.07, timestamp(1987, 4, 27, 0, 43), null),
    Row(null, true, 1.307, timestamp(2010, 1, 7, 0, 0), "autumn"),
    Row("Name7", null, 2.132, timestamp(2000, 4, 27, 0, 43), "summer"),
    Row("Name8", true, 1.777, timestamp(1996, 10, 24, 0, 43), "summer"),
    Row("Name9", true, null, timestamp(1999, 1, 6, 0, 0), "spring"),
    Row("Name10", true, 1.99, null, "summer")
  ))

  def sameValueRDD(sparkContext: SparkContext): RDD[Row] = sparkContext.parallelize(Seq(
    Row("Name1", false, 1.67, timestamp(1970, 1, 20, 0, 43), "summer"),
    Row("Name1", false, 1.67, timestamp(1970, 1, 20, 0, 43), "summer"),
    Row("Name1", false, 1.67, timestamp(1970, 1, 20, 0, 43), "summer"),
    Row("Name1", false, 1.67, timestamp(1970, 1, 20, 0, 43), "summer"),
    Row("Name1", false, 1.67, timestamp(1970, 1, 20, 0, 43), "summer"),
    Row("Name1", false, 1.67, timestamp(1970, 1, 20, 0, 43), "summer"),
    Row("Name1", false, 1.67, timestamp(1970, 1, 20, 0, 43), "summer"),
    Row("Name1", false, 1.67, timestamp(1970, 1, 20, 0, 43), "summer"),
    Row("Name1", false, 1.67, timestamp(1970, 1, 20, 0, 43), "summer"),
    Row("Name1", false, 1.67, timestamp(1970, 1, 20, 0, 43), "summer")
  ))

  private def timestamp(
      year: Int,
      month: Int,
      day: Int,
      hour: Int,
      minutes: Int): Timestamp =
    new Timestamp(new DateTime(year, month, day, hour, minutes, DateTimeConverter.zone).getMillis)
}

object DataFrameTestFactory extends DataFrameTestFactory {
  val stringColumnName = "Name"
  val booleanColumnName = "BusinessAccount"
  val doubleColumnName = "Whatever"
  val timestampColumnName = "AccountCreationDate"
  val categoricalColumnName = "Season"
}
