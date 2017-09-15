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

package io.deepsense.deeplang.doperables.dataframe.types

import java.lang.{Boolean => JavaBoolean, Double => JavaDouble}
import java.sql.Timestamp

import org.apache.spark.sql.UserDefinedFunction
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.doperations.exceptions.TypeConversionException

object Conversions {

  def nullOr[A <: AnyRef, B](f: A => B)(value: A): B =
    if (value == null) null.asInstanceOf[B] else f(value)

  // (from, to) -> UDF
  val UdfConverters: Map[(DataType, DataType), UserDefinedFunction] = Map(
    (BooleanType, StringType) ->
      udf[String, java.lang.Boolean](defaultToString),
    (ByteType, StringType) ->
      udf[String, java.lang.Byte](defaultToString),
    (DecimalType(), StringType) ->
      udf[String, java.math.BigDecimal](defaultToString),
    (DoubleType, StringType) ->
      udf[String, java.lang.Double](doubleToString),
    (FloatType, StringType) ->
      udf[String, java.lang.Float](floatToString),
    (IntegerType, StringType) ->
      udf[String, java.lang.Integer](defaultToString),
    (LongType, StringType) ->
      udf[String, java.lang.Long](defaultToString),
    (ShortType, StringType) ->
      udf[String, java.lang.Short](defaultToString),
    (TimestampType, StringType) ->
      udf[String, Timestamp](nullOr(timestampToString)),

    (BooleanType, DoubleType) ->
      udf[java.lang.Double, java.lang.Boolean](booleanToDouble),
    (ByteType, DoubleType) ->
      udf[java.lang.Double, java.lang.Byte](defaultToDouble),
    (DecimalType(), DoubleType) ->
      udf[java.lang.Double, java.math.BigDecimal](defaultToDouble),
    (FloatType, DoubleType) ->
      udf[java.lang.Double, java.lang.Float](defaultToDouble),
    (IntegerType, DoubleType) ->
      udf[java.lang.Double, java.lang.Integer](defaultToDouble),
    (LongType, DoubleType) ->
      udf[java.lang.Double, java.lang.Long](defaultToDouble),
    (ShortType, DoubleType) ->
      udf[java.lang.Double, java.lang.Short](defaultToDouble),
    (StringType, DoubleType) ->
      udf[java.lang.Double, String](stringToDouble),
    (TimestampType, DoubleType) ->
      udf[java.lang.Double, Timestamp](timestampToDouble)
  )

  def defaultToString[T <: AnyRef](value: T): String = nullOr[T, String](_.toString)(value)

  def defaultToDouble[T <: java.lang.Number](value: T): JavaDouble =
    nullOr[T, JavaDouble](_.doubleValue())(value)

  def floatToString(f: java.lang.Float): String =
    nullOr[java.lang.Float, String](v => DoubleUtils.double2String(v.toDouble))(f)

  def doubleToString(d: JavaDouble): String =
    nullOr[JavaDouble, String](DoubleUtils.double2String(_))(d)

  def timestampToString(t: Timestamp): String =
    nullOr[Timestamp, String] { x =>
      DateTimeConverter.toString(DateTimeConverter.fromMillis(x.getTime))
    }(t)

  def booleanToDouble(b: JavaBoolean): JavaDouble =
    nullOr[JavaBoolean, JavaDouble](x => if (x) 1.0 else 0.0)(b)

  def stringToDouble(s: String): JavaDouble = nullOr[String, java.lang.Double] { x =>
      try {
        java.lang.Double.parseDouble(x)
      } catch {
        case _: NumberFormatException =>
          throw new TypeConversionException(x, ColumnType.string, ColumnType.numeric)
      }
    }(s)

  def timestampToDouble(t: Timestamp): JavaDouble =
    nullOr[Timestamp, JavaDouble](_.getTime.toDouble)(t)

  def anyToString(x: Any): String = x match {
    case d: JavaDouble => doubleToString(d)
    case t: Timestamp => timestampToString(t)
    case s: String => s
    case b: JavaBoolean => defaultToString(b)
  }
}
