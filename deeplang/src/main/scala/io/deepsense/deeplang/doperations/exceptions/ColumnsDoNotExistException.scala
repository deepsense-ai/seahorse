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

package io.deepsense.deeplang.doperations.exceptions

import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.doperables.dataframe.{SchemaPrintingUtils, DataFrame}
import io.deepsense.deeplang.params.selections.ColumnSelection

case class ColumnsDoNotExistException(
    invalidSelections: Vector[ColumnSelection],
    schema: StructType)
  extends DOperationExecutionException(
    s"One or more columns from specified selections: $invalidSelections " +
      s"does not exist in ${SchemaPrintingUtils.structTypeToString(schema)}",
    None)

object ColumnsDoNotExistException {
  def apply(
      invalidSelections: Vector[ColumnSelection],
      dataFrame: DataFrame): ColumnsDoNotExistException =
    ColumnsDoNotExistException(invalidSelections, dataFrame.sparkDataFrame.schema)
}
