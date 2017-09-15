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

import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.types.ColumnType.ColumnType
import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.doperables.dataframe.{CategoricalColumnMetadata, ColumnMetadata, DataFrame}
import io.deepsense.deeplang.doperations.MissingValuesHandler.{EmptyColumnsStrategy, Strategy}
import io.deepsense.deeplang.doperations.exceptions.{MultipleTypesReplacementException, WrongReplacementValueException}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.parameters.ChoiceParameter.BinaryChoice
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.params.choice.{Choice, ChoiceParam}
import io.deepsense.deeplang.params._
import io.deepsense.deeplang.{DKnowledge, DOperation1To1, ExecutionContext}

case class MissingValuesHandler()
  extends DOperation1To1[DataFrame, DataFrame]
  with Params {

  import MissingValuesHandler._

  override val name: String = "Missing Values Handler"
  override val id: Id = "e1120fbc-375b-4967-9c23-357ab768272f"

  override protected def _inferFullKnowledge(context: InferContext)
                                            (knowledge: DKnowledge[DataFrame])
  : ((DKnowledge[DataFrame]), InferenceWarnings) = {
    // TODO Check column selection correctness (this applies to other operations as well)
    // TODO New column metadata if "missing value indicator" is checked
    ((knowledge), InferenceWarnings.empty)
  }

  val selectedColumns = ColumnSelectorParam(
    name = "columns",
    description = "Columns containing missing values to handle",
    portIndex = 0)

  def getSelectedColumns: MultipleColumnSelection = $(selectedColumns)
  def setSelectedColumns(value: MultipleColumnSelection): this.type = set(selectedColumns, value)

  val strategy = ChoiceParam[Strategy](
    name = "strategy",
    description = "Strategy of handling missing values")
  setDefault(strategy, Strategy.RemoveRow())

  def getStrategy: Strategy = $(strategy)
  def setStrategy(value: Strategy): this.type = set(strategy, value)

  val missingValueIndicator = ChoiceParam[MissingValueIndicatorChoice](
    name = "missing value indicator",
    description = "Generate missing value indicator column")
  setDefault(missingValueIndicator, MissingValueIndicatorChoice.No())

  def getMissingValueIndicator: MissingValueIndicatorChoice = $(missingValueIndicator)
  def setMissingValueIndicator(value: MissingValueIndicatorChoice): this.type =
    set(missingValueIndicator, value)

  val params = declareParams(selectedColumns, strategy, missingValueIndicator)

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {

    val strategy = getStrategy
    val columns = dataFrame.getColumnNames(getSelectedColumns)
    val indicator = getMissingValueIndicator.getIndicatorPrefix

    val indicatedDataFrame = addNullIndicatorColumns(context, dataFrame, columns, indicator)

    strategy match {
      case Strategy.RemoveRow() =>
        removeRowsWithEmptyValues(context, indicatedDataFrame, columns, indicator)
      case Strategy.RemoveColumn() =>
        removeColumnsWithEmptyValues(context, indicatedDataFrame, columns, indicator)
      case (replaceWithModeStrategy: Strategy.ReplaceWithMode) =>
        replaceWithMode(
          context,
          indicatedDataFrame,
          columns,
          replaceWithModeStrategy.getEmptyColumnStrategy,
          indicator)
      case (customValueStrategy: Strategy.ReplaceWithCustomValue) =>
        replaceWithCustomValue(
          context,
          indicatedDataFrame,
          columns,
          customValueStrategy.getCustomValue,
          indicator)
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
      columnName -> dataFrame.columnType(columnName)): _*)

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
      emptyColumnStrategy: EmptyColumnsStrategy,
      indicator: Option[String]) = {

    val columnModes = Map(columns.map(column =>
      column -> calculateMode(dataFrame, column)): _*)

    val nonEmptyColumnModes = Map[String, Any](columnModes
      .filterKeys(column => columnModes(column).isDefined)
      .mapValues(_.get).toSeq: _*)

    val allEmptyColumns = columnModes.keys.filter(column => columnModes(column).isEmpty)

    var resultDF = MissingValuesHandlerUtils.replaceNulls(context, dataFrame, columns,
      columnName => nonEmptyColumnModes.getOrElse(columnName, null))

    if (emptyColumnStrategy == EmptyColumnsStrategy.RemoveEmptyColumns()) {
      val retainedColumns = dataFrame.sparkDataFrame.columns.filter(
        !allEmptyColumns.toList.contains(_))
      resultDF = context.dataFrameBuilder.buildDataFrame(
        resultDF.sparkDataFrame.select(retainedColumns.head, retainedColumns.tail: _*))
    }

    resultDF
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

  sealed trait Strategy extends Choice {
    import Strategy._

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[RemoveRow],
      classOf[RemoveColumn],
      classOf[ReplaceWithCustomValue],
      classOf[ReplaceWithMode])
  }

  object Strategy {
    case class RemoveRow() extends Strategy {
      override val name: String = "remove row"
      override val params: Array[Param[_]] = declareParams()
    }

    case class RemoveColumn() extends Strategy {
      override val name: String = "remove column"
      override val params: Array[Param[_]] = declareParams()
    }

    case class ReplaceWithCustomValue() extends Strategy {

      override val name: String = "replace with custom value"
      val customValue = StringParam(
        name = "value",
        description = "Replacement for missing values")

      def getCustomValue: String = $(customValue)
      def setCustomValue(value: String): this.type = set(customValue, value)

      override val params: Array[Param[_]] = declareParams(customValue)
    }

    case class ReplaceWithMode() extends Strategy {

      override val name: String = "replace with mode"

      val emptyColumnStrategy = ChoiceParam[EmptyColumnsStrategy](
        name = "empty column strategy",
        description = "Strategy of handling columns with missing all values")
      setDefault(emptyColumnStrategy, EmptyColumnsStrategy.RemoveEmptyColumns())

      def getEmptyColumnStrategy: EmptyColumnsStrategy = $(emptyColumnStrategy)
      def setEmptyColumnStrategy(value: EmptyColumnsStrategy): this.type =
        set(emptyColumnStrategy, value)

      override val params: Array[Param[_]] = declareParams(emptyColumnStrategy)
    }
  }

  sealed trait EmptyColumnsStrategy extends Choice {
    import EmptyColumnsStrategy._

    override val choiceOrder: List[Class[_ <: EmptyColumnsStrategy]] = List(
      classOf[RemoveEmptyColumns],
      classOf[RetainEmptyColumns])
  }

  object EmptyColumnsStrategy {
    case class RemoveEmptyColumns() extends EmptyColumnsStrategy {
      override val name: String = "remove"
      override val params: Array[Param[_]] = declareParams()
    }
    case class RetainEmptyColumns() extends EmptyColumnsStrategy {
      override val name: String = "retain"
      override val params: Array[Param[_]] = declareParams()
    }
  }

  sealed trait MissingValueIndicatorChoice extends Choice {
    import MissingValueIndicatorChoice._

    def getIndicatorPrefix: Option[String]
    override val choiceOrder: List[Class[_ <: MissingValueIndicatorChoice]] =
      List(
        classOf[Yes],
        classOf[No])
  }

  object MissingValueIndicatorChoice {
    case class Yes() extends MissingValueIndicatorChoice {

      override val name: String = "Yes"

      val indicatorPrefix = PrefixBasedColumnCreatorParam(
        name = "indicator column prefix",
        description = "Prefix for columns indicating presence of missing values"
      )
      setDefault(indicatorPrefix, "")

      override def getIndicatorPrefix: Option[String] = Some($(indicatorPrefix))
      def setIndicatorPrefix(value: String): this.type = set(indicatorPrefix, value)

      override val params: Array[Param[_]] = declareParams(indicatorPrefix)
    }
    case class No() extends MissingValueIndicatorChoice {
      override val name: String = "No"

      override def getIndicatorPrefix: Option[String] = None

      override val params: Array[Param[_]] = declareParams()
    }
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

    context.dataFrameBuilder.buildDataFrame(df.schema, resultSparkDF.rdd)
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
