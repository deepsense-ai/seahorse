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

import java.sql.Timestamp

import scala.collection.immutable.ListMap
import scala.reflect.runtime.{universe => ru}

import org.apache.spark.sql.types._

import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.doperables.dataframe.{CategoricalColumnMetadata, ColumnMetadata, DataFrame}
import io.deepsense.deeplang.doperations.MissingValuesHandler.{EmptyColumnsMode, Strategy}
import io.deepsense.deeplang.doperations.exceptions.{MultipleTypesReplacementException, WrongReplacementValueException}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.parameters.ChoiceParameter.BinaryChoice
import io.deepsense.deeplang.parameters.ColumnType.ColumnType
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DKnowledge, DOperation1To1, ExecutionContext}

case class MissingValuesHandler()
  extends DOperation1To1[DataFrame, DataFrame]
  with MissingValuesHandlerParams {

  override val name: String = "Missing Values Handler"
  override val id: Id = "e1120fbc-375b-4967-9c23-357ab768272f"

  override protected def _inferFullKnowledge(context: InferContext)
                                            (knowledge: DKnowledge[DataFrame])
  : ((DKnowledge[DataFrame]), InferenceWarnings) = {
    // TODO Check column selection correctness (this applies to other operations as well)
    // TODO New column metadata if "missing value indicator" is checked
    ((knowledge), InferenceWarnings.empty)
  }

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {

    val strategy = Strategy.withName(strategyParam.value.get)
    val columns = dataFrame.getColumnNames(selectedColumnsParam.value.get)
    val indicator = getIndicator()

    val indicatedDataFrame = addNullIndicatorColumns(context, dataFrame, columns, indicator)

    strategy match {
      case Strategy.REMOVE_ROW =>
        removeRowsWithEmptyValues(context, indicatedDataFrame, columns, indicator)
      case Strategy.REMOVE_COLUMN =>
        removeColumnsWithEmptyValues(context, indicatedDataFrame, columns, indicator)
      case Strategy.REPLACE_WITH_MODE =>
        replaceWithMode(context, indicatedDataFrame, columns, indicator)
      case Strategy.REPLACE_WITH_CUSTOM_VALUE =>
        replaceWithCustomValue(
          context, indicatedDataFrame, columns, customValueParameter.value.get, indicator)
    }
  }

  private def addNullIndicatorColumns(
      context: ExecutionContext,
      dataFrame: DataFrame,
      columns: Seq[String],
      indicator: Option[String]) = {

    indicator match {
      case Some(prefix) => {
        val attachedColumns = columns.map(missingValueIndicatorColumn(dataFrame, _, prefix))
        dataFrame.withColumns(context, attachedColumns)
      }
      case None => {
        dataFrame
      }
    }
  }

  private def removeRowsWithEmptyValues(
      context: ExecutionContext,
      dataFrame: DataFrame,
      columns: Seq[String],
      indicator: Option[String]) = {

    val rdd = dataFrame.sparkDataFrame.rdd.filter(
      row => !row.getValuesMap(columns).values.toList.contains(null))
    context.dataFrameBuilder.buildDataFrame(dataFrame.sparkDataFrame.schema, rdd)
  }

  private def removeColumnsWithEmptyValues(
      context: ExecutionContext,
      dataFrame: DataFrame,
      columns: Seq[String],
      indicator: Option[String]) = {

    val columnsWithNulls = columns.filter(column =>
      dataFrame.sparkDataFrame.select(column).filter(column + " is null").count() > 0)
    val retainedColumns = dataFrame.sparkDataFrame.columns filterNot columnsWithNulls.contains
    context.dataFrameBuilder.buildDataFrame(
      dataFrame.sparkDataFrame.select(retainedColumns.head, retainedColumns.tail: _*))
  }

  private def replaceWithCustomValue(
      context: ExecutionContext,
      dataFrame: DataFrame,
      columns: Seq[String],
      customValue: String,
      indicator: Option[String]) = {

    val columnMetadata = dataFrame.metadata.get.columns

    val columnTypes = Map(columns.map(columnName =>
      (columnName -> dataFrame.columnType(columnName))): _*)

    if (columnTypes.values.toSet.size != 1) {
      throw new MultipleTypesReplacementException(columnTypes)
    }

    MissingValuesHandlerUtils.replaceNulls(context, dataFrame, columns,
      columnName =>
        ReplaceWithCustomValueStrategy.convertReplacementValue(
        customValue, columnMetadata(columnName), columnTypes(columnName)))
  }

  private def replaceWithMode(
      context: ExecutionContext,
      dataFrame: DataFrame,
      columns: Seq[String],
      indicator: Option[String]) = {

    val columnModes = Map(columns.map(column =>
      (column -> calculateMode(dataFrame, column))): _*)

    val nonEmptyColumnModes = Map[String, Any](columnModes
      .filterKeys(column => columnModes(column) != None)
      .mapValues(_.get).toSeq: _*)

    val allEmptyColumns = columnModes.keys.filter(column => columnModes(column) == None)

    var resultDF = MissingValuesHandlerUtils.replaceNulls(context, dataFrame, columns,
      columnName => nonEmptyColumnModes.getOrElse(columnName, null))

    if (emptyColumnStrategyParameter.value.get ==
      MissingValuesHandler.EmptyColumnsMode.REMOVE.toString) {
      val retainedColumns = dataFrame.sparkDataFrame.columns.filter(
        !allEmptyColumns.toList.contains(_))
      resultDF = context.dataFrameBuilder.buildDataFrame(
        resultDF.sparkDataFrame.select(retainedColumns.head, retainedColumns.tail: _*))
    }

    resultDF
  }

  private def getIndicator(): Option[String] = {
    if (missingValueIndicatorParam.value.get == BinaryChoice.YES.toString) {
      indicatorPrefixParam.value
    } else {
      None
    }
  }

  private def missingValueIndicatorColumn(dataFrame: DataFrame, column: String, prefix: String) = {
    dataFrame.sparkDataFrame(column).isNull.as(prefix + column).cast(BooleanType)
  }

  private def calculateMode(dataFrame: DataFrame, column: String): Option[Any] = {

    import org.apache.spark.sql.functions.desc

    val resultArray = dataFrame.sparkDataFrame
      .select(column)
      .filter(column + " is not null")
      .groupBy(column)
      .count()
      .orderBy(desc("count"))
      .limit(1)
      .collect()

    if (resultArray.isEmpty) {
      None
    } else {
      Some(resultArray(0)(0))
    }
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

object MissingValuesHandler {
  class Strategy extends Enumeration {
    type Strategy = Value
    val REMOVE_ROW = Value("remove row")
    val REMOVE_COLUMN = Value("remove column")
    val REPLACE_WITH_CUSTOM_VALUE = Value("replace with custom value")
    val REPLACE_WITH_MODE = Value("replace with mode")
  }

  object Strategy extends Strategy

  class EmptyColumnsMode extends Enumeration {
    type EmptyColumnsMode = Value
    val REMOVE = Value("remove")
    val RETAIN = Value("retain")
  }

  object EmptyColumnsMode extends EmptyColumnsMode

  def apply(columns: MultipleColumnSelection,
            strategy: Strategy.Value,
            indicator: BinaryChoice.Value = BinaryChoice.NO,
            indicatorPrefix: Option[String] = None): MissingValuesHandler = {

    val handler = MissingValuesHandler()
    handler.selectedColumnsParam.value = Some(columns)
    handler.strategyParam.value = Some(strategy.toString)
    handler.missingValueIndicatorParam.value = Some(indicator.toString)
    handler.indicatorPrefixParam.value = indicatorPrefix
    handler
  }

  def replaceWithCustomValue(
      columns: MultipleColumnSelection,
      customValue: String,
      indicator: BinaryChoice.Value = BinaryChoice.NO,
      indicatorPrefix: Option[String] = None): MissingValuesHandler = {

    val handler = MissingValuesHandler(
      columns,
      Strategy.REPLACE_WITH_CUSTOM_VALUE,
      indicator,
      indicatorPrefix)
    handler.customValueParameter.value = Some(customValue)
    handler
  }

  def replaceWithMode(
      columns: MultipleColumnSelection,
      emptyColumnsMode: EmptyColumnsMode.Value,
      indicator: BinaryChoice.Value = BinaryChoice.NO,
      indicatorPrefix: Option[String] = None): MissingValuesHandler = {

    val handler = MissingValuesHandler(
      columns,
      Strategy.REPLACE_WITH_MODE,
      indicator,
      indicatorPrefix)
    handler.emptyColumnStrategyParameter.value = Some(emptyColumnsMode.toString)
    handler
  }
}

private object MissingValuesHandlerUtils {

  import org.apache.spark.sql.functions.when

  def replaceNulls(
      context: ExecutionContext,
      dataFrame: DataFrame,
      chosenColumns: Seq[String],
      replaceFunction: String => Any): DataFrame = {

    val df = dataFrame.sparkDataFrame

    val resultSparkDF = df.select(
      df.columns.toSeq.map(columnName => {
        if (chosenColumns.contains(columnName)) {
          when(df(columnName).isNull,
            replaceFunction(columnName))
            .otherwise(df(columnName)).as(columnName)
        } else {
          df(columnName)
        }
      }): _*)

    context.dataFrameBuilder.buildDataFrame(resultSparkDF)
  }

}

private object ReplaceWithCustomValueStrategy {

  def convertReplacementValue(
      rawValue: String, colMetadata: ColumnMetadata, colType: ColumnType): Any = {

    try {
      colType match {
        case ColumnType.numeric => rawValue.toDouble
        case ColumnType.boolean => rawValue.toBoolean
        case ColumnType.string => rawValue
        case ColumnType.timestamp => Timestamp.valueOf(rawValue)
        case ColumnType.categorical =>
          colMetadata.asInstanceOf[CategoricalColumnMetadata].categories.get.valueToId(rawValue)
      }
    } catch {
      case e: Exception =>
        throw new WrongReplacementValueException(rawValue, colMetadata.name, colType)
    }
  }

}

trait MissingValuesHandlerParams {
  val selectedColumnsParam = ColumnSelectorParameter(
    "Columns containing missing values to handle", required = true, portIndex = 0)

  val defaultStrategy = Strategy.REMOVE_ROW

  val customValueParameter = StringParameter(
    description = "Replacement for missing values",
    default = None,
    required = true,
    validator = new AcceptAllRegexValidator()
  )

  val emptyColumnStrategyParameter = ChoiceParameter(
    "Strategy of handling columns with missing all values",
    default = Some(EmptyColumnsMode.REMOVE.toString),
    required = true,
    options = ListMap(
      MissingValuesHandler.EmptyColumnsMode.REMOVE.toString -> ParametersSchema(),
      MissingValuesHandler.EmptyColumnsMode.RETAIN.toString -> ParametersSchema()
    )
  )

  val strategyParam = ChoiceParameter(
    "Strategy of handling missing values",
    default = Some(defaultStrategy.toString),
    required = true,
    options = ListMap(
      MissingValuesHandler.Strategy.REMOVE_ROW.toString -> ParametersSchema(),
      MissingValuesHandler.Strategy.REMOVE_COLUMN.toString -> ParametersSchema(),
      MissingValuesHandler.Strategy.REPLACE_WITH_CUSTOM_VALUE.toString -> ParametersSchema(
        "value" -> customValueParameter
      ),
      MissingValuesHandler.Strategy.REPLACE_WITH_MODE.toString -> ParametersSchema(
        "empty column strategy" -> emptyColumnStrategyParameter
      )
    )
  )

  val indicatorPrefixParam = PrefixBasedColumnCreatorParameter(
    "Prefix for columns indicating presence of missing values",
    default = None,
    required = true
  )

  val missingValueIndicatorParam = ChoiceParameter.binaryChoice(
    description = "Generate missing value indicator column",
    default = Some(BinaryChoice.NO.toString),
    required = true,
    yesSchema = ParametersSchema(
      "indicator column" -> indicatorPrefixParam
    ),
    noSchema = ParametersSchema()
  )

  val parameters: ParametersSchema = ParametersSchema(
    "columns" -> selectedColumnsParam,
    "strategy" -> strategyParam,
    "missing value indicator" -> missingValueIndicatorParam
  )
}
