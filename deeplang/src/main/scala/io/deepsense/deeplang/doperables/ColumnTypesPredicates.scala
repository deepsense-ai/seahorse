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

package io.deepsense.deeplang.doperables

import scala.util.{Failure, Success, Try}

import org.apache.spark.sql.types.StructField

import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperables.dataframe.types.categorical.MappingMetadataConverter
import io.deepsense.deeplang.doperations.exceptions.WrongColumnTypeException
import io.deepsense.deeplang.parameters.ColumnType

object ColumnTypesPredicates {
  type Predicate = StructField => Try[Unit]

  def isNumeric: Predicate = (field) =>
    SparkConversions.sparkColumnTypeToColumnType(field.dataType) match {
      case ColumnType.numeric => Success()
      case _ =>
        val columnType = SparkConversions.sparkColumnTypeToColumnType(field.dataType)
        Failure(WrongColumnTypeException(field.name, columnType, ColumnType.numeric))
    }

  def isNumericOrCategorical: Predicate = (field) =>
    SparkConversions.sparkColumnTypeToColumnType(field.dataType) match {
      case ColumnType.numeric => Success()
      case ColumnType.categorical => Success()
      case _ =>
        val columnType = SparkConversions.sparkColumnTypeToColumnType(field.dataType)
        Failure(WrongColumnTypeException(field.name, columnType,
          ColumnType.numeric, ColumnType.categorical))
    }

  def isNumericOrBinaryValued: Predicate = (field) =>
    SparkConversions.sparkColumnTypeToColumnType(field.dataType) match {
      case ColumnType.boolean => Success()
      case ColumnType.numeric => Success()
      case ColumnType.categorical =>
        MappingMetadataConverter.mappingFromMetadata(field.metadata).get.values.size match {
          case 1 => Success()
          case 2 => Success()
          case categoriesCount =>
            Failure(WrongColumnTypeException(
              s"Column '${field.name}' is '${ColumnType.categorical}' " +
                s"with more than 2 levels: $categoriesCount."))
        }
      case _ =>
        val columnType = SparkConversions.sparkColumnTypeToColumnType(field.dataType)
        Failure(WrongColumnTypeException(
          s"Column '${field.name}' is of type '$columnType', " +
            s"which is unsupported in this operation. " +
            s"Expected '${ColumnType.boolean}', '${ColumnType.numeric}' " +
            s"or '${ColumnType.categorical}' with 2 levels."))
    }
}
