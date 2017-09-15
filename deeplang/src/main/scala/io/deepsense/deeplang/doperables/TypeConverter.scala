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

import org.apache.spark.sql.types.{DataType, DoubleType, StringType, StructType}
import org.apache.spark.sql.{Column, UserDefinedFunction}

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.TypeConverter.TargetTypeChoice
import io.deepsense.deeplang.doperables.dataframe._
import io.deepsense.deeplang.doperables.dataframe.types.Conversions
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
    val columns = dataFrame.getColumnNames(getSelectedColumns)

    logger.debug("Finding converters...")
    val converters = findConverters(dataFrame, columns, targetType)
    val columnsOldToNew = findColumnNameMapping(converters.keys, dataFrame)
    logger.debug("Executing converters and selecting converted columns...")
    val cleanedUpDf = convert(dataFrame, converters, columnsOldToNew)
    logger.debug("Building DataFrame...")
    DataFrame.fromSparkDataFrame(cleanedUpDf)
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

  private def findConverters(
      dataFrame: DataFrame,
      columns: Seq[String],
      targetDataType: DataType): Map[String, UserDefinedFunction] = {
    val columnDataTypes = columns
      .map(n => n -> dataFrame.sparkDataFrame.schema.apply(n).dataType)
      .toMap

    columnDataTypes.collect {
      case (columnName, sourceDataType)
        if sourceDataType != targetDataType =>
          columnName -> Conversions.UdfConverters((sourceDataType, targetDataType))
    }
  }

  private def findColumnNameMapping(columnsToConvert: Iterable[String], dataFrame: DataFrame) = {
    val pairs = columnsToConvert.map(old => (old, dataFrame.uniqueColumnName(old, "convert_type")))
    val oldToNew = pairs.toMap
    oldToNew
  }

  private def convert(
      dataFrame: DataFrame,
      converters: Map[String, UserDefinedFunction],
      columnsOldToNew: Map[String, String]) = {
    val convertedColumns: Seq[Column] = converters.toSeq.map {
      case (columnName: String, converter: UserDefinedFunction) =>
        val column = converter(dataFrame.sparkDataFrame(columnName))
        val alias = columnsOldToNew(columnName)
        column.as(alias)
    }
    val dfWithConvertedColumns =
      dataFrame.sparkDataFrame.select(new Column("*") +: convertedColumns: _*)
    val correctColumns: Seq[Column] = dataFrame.sparkDataFrame.columns.toSeq.map(name => {
      if (columnsOldToNew.contains(name)) {
        new Column(columnsOldToNew(name)).as(name)
      } else {
        new Column(name)
      }
    })
    dfWithConvertedColumns.select(correctColumns: _*)
  }

  override def report(executionContext: ExecutionContext): Report = Report()
}

object TypeConverter {

  sealed trait TargetTypeChoice extends Choice {
    import TargetTypeChoice._

    val columnType: DataType
    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[StringTargetTypeChoice],
      classOf[NumericTargetTypeChoice])
  }

  object TargetTypeChoice {
    def fromColumnType(targetType: DataType): TargetTypeChoice = {
      targetType match {
        case StringType => StringTargetTypeChoice()
        case DoubleType => NumericTargetTypeChoice()
      }
    }

    case class StringTargetTypeChoice() extends TargetTypeChoice {
      override val name = "string"
      override val columnType = StringType
      override val params = declareParams()
    }

    case class NumericTargetTypeChoice() extends TargetTypeChoice {
      override val name = "numeric"
      override val columnType = DoubleType
      override val params = declareParams()
    }
  }
}
