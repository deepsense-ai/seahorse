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

import scala.annotation.tailrec

import org.apache.spark.sql.types.StructType

import io.deepsense.commons.types.ColumnType.ColumnType
import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperations.exceptions.{ColumnDoesNotExistException, ColumnsDoNotExistException}
import io.deepsense.deeplang.params.selections._

trait DataFrameColumnsGetter {

  this: DataFrame =>

  /**
   * Returns name of column based on selection.
   * Throws [[ColumnDoesNotExistException]] if out-of-range index
   * or non-existing column name is selected.
   */
  def getColumnName(singleColumnSelection: SingleColumnSelection): String =
    DataFrameColumnsGetter.getColumnName(sparkDataFrame.schema, singleColumnSelection)

  /**
   * Names of columns selected by provided selections.
   * Order of returned columns is the same as in schema.
   * If a column will occur in many selections, it won't be duplicated in result.
   * Throws [[ColumnsDoNotExistException]] if out-of-range indexes
   * or non-existing column names are selected.
   */
  def getColumnNames(multipleColumnSelection: MultipleColumnSelection): Seq[String] =
    DataFrameColumnsGetter.getColumnNames(sparkDataFrame.schema, multipleColumnSelection)
}

object DataFrameColumnsGetter {
  /**
   * Creates column name by adding some suffix to base column name. Appends integer if it is
   * not equal to 0. Parts of name are separated by underscore.
   * E. g. createColumnName('xyz', 'a', 3) returns 'xyz_a_3',
   * and createColumnName('xyz', 'a', 0) returns 'xyz_a'.
   */
  private[deeplang] def createColumnName(
    baseColumnName: String,
    addedPart: String,
    level: Int): String = {
    val levelSuffix = if (level > 0) "_" + level else ""
    (baseColumnName + "_" + addedPart + levelSuffix).replace(".", "_")
    // TODO: 'replace' should be removed after spark upgrade to 1.4 version. DS-635
  }

  /**
   * Returns name of column based on selection.
   * Throws [[ColumnDoesNotExistException]] if out-of-range index
   * or non-existing column name is selected.
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
   * Names of columns selected by provided selections.
   * Order of returned columns is the same as in schema.
   * If a column will occur in many selections, it won't be duplicated in result.
   * Throws [[ColumnsDoNotExistException]] if out-of-range indexes
   * or non-existing column names are selected.
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

  /**
    * Method useful for generating names for new columns. When we want to add new columns
    * to a dataframe, we need to generate a new name for them assuring that this name is not already
    * used in the dataframe. Common use case is when we generate new columns' names based on
    * existing column name by adding some sort of extension. When new name generated using base
    * column name and extension is already used, we want to add level number.
    * E. g. assume that we have column named 'xyz' and we want to add two new columns, with
    * suffixes '_a' and '_b', so 'xyz_a' and 'xyz_b'. But if there already is column 'xyz_a' in
    * dataframe, we want new columns to be named 'xyz_a_1' and 'xyz_b_1'.
    * This method allows to compute lowest unoccupied level provided original column name
    * ('xyz' from example) and extensions ({'a', 'b'} from example).
    * @param originalColumnName Name of the column which is a base for a new column.
    * @param newColumnNameExtensions Extensions of base name for generating new columns.
    * @return Lowest unoccupied level that can be used to generate a new name for columns.
    */
  def getFirstFreeNamesLevel(
      schema: StructType,
      originalColumnName: String,
      newColumnNameExtensions: Set[String]): Int = {

    val existingColumnsNames = schema.fieldNames.toSet

    @tailrec
    def getFirstFreeNamesLevel(level: Int): Int = {
      val levelTaken: Boolean = newColumnNameExtensions.exists(
        ext => existingColumnsNames.contains(DataFrameColumnsGetter.createColumnName(
          originalColumnName, ext, level))
      )
      if (levelTaken) getFirstFreeNamesLevel(level + 1) else level
    }

    getFirstFreeNamesLevel(0)
  }

  /**
    * Generates unique column name created of some other column's name, suffix and optionally
    * integer appended.
    * E. g. uniqueColumnName('xyz', 'a') returns 'xyz_a' if there is no such name in dataframe yet,
    * and it returns 'xyz_a_2' if there already are columns 'xyz_a' and 'xyz_a_1' in dataframe.
    */
  def uniqueColumnName(
      schema: StructType,
      originalColumnName: String,
      columnNameSuffix: String): String = {
    val level = getFirstFreeNamesLevel(schema, originalColumnName, Set(columnNameSuffix))
    createColumnName(originalColumnName, columnNameSuffix, level)
  }

  private def assertColumnSelectionsValid(
    schema: StructType,
    multipleColumnSelection: MultipleColumnSelection): Unit = {

    val selections = multipleColumnSelection.selections
    val invalidSelections = selections.filterNot(isSelectionValid(schema, _))
    if (invalidSelections.nonEmpty) {
      throw ColumnsDoNotExistException(invalidSelections, schema)
    }
  }

  def assertColumnNamesValid(schema: StructType, columns: Seq[String]): Unit = {
    assertColumnSelectionsValid(schema,
      MultipleColumnSelection(Vector(NameColumnSelection(columns.toSet)), excluding = false))
  }

  /**
   * Checks if given selection is valid with regard to dataframe schema.
   * Returns false if some specified names or indexes are incorrect.
   */
  private def isSelectionValid(
    schema: StructType,
    selection: ColumnSelection): Boolean = selection match {

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
  }
}
