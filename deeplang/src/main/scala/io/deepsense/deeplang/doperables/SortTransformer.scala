/**
 * Copyright 2016, deepsense.io
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

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import io.deepsense.deeplang.params._
import io.deepsense.deeplang.params.selections.{IndexSingleColumnSelection, NameSingleColumnSelection, SingleColumnSelection}
import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.StructType


class SortTransformer extends Transformer {

  val columns = ParamsSequence[SortColumnParam](
    name = "Sort columns",
    description = "Columns to sort by"
  )

  def getColumns: Seq[SortColumnParam] = $(columns)
  def setColumns(sortColumnParams: Seq[SortColumnParam]): this.type =
    set(columns, sortColumnParams)

  override private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    DataFrame.fromSparkDataFrame(
      df.sparkDataFrame.sort($(columns).map(
        SortColumnParam.columnParamToColumnExpression(_, df)): _*))
  }

  override def params: Array[Param[_]] = declareParams(columns)

  override private[deeplang] def _transformSchema(schema: StructType): Option[StructType] = {
    Some(schema)
  }

}

class SortColumnParam extends Params {
  import SortColumnParam._

  val column = SingleColumnSelectorParam(
    name = columnNameParamName,
    description = "Column name",
    portIndex = 0
  )

  val descending = BooleanParam(
    name = descendingFlagParamName,
    description = "Sort in descending order when true"
  )

  setDefault(descending, false)

  def getDescending: Boolean = $(descending)
  def isDescending: Boolean = getDescending
  def setDescending(desc: Boolean): this.type = set(descending, desc)
  def getColumn: SingleColumnSelection = $(column)
  def setColumn(col: SingleColumnSelection): this.type = set(column, col)

  override def params: Array[Param[_]] = declareParams(column, descending)

}

object SortColumnParam {

  val columnNameParamName = "Column name"
  val descendingFlagParamName = "Descending flag"

  def columnParamToColumnExpression(scp: SortColumnParam, df: DataFrame): Column = {
    val column = col(DataFrameColumnsGetter.getColumnName(df.schema.get, scp.getColumn))
    if (scp.getDescending) {
      column.desc
    } else {
      column.asc
    }
  }

  def apply(columnName: String, descending: Boolean): SortColumnParam = {
    new SortColumnParam()
      .setColumn(new NameSingleColumnSelection(columnName))
      .setDescending(descending)
  }

  def apply(columnIndex: Int, descending: Boolean): SortColumnParam = {
    new SortColumnParam()
      .setColumn(new IndexSingleColumnSelection(columnIndex))
      .setDescending(descending)
  }

  def apply(columnSelection: SingleColumnSelection, descending: Boolean):
  SortColumnParam = {
    new SortColumnParam()
      .setColumn(columnSelection)
      .setDescending(descending)
  }
}
