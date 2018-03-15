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

import java.sql.Timestamp

import scala.util.Try

import org.apache.spark.sql
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types._

import ai.deepsense.commons.types.SparkConversions
import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import ai.deepsense.deeplang.doperations.exceptions.{MultipleTypesReplacementException, ValueConversionException}
import ai.deepsense.deeplang.params._
import ai.deepsense.deeplang.params.choice.{Choice, ChoiceParam}
import ai.deepsense.deeplang.params.selections.MultipleColumnSelection
import ai.deepsense.deeplang.params.validators.AcceptAllRegexValidator

case class MissingValuesHandler()
  extends Transformer {

  import ai.deepsense.deeplang.doperables.MissingValuesHandler._

  override def applyTransformSchema(schema: StructType): Option[StructType] = {
    getStrategy match {
      case Strategy.RemoveColumn() => None
      case _ =>
        val indicator = getMissingValueIndicator.getIndicatorPrefix
        indicator match {
          case Some(prefix) =>
            val columnNames = DataFrameColumnsGetter.getColumnNames(schema, getSelectedColumns)
            val newColumns = columnNames.map(s =>
              StructField(prefix + s, BooleanType, nullable = false))
            val inferredSchema = StructType(schema.fields ++ newColumns)
            Some(inferredSchema)
          case None => Some(schema)
        }
    }
  }

  val selectedColumns = ColumnSelectorParam(
    name = "columns",
    description = Some("Columns containing missing values to handle."),
    portIndex = 0)

  def getSelectedColumns: MultipleColumnSelection = $(selectedColumns)
  def setSelectedColumns(value: MultipleColumnSelection): this.type = set(selectedColumns, value)

  val strategy = ChoiceParam[Strategy](
    name = "strategy",
    description = Some("Strategy of handling missing values."))
  setDefault(strategy, Strategy.RemoveRow())

  def getStrategy: Strategy = $(strategy)
  def setStrategy(value: Strategy): this.type = set(strategy, value)

  val userDefinedMissingValues = ParamsSequence[UserDefinedMissingValue](
    name = "user-defined missing values",
    description = Some("Sequence of values to be considered as missing."))

  def getUserDefinedMissingValues: Seq[String] = $(userDefinedMissingValues).map(_.getMissingValue)
  def setUserDefinedMissingValues(value: Seq[String]): this.type =
    set(userDefinedMissingValues,
      value.map(UserDefinedMissingValue().setMissingValue(_)))

  setDefault(
    userDefinedMissingValues,
    Seq(
      UserDefinedMissingValue().setMissingValue("NA"),
      UserDefinedMissingValue().setMissingValue("NaN")))


  val missingValueIndicator = ChoiceParam[MissingValueIndicatorChoice](
    name = "missing value indicator",
    description = Some("Generate missing value indicator column."))
  setDefault(missingValueIndicator, MissingValueIndicatorChoice.No())

  def getMissingValueIndicator: MissingValueIndicatorChoice = $(missingValueIndicator)
  def setMissingValueIndicator(value: MissingValueIndicatorChoice): this.type =
    set(missingValueIndicator, value)

  override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array(
    selectedColumns,
    strategy,
    missingValueIndicator,
    userDefinedMissingValues)

  override def applyTransform(context: ExecutionContext, dataFrame: DataFrame): DataFrame = {

    val strategy = getStrategy
    val columns = dataFrame.getColumnNames(getSelectedColumns)
    val indicator = getMissingValueIndicator.getIndicatorPrefix

    val declaredAsMissingValues = $(userDefinedMissingValues).map(_.getMissingValue)

    val indicatedDataFrame = addMissingIndicatorColumns(
      context, dataFrame, declaredAsMissingValues, columns, indicator)

    strategy match {
      case Strategy.RemoveRow() =>
        removeRowsWithEmptyValues(
          context,
          indicatedDataFrame,
          declaredAsMissingValues,
          columns,
          indicator)
      case Strategy.RemoveColumn() =>
        removeColumnsWithEmptyValues(
          context,
          indicatedDataFrame,
          declaredAsMissingValues,
          columns,
          indicator)
      case (replaceWithModeStrategy: Strategy.ReplaceWithMode) =>
        replaceWithMode(
          context,
          indicatedDataFrame,
          declaredAsMissingValues,
          columns,
          replaceWithModeStrategy.getEmptyColumnStrategy,
          indicator)
      case (customValueStrategy: Strategy.ReplaceWithCustomValue) =>
        replaceWithCustomValue(
          context,
          indicatedDataFrame,
          declaredAsMissingValues,
          columns,
          customValueStrategy.getCustomValue,
          indicator)
    }
  }

  private def addMissingIndicatorColumns(
      context: ExecutionContext,
      dataFrame: DataFrame,
      declaredAsMissingValues: Seq[String],
      columns: Seq[String],
      indicator: Option[String]) = {

    indicator match {
      case Some(prefix) =>
        val attachedColumns = columns.map(
          missingValueIndicatorColumn(dataFrame, declaredAsMissingValues, _, prefix))
        dataFrame.withColumns(context, attachedColumns)
      case None =>
        dataFrame
    }
  }

  private def removeRowsWithEmptyValues(
      context: ExecutionContext,
      dataFrame: DataFrame,
      declaredAsMissingValues: Seq[String],
      columns: Seq[String],
      indicator: Option[String]) = {

    val df = dataFrame.sparkDataFrame

    val resultDF =
      df.filter(!CommonQueries.isMissingInRowPredicate(df, columns, declaredAsMissingValues))
    DataFrame(resultDF, df.schema)
  }

  private def removeColumnsWithEmptyValues(
      context: ExecutionContext,
      dataFrame: DataFrame,
      declaredAsMissingValues: Seq[String],
      columns: Seq[String],
      indicator: Option[String]) = {

    val df = dataFrame.sparkDataFrame

    val columnsWithMissings = columns.filter {
      columnName =>
        df.select(columnName)
          .filter(
            CommonQueries.isMissingInColumnPredicate(df, columnName, declaredAsMissingValues))
          .count() > 0
    }
    val retainedColumns = df.columns filterNot columnsWithMissings.contains
    DataFrame.fromSparkDataFrame(
      df.select(retainedColumns.head, retainedColumns.tail: _*))
  }

  private def replaceWithCustomValue(
      context: ExecutionContext,
      dataFrame: DataFrame,
      declaredAsMissingValues: Seq[String],
      columns: Seq[String],
      customValue: String,
      indicator: Option[String]) = {

    val columnTypes = columns.map(columnName =>
      columnName -> SparkConversions
        .sparkColumnTypeToColumnType(dataFrame.schema.get(columnName).dataType)).toMap

    if (columnTypes.values.toSet.size != 1) {
      throw MultipleTypesReplacementException(columnTypes)
    }

    MissingValuesHandlerUtils.replaceMissings(
      context,
      dataFrame,
      declaredAsMissingValues,
      columns,
      { case columnName =>
          TypeMapper.convertRawValue(dataFrame.schema.get(columnName), customValue)
      })
  }

  private def replaceWithMode(
      context: ExecutionContext,
      dataFrame: DataFrame,
      declaredAsMissingValues: Seq[String],
      columns: Seq[String],
      emptyColumnStrategy: EmptyColumnsStrategy,
      indicator: Option[String]) = {

    val columnModes = columns.map(column =>
      column -> calculateMode(dataFrame, column, declaredAsMissingValues)).toMap

    val nonEmptyColumnModes =
      for {
        (column, modeOpt) <- columnModes
        mode <- modeOpt
      } yield (column, mode)

    val allEmptyColumns = columnModes -- nonEmptyColumnModes.keySet

    var resultDF = MissingValuesHandlerUtils.replaceMissings(
      context,
      dataFrame,
      declaredAsMissingValues,
      columns,
      nonEmptyColumnModes)

    if (emptyColumnStrategy == EmptyColumnsStrategy.RemoveEmptyColumns()) {
      val retainedColumns = dataFrame.sparkDataFrame.columns.filterNot(allEmptyColumns.contains)
      resultDF =
        DataFrame.fromSparkDataFrame(
          resultDF.sparkDataFrame.select(retainedColumns.map(col): _*)
        )
    }

    resultDF
  }

  private def missingValueIndicatorColumn(
      dataFrame: DataFrame,
      declaredAsMissingValues: Seq[String],
      column: String,
      prefix: String) = {
    CommonQueries.isMissingInColumnPredicate(
        dataFrame.sparkDataFrame, column, declaredAsMissingValues)
      .as(prefix + column).cast(BooleanType)
  }

  private def calculateMode(
      dataFrame: DataFrame,
      column: String,
      declaredAsMissing: Seq[String]): Option[Any] = {

    import org.apache.spark.sql.functions.desc

    val sparkDataFrame = dataFrame.sparkDataFrame

    val sparkColumn = sparkDataFrame(column)

    val resultArray = sparkDataFrame
      .select(sparkColumn)
      .filter(!CommonQueries.isMissingInColumnPredicate(sparkDataFrame, column, declaredAsMissing))
      .groupBy(sparkColumn)
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
}

object MissingValuesHandler {

  sealed trait Strategy extends Choice {
    import ai.deepsense.deeplang.doperables.MissingValuesHandler.Strategy._

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[RemoveRow],
      classOf[RemoveColumn],
      classOf[ReplaceWithCustomValue],
      classOf[ReplaceWithMode])
  }

  object Strategy {
    case class RemoveRow() extends Strategy {
      override val name: String = "remove row"
      override val params: Array[Param[_]] = Array()
    }

    case class RemoveColumn() extends Strategy {
      override val name: String = "remove column"
      override val params: Array[Param[_]] = Array()
    }

    case class ReplaceWithCustomValue() extends Strategy {

      override val name: String = "replace with custom value"
      val customValue = StringParam(
        name = "value",
        description = Some("Replacement for missing values."))

      def getCustomValue: String = $(customValue)
      def setCustomValue(value: String): this.type = set(customValue, value)

      override val params: Array[Param[_]] = Array(customValue)
    }

    case class ReplaceWithMode() extends Strategy {

      override val name: String = "replace with mode"

      val emptyColumnStrategy = ChoiceParam[EmptyColumnsStrategy](
        name = "empty column strategy",
        description = Some("Strategy of handling columns with missing all values."))
      setDefault(emptyColumnStrategy, EmptyColumnsStrategy.RemoveEmptyColumns())

      def getEmptyColumnStrategy: EmptyColumnsStrategy = $(emptyColumnStrategy)
      def setEmptyColumnStrategy(value: EmptyColumnsStrategy): this.type =
        set(emptyColumnStrategy, value)

      override val params: Array[Param[_]] = Array(emptyColumnStrategy)
    }
  }

  sealed trait EmptyColumnsStrategy extends Choice {
    import ai.deepsense.deeplang.doperables.MissingValuesHandler.EmptyColumnsStrategy._

    override val choiceOrder: List[Class[_ <: EmptyColumnsStrategy]] = List(
      classOf[RemoveEmptyColumns],
      classOf[RetainEmptyColumns])
  }

  object EmptyColumnsStrategy {
    case class RemoveEmptyColumns() extends EmptyColumnsStrategy {
      override val name: String = "remove"
      override val params: Array[Param[_]] = Array()
    }
    case class RetainEmptyColumns() extends EmptyColumnsStrategy {
      override val name: String = "retain"
      override val params: Array[Param[_]] = Array()
    }
  }

  sealed trait MissingValueIndicatorChoice extends Choice {
    import ai.deepsense.deeplang.doperables.MissingValuesHandler.MissingValueIndicatorChoice._

    def getIndicatorPrefix: Option[String]
    override val choiceOrder: List[Class[_ <: MissingValueIndicatorChoice]] =
      List(
        classOf[Yes],
        classOf[No])
  }

  object MissingValueIndicatorChoice {
    case class Yes() extends MissingValueIndicatorChoice {

      override val name: String = "Yes"

      val indicatorPrefix = new PrefixBasedColumnCreatorParam(
        name = "indicator column prefix",
        description = Some("Prefix for columns indicating presence of missing values.")
      )
      setDefault(indicatorPrefix, "")

      override def getIndicatorPrefix: Option[String] = Some($(indicatorPrefix))
      def setIndicatorPrefix(value: String): this.type = set(indicatorPrefix, value)

      override val params: Array[Param[_]] = Array(indicatorPrefix)
    }
    case class No() extends MissingValueIndicatorChoice {
      override val name: String = "No"

      override def getIndicatorPrefix: Option[String] = None

      override val params: Array[Param[_]] = Array()
    }
  }
}

