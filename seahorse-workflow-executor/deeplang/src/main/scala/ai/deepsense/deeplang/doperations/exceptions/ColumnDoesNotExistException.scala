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

import ai.deepsense.deeplang.doperations.exceptions.ColumnDoesNotExistException._
import ai.deepsense.deeplang.params.selections.{IndexSingleColumnSelection, NameSingleColumnSelection, SingleColumnSelection}

case class ColumnDoesNotExistException(
    selection: SingleColumnSelection,
    schema: StructType) extends DOperationExecutionException(
  exceptionMessage(selection, schema),
  None)

object ColumnDoesNotExistException {

  private def exceptionMessage(selection: SingleColumnSelection, schema: StructType): String = {
    s"Column ${selectionDescription(selection)} " +
    s"does not exist in the input DataFrame (${schemaDescription(selection, schema)})"
  }

  private def selectionDescription(selection: SingleColumnSelection): String =
    selection match {
      case NameSingleColumnSelection(name) => s"`$name`"
      case IndexSingleColumnSelection(index) => s"with index $index"
    }

  private def schemaDescription(selection: SingleColumnSelection, schema: StructType): String = {
    selection match {
      case IndexSingleColumnSelection(_) =>
        s"index range: 0..${schema.length - 1}"
      case NameSingleColumnSelection(_) =>
        s"column names: ${schema.fields.map(field => s"`${field.name}`").mkString(", ")}"
    }
  }
}
