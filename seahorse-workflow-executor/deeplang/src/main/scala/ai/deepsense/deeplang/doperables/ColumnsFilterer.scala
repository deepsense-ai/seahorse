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

package ai.deepsense.deeplang.doperables

import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import ai.deepsense.deeplang.params.selections.{MultipleColumnSelection, NameColumnSelection}
import ai.deepsense.deeplang.params.{ColumnSelectorParam, Param}

class ColumnsFilterer extends Transformer {

  val selectedColumns = ColumnSelectorParam(
    name = "selected columns",
    description = Some("Columns to be retained in the output DataFrame."),
    portIndex = 0)

  def getSelectedColumns: MultipleColumnSelection = $(selectedColumns)

  def setSelectedColumns(value: MultipleColumnSelection): this.type =
    set(selectedColumns, value)

  def setSelectedColumns(retainedColumns: Seq[String]): this.type =
    setSelectedColumns(
      MultipleColumnSelection(
        Vector(NameColumnSelection(retainedColumns.toSet)),
        excluding = false))

  override val params: Array[Param[_]] = Array(selectedColumns)

  override def applyTransform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    val columns = df.getColumnNames(getSelectedColumns)
    if (columns.isEmpty) {
      DataFrame.empty(ctx)
    } else {
      val filtered = df.sparkDataFrame.select(columns.head, columns.tail: _*)
      DataFrame.fromSparkDataFrame(filtered)
    }
  }

  override def applyTransformSchema(schema: StructType): Option[StructType] = {
    val outputColumns = DataFrameColumnsGetter.getColumnNames(schema, getSelectedColumns)
    val inferredSchema = if (outputColumns.isEmpty) {
      StructType(Seq.empty)
    } else {
      val fields = schema.filter(field => outputColumns.contains(field.name))
      StructType(fields)
    }
    Some(inferredSchema)
  }
}
