/**
 * Copyright (c) 2015, CodiLime Inc.
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
    dataFrameBuilder.buildDataFrame(testSchema, testRDD(sparkContext))

  def oneValueDataFrame(
    dataFrameBuilder: DataFrameBuilder,
    sparkContext: SparkContext): DataFrame =
    dataFrameBuilder.buildDataFrame(testSchema, sameValueRDD(sparkContext))

  val testSchema: StructType = StructType(Array(
    StructField(DataFrameTestFactory.stringColumnName, StringType),
    StructField(DataFrameTestFactory.booleanColumnName, BooleanType),
    StructField(DataFrameTestFactory.longColumnName, LongType),
    StructField(DataFrameTestFactory.doubleColumnName, DoubleType),
    StructField(DataFrameTestFactory.timestampColumnName, TimestampType)
    //TODO: Categorical type currency
  ))

  def testRDD(sparkContext: SparkContext): RDD[Row] = sparkContext.parallelize(Seq(
    Row("Name1", false, 14L, 1.67, timestamp(1970, 1, 20, 0, 43)),
    Row("Name2", false, 1L, 1.95, timestamp(1990, 2, 11, 0, 43)),
    Row("Name3", false, 100L, 1.87, timestamp(1999, 7, 2, 0, 43)),
    Row("Name4", false, 432L, 1.7, timestamp(1954, 12, 18, 0, 43)),
    Row("Name5", false, 43L, 2.07, timestamp(1987, 4, 27, 0, 43)),
    Row(null, true, 10000000L, 1.307, timestamp(2010, 1, 7, 0, 0)),
    Row("Name7", null, 20000L, 2.132, timestamp(2000, 4, 27, 0, 43)),
    Row("Name8", true, null, 1.777, timestamp(1996, 10, 24, 0, 43)),
    Row("Name9", true, 1234L, null, timestamp(1999, 1, 6, 0, 0)),
    Row("Name10", true, 98798797L, 1.99, null)
  ))

  def sameValueRDD(sparkContext: SparkContext): RDD[Row] = sparkContext.parallelize(Seq(
    Row("Name1", false, 14L, 1.67, timestamp(1970, 1, 20, 0, 43)),
    Row("Name1", false, 14L, 1.67, timestamp(1970, 1, 20, 0, 43)),
    Row("Name1", false, 14L, 1.67, timestamp(1970, 1, 20, 0, 43)),
    Row("Name1", false, 14L, 1.67, timestamp(1970, 1, 20, 0, 43)),
    Row("Name1", false, 14L, 1.67, timestamp(1970, 1, 20, 0, 43)),
    Row("Name1", false, 14L, 1.67, timestamp(1970, 1, 20, 0, 43)),
    Row("Name1", false, 14L, 1.67, timestamp(1970, 1, 20, 0, 43)),
    Row("Name1", false, 14L, 1.67, timestamp(1970, 1, 20, 0, 43)),
    Row("Name1", false, 14L, 1.67, timestamp(1970, 1, 20, 0, 43)),
    Row("Name1", false, 14L, 1.67, timestamp(1970, 1, 20, 0, 43))
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
  val longColumnName = "GrossBalance"
  val doubleColumnName = "Whatever"
  val timestampColumnName = "AccountCreationDate"
}
