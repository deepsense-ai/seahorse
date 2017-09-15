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

package ai.deepsense.deeplang.doperables.dataframe

import java.sql.Timestamp

import org.apache.spark.SparkContext
import ai.deepsense.sparkutils.Linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import ai.deepsense.commons.datetime.DateTimeConverter.dateTimeFromUTC

trait DataFrameTestFactory {

  import DataFrameTestFactory._

  def testDataFrame(dataFrameBuilder: DataFrameBuilder, sparkContext: SparkContext): DataFrame =
    dataFrameBuilder.buildDataFrame(
      testSchema,
      testRDD(sparkContext))

  def oneValueDataFrame(
      dataFrameBuilder: DataFrameBuilder,
      sparkContext: SparkContext): DataFrame =
    dataFrameBuilder.buildDataFrame(
      testSchema,
      sameValueRDD(sparkContext))

  def allTypesDataFrame(dataFrameBuilder: DataFrameBuilder, sparkContext: SparkContext): DataFrame =
    dataFrameBuilder.buildDataFrame(
      allTypesSchema,
      allTypesRDD(sparkContext))

  val testSchema: StructType = StructType(Array(
    StructField(stringColumnName, StringType),
    StructField(booleanColumnName, BooleanType),
    StructField(doubleColumnName, DoubleType),
    StructField(timestampColumnName, TimestampType),
    StructField(integerColumnName, IntegerType)
  ))

  val allTypesSchema: StructType = StructType(Seq(
    StructField(byteColumnName, ByteType),
    StructField(shortColumnName, ShortType),
    StructField(integerColumnName, IntegerType),
    StructField(longColumnName, LongType),
    StructField(floatColumnName, FloatType),
    StructField(doubleColumnName, DoubleType),
    StructField(decimalColumnName, DecimalType(10, 2)),
    StructField(stringColumnName, StringType),
    StructField(binaryColumnName, BinaryType),
    StructField(booleanColumnName, BooleanType),
    StructField(timestampColumnName, TimestampType),
    StructField(dateColumnName, DateType),
    StructField(arrayColumnName, ArrayType(IntegerType)),
    StructField(mapColumnName, DataTypes.createMapType(StringType, IntegerType)),
    StructField(structColumnName, StructType(Seq(StructField("x", IntegerType)))),
    StructField(vectorColumnName, new ai.deepsense.sparkutils.Linalg.VectorUDT)
  ))

  def testRDD(sparkContext: SparkContext): RDD[Row] = sparkContext.parallelize(Seq(
    Row("Name1", false, 1.67, timestampUTC(1970, 1, 20, 0, 43), 0),
    Row("Name2", false, 1.95, timestampUTC(1990, 2, 11, 0, 43), 1),
    Row("Name3", false, 1.87, timestampUTC(1999, 7, 2, 0, 43), 3),
    Row("Name4", false, 1.7, timestampUTC(1954, 12, 18, 0, 43), 0),
    Row("Name5", false, 2.07, timestampUTC(1987, 4, 27, 0, 43), null),
    Row(null, true, 1.307, timestampUTC(2010, 1, 7, 0, 0), 2),
    Row("Name7", null, 2.132, timestampUTC(2000, 4, 27, 0, 43), 1),
    Row("Name8", true, 1.777, timestampUTC(1996, 10, 24, 0, 43), 1),
    Row("Name9", true, null, timestampUTC(1999, 1, 6, 0, 0), 0),
    Row("Name10", true, 1.99, null, 1)
  ))

  def sameValueRDD(sparkContext: SparkContext): RDD[Row] = sparkContext.parallelize(Seq(
    Row("Name1", false, 1.67, timestampUTC(1970, 1, 20, 0, 43), 1),
    Row("Name1", false, 1.67, timestampUTC(1970, 1, 20, 0, 43), 1),
    Row("Name1", false, 1.67, timestampUTC(1970, 1, 20, 0, 43), 1),
    Row("Name1", false, 1.67, timestampUTC(1970, 1, 20, 0, 43), 1),
    Row("Name1", false, 1.67, timestampUTC(1970, 1, 20, 0, 43), 1),
    Row("Name1", false, 1.67, timestampUTC(1970, 1, 20, 0, 43), 1),
    Row("Name1", false, 1.67, timestampUTC(1970, 1, 20, 0, 43), 1),
    Row("Name1", false, 1.67, timestampUTC(1970, 1, 20, 0, 43), 1),
    Row("Name1", false, 1.67, timestampUTC(1970, 1, 20, 0, 43), 1),
    Row("Name1", false, 1.67, timestampUTC(1970, 1, 20, 0, 43), 1)
  ))

  def allTypesRDD(sparkContext: SparkContext): RDD[Row] = sparkContext.parallelize(Seq(
    Row(
      Byte.box(0),
      Short.box(0),
      0,
      0L,
      0.0f,
      0.0,
      BigDecimal(0),
      "x",
      Array[Byte](),
      false,
      timestampUTC(1970, 1, 20, 0, 0),
      java.sql.Date.valueOf("1970-01-20"),
      Array(1, 2, 3),
      Map("x" -> 0),
      Row(0),
      Vectors.dense(1, 2, 3)),
    Row(
      Byte.box(1),
      Short.box(1),
      1,
      1L,
      1.0f,
      1.0,
      BigDecimal(1),
      "y",
      Array[Byte](),
      true,
      timestampUTC(1970, 1, 22, 0, 0),
      java.sql.Date.valueOf("1970-01-22"),
      Array(4, 5, 6),
      Map("y" -> 1),
      Row(1),
      Vectors.dense(4, 5, 6))
  ))

  private def timestampUTC(
      year: Int,
      month: Int,
      day: Int,
      hour: Int = 0,
      minutes: Int = 0,
      seconds: Int = 0): Timestamp =
    new Timestamp(
      dateTimeFromUTC(year, month, day, hour, minutes, seconds).getMillis)
}

object DataFrameTestFactory extends DataFrameTestFactory {
  val stringColumnName = "stringColumn"
  val booleanColumnName = "booleanColumn"
  val doubleColumnName = "doubleColumn"
  val timestampColumnName = "timestampColumn"
  val integerColumnName = "integerColumn"

  val byteColumnName = "byteColumn"
  val shortColumnName = "shortColumn"
  val longColumnName = "longColumn"
  val floatColumnName = "floatColumn"
  val decimalColumnName = "decimalColumn"
  val binaryColumnName = "binaryColumn"
  val dateColumnName = "dateColumn"
  val arrayColumnName = "arrayColumn"
  val mapColumnName = "mapColumn"
  val structColumnName = "structColumn"
  val vectorColumnName = "vectorColumn"
}
