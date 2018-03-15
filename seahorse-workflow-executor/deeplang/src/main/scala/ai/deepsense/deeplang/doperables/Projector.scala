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

import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.Projector.ColumnProjection
import ai.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import ai.deepsense.deeplang.params.selections.SingleColumnSelection
import ai.deepsense.deeplang.params._
import ai.deepsense.deeplang.params.choice.{Choice, ChoiceParam}
import ai.deepsense.deeplang.utils.SparkUtils

class Projector extends Transformer {

  val projectionColumns = ParamsSequence[ColumnProjection](
    name = "projection columns",
    description = Some("Column to project in the output DataFrame."))

  def getProjectionColumns: Seq[ColumnProjection] = $(projectionColumns)
  def setProjectionColumns(value: Seq[ColumnProjection]): this.type = set(projectionColumns, value)

  override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array(projectionColumns)

  override def applyTransform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    val exprSeq = getProjectionColumns.map { cp =>
      val renameExpressionPart = cp.getRenameColumn.getColumnName match {
        case None => ""
        case Some(columnName) => s" AS ${SparkUtils.escapeColumnName(columnName)}"
      }
      SparkUtils.escapeColumnName(df.getColumnName(cp.getOriginalColumn)) + renameExpressionPart
    }
    if (exprSeq.isEmpty) {
      DataFrame.empty(ctx)
    } else {
      val filtered = df.sparkDataFrame.selectExpr(exprSeq: _*)
      DataFrame.fromSparkDataFrame(filtered)
    }
  }

  override def applyTransformSchema(schema: StructType): Option[StructType] = {
    val namesPairsSeq = getProjectionColumns.map { cp =>
      val originalColumnName = DataFrameColumnsGetter.getColumnName(schema, cp.getOriginalColumn)
      val resultColumnName = cp.getRenameColumn.getColumnName match {
        case None => originalColumnName
        case Some(columnName) => columnName
      }
      (originalColumnName, resultColumnName)
    }
    val fields = namesPairsSeq.map { case (originalColumnName: String, renamedColumnName: String) =>
      schema(originalColumnName).copy(name = renamedColumnName)
    }
    Some(StructType(fields))
  }
}

object Projector {
  val OriginalColumnParameterName = "original column"
  val RenameColumnParameterName = "rename column"
  val ColumnNameParameterName = "column name"


  case class ColumnProjection() extends Params {

    val originalColumn = SingleColumnSelectorParam(
      name = OriginalColumnParameterName,
      description = Some("Column from the input DataFrame."),
      portIndex = 0)

    def getOriginalColumn: SingleColumnSelection = $(originalColumn)
    def setOriginalColumn(value: SingleColumnSelection): this.type = set(originalColumn, value)

    val renameColumn = ChoiceParam[RenameColumnChoice](
      name = RenameColumnParameterName,
      description = Some("Determine if the column should be renamed."))
    setDefault(renameColumn, RenameColumnChoice.No())

    def getRenameColumn: RenameColumnChoice = $(renameColumn)
    def setRenameColumn(value: RenameColumnChoice): this.type = set(renameColumn, value)

    val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array(originalColumn, renameColumn)
  }


  sealed trait RenameColumnChoice extends Choice {
    import RenameColumnChoice._

    def getColumnName: Option[String]
    override val choiceOrder: List[Class[_ <: RenameColumnChoice]] =
      List(
        classOf[No],
        classOf[Yes])
  }

  object RenameColumnChoice {
    case class Yes() extends RenameColumnChoice {

      override val name: String = "Yes"

      val columnName = SingleColumnCreatorParam(
        name = ColumnNameParameterName,
        description = Some("New name for a column in the output DataFrame.")
      )
      setDefault(columnName, "")

      override def getColumnName: Option[String] = Some($(columnName))
      def setColumnName(value: String): this.type = set(columnName, value)

      override val params: Array[Param[_]] = Array(columnName)
    }
    case class No() extends RenameColumnChoice {
      override val name: String = "No"

      override def getColumnName: Option[String] = None

      override val params: Array[Param[_]] = Array()
    }
  }

}
