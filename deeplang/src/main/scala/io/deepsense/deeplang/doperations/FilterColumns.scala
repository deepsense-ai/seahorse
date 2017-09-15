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

/** TODO as Transformer */
package io.deepsense.deeplang.doperations

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.params.{Param, ColumnSelectorParam, Params}
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}

case class FilterColumns() extends DOperation1To1[DataFrame, DataFrame] with Params {

  override val name: String = "Filter Columns"
  override val id: Id = "96b28b7f-d54c-40d7-9076-839411793d20"

  val selectedColumns = ColumnSelectorParam(
    name = "selected columns",
    description = "Columns to be retained in the output DataFrame",
    portIndex = 0)

  def getSelectedColumns: MultipleColumnSelection = $(selectedColumns)

  def setSelectedColumns(value: MultipleColumnSelection): this.type =
    set(selectedColumns, value)

  def setSelectedColumns(retainedColumns: Seq[String]): this.type =
    setSelectedColumns(
      MultipleColumnSelection(
        Vector(NameColumnSelection(retainedColumns.toSet)),
        excluding = false))

  val params = declareParams(selectedColumns)

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {
    val columns = dataFrame.getColumnNames(getSelectedColumns)
    if (columns.nonEmpty) {
      val filtered = dataFrame.sparkDataFrame.select(columns.head, columns.tail: _*)
      context.dataFrameBuilder.buildDataFrame(filtered)
    } else {
      DataFrame.empty(context)
    }
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}
