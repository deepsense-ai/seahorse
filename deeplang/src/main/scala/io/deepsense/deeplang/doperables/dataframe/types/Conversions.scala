/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe.types

import java.lang.{Boolean => JavaBoolean, Double => JavaDouble, Integer => JavaInteger}
import java.sql.Timestamp

import org.apache.spark.sql.UserDefinedFunction
import org.apache.spark.sql.functions._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoriesMapping
import io.deepsense.deeplang.doperations.exceptions.TypeConversionException
import io.deepsense.deeplang.parameters.ColumnType
import io.deepsense.deeplang.parameters.ColumnType._

object Conversions {

  def nullOr[A <: AnyRef, B](f: A => B)(value: A): B =
    if (value == null) null.asInstanceOf[B] else f(value)

  def generateCategoricalConversion(
      mapping: CategoriesMapping,
      targetType: ColumnType.ColumnType): UserDefinedFunction = {
    val mapToString = categoricalToString(mapping) _
    if (targetType == ColumnType.numeric) {
      udf[java.lang.Double, java.lang.Integer](nullOr(mapToString.andThen(stringToDouble)))
    } else if (targetType == ColumnType.string) {
      udf[String, java.lang.Integer](nullOr(mapToString))
    } else {
      ???
    }
  }

  // (from, to) -> UDF
  val UdfConverters: Map[(ColumnType, ColumnType), UserDefinedFunction] = Map(
    (ColumnType.boolean, ColumnType.string) ->
      udf[String, java.lang.Boolean](booleanToString),
    (ColumnType.numeric, ColumnType.string) ->
      udf[String, java.lang.Double](doubleToString),
    (ColumnType.timestamp, ColumnType.string) ->
      udf[String, Timestamp](nullOr(timestampToString)),

    (ColumnType.boolean, ColumnType.numeric) ->
      udf[java.lang.Double, java.lang.Boolean](booleanToDouble),
    (ColumnType.string, ColumnType.numeric) ->
      udf[java.lang.Double, String](stringToDouble),
    (ColumnType.timestamp, ColumnType.numeric) ->
      udf[java.lang.Double, Timestamp](timestampToDouble)
  )

  def booleanToString(b: JavaBoolean): String = nullOr[JavaBoolean, String](_.toString)(b)
  def doubleToString(d: JavaDouble): String =
    nullOr[JavaDouble, String](DoubleUtils.double2String(_))(d)
  def timestampToString(t: Timestamp): String =
    nullOr[Timestamp, String] { x =>
      DateTimeConverter.toString(DateTimeConverter.fromMillis(x.getTime))
    }(t)
  def categoricalToString(mapping: CategoriesMapping)(c: JavaInteger) =
    nullOr[JavaInteger, String](mapping.idToValue(_))(c)
  def booleanToDouble(b: JavaBoolean): JavaDouble =
    nullOr[JavaBoolean, JavaDouble](x => if (x) 1.0 else 0.0)(b)
  def stringToDouble(s: String): JavaDouble = {
    nullOr[String, java.lang.Double] { x =>
      try {
        java.lang.Double.parseDouble(x)
      } catch {
        case _: NumberFormatException =>
          throw new TypeConversionException(x, ColumnType.string, ColumnType.numeric)
      }
    }(s)
  }
  def timestampToDouble(t: Timestamp): JavaDouble =
    nullOr[Timestamp, JavaDouble](_.getTime.toDouble)(t)
  def anyToString(x: Any): String = {
    x match {
      case d: JavaDouble => doubleToString(d)
      case t: Timestamp => timestampToString(t)
      case s: String => s
      case b: JavaBoolean => booleanToString(b)
    }
  }
}
