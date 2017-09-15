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

import java.util.UUID

import org.apache.spark.sql.types.StructType

import ai.deepsense.commons.types.ColumnType.ColumnType
import ai.deepsense.commons.types.SparkConversions
import ai.deepsense.deeplang.doperations.exceptions.{ColumnDoesNotExistException, ColumnsDoNotExistException}
import ai.deepsense.deeplang.params.selections._

trait DataFrameColumnsGetter {

  this: DataFrame =>

  /**
   * Returns name of column based on selection.
   * Throws [[ai.deepsense.deeplang.doperations.exceptions.ColumnDoesNotExistException ColumnDoesNotExistException]]
   * if out-of-range index or non-existing column name is selected.
   */
  def getColumnName(singleColumnSelection: SingleColumnSelection): String =
    DataFrameColumnsGetter.getColumnName(sparkDataFrame.schema, singleColumnSelection)

  /**
   * Names of columns selected by provided selections.
   * Order of returned columns is the same as in schema.
   * If a column will occur in many selections, it won't be duplicated in result.
   * Throws [[ai.deepsense.deeplang.doperations.exceptions.ColumnDoesNotExistException ColumnsDoNotExistException]]
   * if out-of-range indexes or non-existing column names are selected.
   */
  def getColumnNames(multipleColumnSelection: MultipleColumnSelection): Seq[String] =
    DataFrameColumnsGetter.getColumnNames(sparkDataFrame.schema, multipleColumnSelection)
}

object DataFrameColumnsGetter {

  def uniqueSuffixedColumnName(column: String): String = column + "_" + UUID.randomUUID().toString

  def prefixedColumnName(column: String, prefix: String): String = prefix + column

  /**
   * Returns name of column based on selection.
   * Throws [[ai.deepsense.deeplang.doperations.exceptions.ColumnDoesNotExistException ColumnDoesNotExistException]]
   * if out-of-range index or non-existing column name is selected.
   */
  def getColumnName(
      schema: StructType,
      singleColumnSelection: SingleColumnSelection): String =
    tryGetColumnName(schema, singleColumnSelection).getOrElse {
      throw ColumnDoesNotExistException(singleColumnSelection, schema)
    }

  private def tryGetColumnName(
      schema: StructType,
      singleColumnSelection: SingleColumnSelection): Option[String] =
    singleColumnSelection match {
      case NameSingleColumnSelection(name) =>
        if (schema.fieldNames.contains(name)) Some(name) else None
      case IndexSingleColumnSelection(index) =>
        if (index >= 0 && index < schema.length) {
          Some(schema.fieldNames(index))
        } else {
          None
        }
    }

  /**
   * Throws [[ai.deepsense.deeplang.doperations.exceptions.WrongColumnTypeException WrongColumnTypeException]]
   * if column has type different than one of expected.
   */
  def assertExpectedColumnType(
      schema: StructType,
      singleColumnSelection: SingleColumnSelection,
      expectedTypes: ColumnType*): Unit = {
    val columnName = DataFrameColumnsGetter.getColumnName(schema, singleColumnSelection)
    DataFrame.assertExpectedColumnType(
      schema.fields.filter(_.name == columnName).head,
      expectedTypes: _*)
  }


  /**
   * Names of columns selected by provided selections.
   * Order of returned columns is the same as in schema.
   * If a column will occur in many selections, it won't be duplicated in result.
   * Throws [[ai.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException ColumnsDoNotExistException]]
   * if out-of-range indexes or non-existing column names are selected.
   */
  def getColumnNames(
    schema: StructType,
    multipleColumnSelection: MultipleColumnSelection): Seq[String] = {

    assertColumnSelectionsValid(schema, multipleColumnSelection)
    val selectedColumns = for {
      (column, index) <- schema.fields.zipWithIndex
      columnName = column.name
      columnType = SparkConversions.sparkColumnTypeToColumnType(column.dataType)
      selection <- multipleColumnSelection.selections
      if DataFrameColumnsGetter.isFieldSelected(columnName, index, columnType, selection)
    } yield columnName

    if (multipleColumnSelection.excluding) {
      schema.fieldNames.filterNot(selectedColumns.contains(_)).distinct
    } else {
      selectedColumns.distinct
    }
  }

  private def assertColumnSelectionsValid(
    schema: StructType,
    multipleColumnSelection: MultipleColumnSelection): Unit = {

    val selections = multipleColumnSelection.selections
    selections.foreach(checkSelectionValidity(schema, _))
  }

  def assertColumnNamesValid(schema: StructType, columns: Seq[String]): Unit = {
    assertColumnSelectionsValid(schema,
      MultipleColumnSelection(Vector(NameColumnSelection(columns.toSet)), excluding = false))
  }

  /**
   * Checks if given selection is valid with regard to dataframe schema.
   * Throws a ColumnsDoNotExistException if some specified names or indexes are incorrect.
   */
  private def checkSelectionValidity(
    schema: StructType,
    selection: ColumnSelection): Unit = {

    val valid = selection match {
      case IndexColumnSelection(indexes) =>
        val length = schema.length
        val indexesOutOfBounds = indexes.filter(index => index < 0 || index >= length)
        indexesOutOfBounds.isEmpty
      case NameColumnSelection(names) =>
        val allNames = schema.fieldNames.toSet
        val nonExistingNames = names.filter(!allNames.contains(_))
        nonExistingNames.isEmpty
      case TypeColumnSelection(_) => true
      case IndexRangeColumnSelection(Some(lowerBound), Some(upperBound)) =>
        schema.length > upperBound && lowerBound >= 0
      case IndexRangeColumnSelection(None, None) => true
      case IndexRangeColumnSelection(_, _) => throw new IllegalArgumentException("Malformed IndexRangeColumnSelection")
    }

    if (!valid) {
      throw ColumnsDoNotExistException(selection, schema)
    }
  }

  /**
   * Tells if column is selected by given selection.
   * Out-of-range indexes and non-existing column names are ignored.
   * @param columnName Name of field.
   * @param columnIndex Index of field in schema.
   * @param columnType Type of field's column.
   * @param selection Selection of columns.
   * @return True iff column meets selection's criteria.
   */
  private[DataFrameColumnsGetter] def isFieldSelected(
      columnName: String,
      columnIndex: Int,
      columnType: ColumnType,
      selection: ColumnSelection): Boolean = selection match {
    case IndexColumnSelection(indexes) => indexes.contains(columnIndex)
    case NameColumnSelection(names) => names.contains(columnName)
    case TypeColumnSelection(types) => types.contains(columnType)
    case IndexRangeColumnSelection(Some(lowerBound), Some(upperBound)) =>
      columnIndex >= lowerBound && columnIndex <= upperBound
    case IndexRangeColumnSelection(None, None) => false
    case IndexRangeColumnSelection(_, _) => throw new IllegalArgumentException("Malformed IndexRangeColumnSelection")
  }
}
