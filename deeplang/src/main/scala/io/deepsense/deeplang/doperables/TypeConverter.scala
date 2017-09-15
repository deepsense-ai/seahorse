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

package io.deepsense.deeplang.doperables

import org.apache.spark.sql.types._

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.TypeConverter.TargetTypeChoice
import io.deepsense.deeplang.doperables.dataframe._
import io.deepsense.deeplang.params.ColumnSelectorParam
import io.deepsense.deeplang.params.choice.{Choice, ChoiceParam}
import io.deepsense.deeplang.params.selections.MultipleColumnSelection

case class TypeConverter() extends Transformer {

  val selectedColumns = ColumnSelectorParam(
    name = "selected columns",
    description = "Columns to be converted",
    portIndex = 0)

  def getSelectedColumns: MultipleColumnSelection = $(selectedColumns)
  def setSelectedColumns(value: MultipleColumnSelection): this.type = set(selectedColumns, value)

  val targetType = ChoiceParam[TargetTypeChoice](
    name = "target type",
    description = "Target type of the columns")

  def getTargetType: TargetTypeChoice = $(targetType)
  def setTargetType(value: TargetTypeChoice): this.type = set(targetType, value)

  val params = declareParams(selectedColumns, targetType)

  override def _transform(context: ExecutionContext, dataFrame: DataFrame): DataFrame = {
    val targetType = getTargetType.columnType
    val columnsToConvert = dataFrame.getColumnNames(getSelectedColumns)
    val allColumns = dataFrame.sparkDataFrame.schema.map(_.name)

    val transformingExpression =
      buildTransformingExpression(allColumns, columnsToConvert.toSet, targetType)

    val sparkDataFrame = dataFrame.sparkDataFrame.selectExpr(transformingExpression: _*)

    DataFrame.fromSparkDataFrame(sparkDataFrame)
  }

  override def _transformSchema(schema: StructType): Option[StructType] = {
    val convertedColumns = DataFrameColumnsGetter.getColumnNames(schema, getSelectedColumns)
    val convertedFields = schema.map { field =>
      if (convertedColumns.contains(field.name)) {
        field.copy(dataType = getTargetType.columnType)
      } else {
        field
      }
    }
    Some(StructType(convertedFields))
  }

  private def buildTransformingExpression(
    allColumns: Seq[String],
    columnsToConvert: Set[String],
    targetType: DataType): Seq[String] = {
    val typeName = targetType.typeName

    allColumns.map { column =>
      if (columnsToConvert.contains(column)) {
        s"cast(`$column` as $typeName) as `$column`"
      } else {
        s"`$column`"
      }
    }
  }

  override def report(executionContext: ExecutionContext): Report = Report()
}

object TypeConverter {

  sealed abstract class TargetTypeChoice(val columnType: DataType) extends Choice {
    override val choiceOrder: List[Class[_ <: Choice]] = TargetTypeChoices.choiceOrder

    override val params = declareParams()
    val name = columnType.simpleString
  }

  object TargetTypeChoices {
    val choiceOrder = List(
      StringTargetTypeChoice(),
      BooleanTargetTypeChoice(),
      TimestampTargetTypeChoice(),
      DoubleTargetTypeChoice(),
      FloatTargetTypeChoice(),
      LongTargetTypeChoice(),
      IntegerTargetTypeChoice()).map(_.getClass)

    case class StringTargetTypeChoice() extends TargetTypeChoice(StringType)
    case class DoubleTargetTypeChoice() extends TargetTypeChoice(DoubleType)
    case class TimestampTargetTypeChoice() extends TargetTypeChoice(TimestampType)
    case class BooleanTargetTypeChoice() extends TargetTypeChoice(BooleanType)
    case class IntegerTargetTypeChoice() extends TargetTypeChoice(IntegerType)
    case class FloatTargetTypeChoice() extends TargetTypeChoice(FloatType)
    case class LongTargetTypeChoice() extends TargetTypeChoice(LongType)
  }
}
