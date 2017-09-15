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

package ai.deepsense.deeplang.doperations.exceptions

import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException._
import ai.deepsense.deeplang.params.selections._

case class ColumnsDoNotExistException(
    invalidSelection: ColumnSelection,
    schema: StructType)
  extends DOperationExecutionException(exceptionMessage(invalidSelection, schema), None)

object ColumnsDoNotExistException {
  def apply(
      invalidSelection: ColumnSelection,
      dataFrame: DataFrame): ColumnsDoNotExistException =
    ColumnsDoNotExistException(invalidSelection, dataFrame.sparkDataFrame.schema)

  private def exceptionMessage(selection: ColumnSelection, schema: StructType): String =
    s"${selectionDescription(selection, schema)} (${schemaDescription(selection, schema)})"

  private def selectionDescription(selection: ColumnSelection, schema: StructType): String =
    selection match {
      case IndexColumnSelection(indices) =>
        s"One or more columns from index list: (${indices.mkString(", ")})" +
          " does not exist in the input DataFrame"
      case IndexRangeColumnSelection(begin, end) =>
        s"One or more columns from index range ${begin.get}..${end.get}" +
          " does not exist in the input DataFrame"
      case NameColumnSelection(names) =>
        val dfColumnNames = schema.map(field => field.name)
        val missingColumns = (names -- dfColumnNames.toSet).map(name => s"`$name`")
        val (pluralityDependentPrefix, pluralityDependentVerb) =
          if (missingColumns.size > 1) ("Columns:", "do") else ("Column", "does")
        s"$pluralityDependentPrefix ${missingColumns.mkString(", ")}" +
          s" ${pluralityDependentVerb} not exist in the input DataFrame"
      case TypeColumnSelection(_) =>
        throw new IllegalStateException("This shouldn't be called on TypeColumnSelection!")
    }

  private def schemaDescription(selection: ColumnSelection, schema: StructType): String = {
    selection match {
      case IndexColumnSelection(_) | IndexRangeColumnSelection(_, _) =>
        s"index range: 0..${schema.length - 1}"
      case NameColumnSelection(names) =>
        s"column names: ${schema.fields.map(field => s"`${field.name}`").mkString(", ")}"
      case TypeColumnSelection(_) =>
        throw new IllegalStateException("This shouldn't be called on TypeColumnSelection!")
    }
  }
}
