/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import ai.deepsense.deeplang.params._
import ai.deepsense.deeplang.params.selections.{IndexSingleColumnSelection, NameSingleColumnSelection, SingleColumnSelection}

/**
  * Sorts the input [[ai.deepsense.deeplang.doperables.dataframe.DataFrame Dataframe]]
  * according to selected columns.
  */
class SortTransformer extends Transformer {

  val columns = ParamsSequence[SortColumnParam](
    name = "sort columns",
    description = Some("Columns that will be used to sort the DataFrame.")
  )

  def getColumns: Seq[SortColumnParam] = $(columns)
  def setColumns(sortColumnParams: Seq[SortColumnParam]): this.type =
    set(columns, sortColumnParams)

  override protected def applyTransform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    getColumns match {
      case Nil => df  // Sort in Spark 2.0 is no-op for empty columns, but in 1.6 it throws. Here we always do no-op.
      case selectedColumns =>
        DataFrame.fromSparkDataFrame(
          df.sparkDataFrame.sort($(columns).map(
            SortColumnParam.columnParamToColumnExpression(_, df)): _*))
    }
  }

  override def params: Array[Param[_]] = Array(columns)

  override protected def applyTransformSchema(schema: StructType): Option[StructType] = {
    // Check that all columns selected for sorting exist
    getSelectedSortColumnNames(schema, _.getColumn)
    Some(schema)
  }

  private def getSelectedSortColumnNames(
      schema: StructType,
      selector: SortColumnParam => SingleColumnSelection): Seq[String] = {
    getColumns.map(columnPair =>
      DataFrameColumnsGetter.getColumnName(schema, selector(columnPair)))
  }
}

class SortColumnParam extends Params {
  import SortColumnParam._

  val column = SingleColumnSelectorParam(
    name = columnNameParamName,
    description = None,
    portIndex = 0
  )

  val descending = BooleanParam(
    name = descendingFlagParamName,
    description = Some("Should sort in descending order?")
  )

  setDefault(descending, false)

  def getDescending: Boolean = $(descending)
  def isDescending: Boolean = getDescending
  def setDescending(desc: Boolean): this.type = set(descending, desc)
  def getColumn: SingleColumnSelection = $(column)
  def setColumn(col: SingleColumnSelection): this.type = set(column, col)

  override def params: Array[Param[_]] = Array(column, descending)

}

object SortColumnParam {

  val columnNameParamName = "column name"
  val descendingFlagParamName = "descending"

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
