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

package io.deepsense.deeplang.doperations

import scala.reflect.runtime.{universe => ru}

import org.apache.spark.sql.types.DataType
import org.apache.spark.sql.{Column, UserDefinedFunction}

import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.types.ColumnType.ColumnType
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe._
import io.deepsense.deeplang.doperables.dataframe.types.{Conversions, SparkConversions}
import io.deepsense.deeplang.doperations.ConvertType.TargetTypeChoice
import io.deepsense.deeplang.doperations.ConvertType.TargetTypeChoice.{NumericTargetTypeChoice, StringTargetTypeChoice}
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.params.choice.{Choice, ChoiceParam}
import io.deepsense.deeplang.params.{ColumnSelectorParam, Params}

case class ConvertType()
    extends DOperation1To1[DataFrame, DataFrame]
    with Params {

  override val name: String = "Convert Type"
  override val id: Id = "f8b3c5d0-febe-11e4-b939-0800200c9a66"

  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]

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

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {
    val targetType = getTargetType.columnType
    val columns = dataFrame
      .getColumnNames(getSelectedColumns)

    logger.debug("Finding converters...")
    val converters = findConverters(dataFrame, columns, targetType)
    val columnsOldToNew = findColumnNameMapping(converters.keys, dataFrame)
    logger.debug("Executing converters and selecting converted columns...")
    val cleanedUpDf = convert(dataFrame, converters, columnsOldToNew)
    logger.debug("Building DataFrame...")
    context.dataFrameBuilder.buildDataFrame(cleanedUpDf)
  }

  private def findConverters(
      dataFrame: DataFrame,
      columns: Seq[String],
      targetType: ColumnType): Map[String, UserDefinedFunction] = {
    require(targetType != ColumnType.categorical)
    val columnDataTypes = columns
      .map(n => n -> dataFrame.sparkDataFrame.schema.apply(n).dataType)
      .toMap

    columnDataTypes.collect {
      case (columnName, sourceDataType)
        if SparkConversions.sparkColumnTypeToColumnType(sourceDataType) != targetType =>
          val converter = findConverter(dataFrame, columnName, targetType, sourceDataType)
          columnName -> converter
    }
  }

  private def findConverter(
      dataFrame: DataFrame,
      columnName: String,
      targetType: ColumnType,
      sourceDataType: DataType): UserDefinedFunction = {
    val sourceColumnType = SparkConversions.sparkColumnTypeToColumnType(sourceDataType)
      Conversions.UdfConverters(
        (SparkConversions.sparkColumnTypeToColumnType(sourceDataType), targetType))
  }

  private def findColumnNameMapping(columnsToConvert: Iterable[String], dataFrame: DataFrame) = {
    val pairs = columnsToConvert.map(old => (old, dataFrame.uniqueColumnName(old, "convert_type")))
    val oldToNew = pairs.toMap
    oldToNew
  }

  val safeConvertsTo = Seq(ColumnType.string, ColumnType.categorical)
  val safeConvertsMap = Map(
    ColumnType.numeric -> (safeConvertsTo),
    ColumnType.boolean -> (safeConvertsTo ++ Seq(ColumnType.numeric)),
    ColumnType.categorical -> Seq(ColumnType.string),
    ColumnType.string -> Seq(ColumnType.categorical),
    ColumnType.timestamp -> (safeConvertsTo ++ Seq(ColumnType.numeric))
  )

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
}

object ConvertType {

  sealed trait TargetTypeChoice extends Choice {
    val columnType: ColumnType.Value
    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[StringTargetTypeChoice],
      classOf[NumericTargetTypeChoice])
  }

  object TargetTypeChoice {
    def fromColumnType(targetType: ColumnType): TargetTypeChoice = {
      targetType match {
        case ColumnType.string => StringTargetTypeChoice()
        case ColumnType.numeric => NumericTargetTypeChoice()
      }
    }

    case class StringTargetTypeChoice() extends TargetTypeChoice {
      override val name = "string"
      override val columnType = ColumnType.string
      override val params = declareParams()
    }

    case class NumericTargetTypeChoice() extends TargetTypeChoice {
      override val name = "numeric"
      override val columnType = ColumnType.numeric
      override val params = declareParams()
    }
  }
}