case class UserDefinedMissingValue() extends Params {

  val missingValue = StringParam(
    name = "missing value",
    description =
      Some("""Value to be considered as a missing one.
        |Provided value will be cast to all chosen column types if possible,
        |so for example a value "-1" might be applied to all numeric and string columns."""
        .stripMargin),
    validator = new AcceptAllRegexValidator())

  def getMissingValue: String = $(missingValue)
  def setMissingValue(value: String): this.type = set(missingValue, value)
  setDefault(missingValue, "")

  val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array(missingValue)
}

private object MissingValuesHandlerUtils {

  import org.apache.spark.sql.functions.when

  def replaceMissings(
      context: ExecutionContext,
      dataFrame: DataFrame,
      declaredAsMissingValues: Seq[String],
      chosenColumns: Seq[String],
      replaceFunction: PartialFunction[String, Any]): DataFrame = {

    val df = dataFrame.sparkDataFrame

    val resultSparkDF = df.select(
      df.columns.map(columnName => {
        if (chosenColumns.contains(columnName)) {
          when(
            CommonQueries.isMissingInColumnPredicate(df, columnName, declaredAsMissingValues),
            replaceFunction.applyOrElse(columnName, Function.const(null)))
            .otherwise(df(columnName)).as(columnName)
        } else {
          df(columnName)
        }
      }): _*)

    context.dataFrameBuilder.buildDataFrame(df.schema, resultSparkDF.rdd)
  }
}

private object CommonQueries {

  def isMissingInRowPredicate(
      df: sql.DataFrame,
      columns: Seq[String],
      declaredAsMissing: Seq[String]): sql.Column = {

    val predicates = columns.map(isMissingInColumnPredicate(df, _, declaredAsMissing))
    predicates.reduce { _ or _ }
  }

  def isMissingInColumnPredicate(
      df: sql.DataFrame,
      columnName: String,
      declaredAsMissing: Seq[String]): sql.Column = {

    val convertedMissingValues =
      TypeMapper.convertRawValuesToColumnTypeIfPossible(
        df.schema, columnName, declaredAsMissing)

    val predicate = df(columnName)
      .isNull
      .or(df(columnName).isin(convertedMissingValues: _*))

    df.schema(columnName).dataType match {
      case _: DoubleType | FloatType => predicate.or(df(columnName).isNaN)
      case _ => predicate
    }
  }
}

private object TypeMapper {

  def convertRawValuesToColumnTypeIfPossible(
      schema: StructType,
      columnName: String,
      rawValues: Seq[String]): Seq[Any] = {
    val colIndex = schema.fieldIndex(columnName)
    val colStructField = schema.fields(colIndex)
    TypeMapper.convertRawValuesIfPossible(colStructField, rawValues)
  }

  def convertRawValuesIfPossible(field: StructField, rawValues: Seq[String]): Seq[Any] = {
    rawValues.flatMap((rawValue: String) => Try(convertRawValue(field, rawValue)).toOption)
  }

  def convertRawValues(field: StructField, rawValues: Seq[String]): Seq[Any] = {
    rawValues.map((rawValue: String) => convertRawValue(field, rawValue))
  }

  def convertRawValue(field: StructField, rawValue: String): Any = {
    try {
      field.dataType match {
        case ByteType => rawValue.toByte
        case DecimalType() => new java.math.BigDecimal(rawValue)
        case DoubleType => rawValue.toDouble
        case FloatType => rawValue.toFloat
        case IntegerType => rawValue.toInt
        case LongType => rawValue.toLong
        case ShortType => rawValue.toShort
        case BooleanType => rawValue.toBoolean
        case StringType => rawValue
        case TimestampType => Timestamp.valueOf(rawValue)
      }
    } catch {
      case e: Exception =>
        throw new ValueConversionException(rawValue, field)
    }
  }
}
