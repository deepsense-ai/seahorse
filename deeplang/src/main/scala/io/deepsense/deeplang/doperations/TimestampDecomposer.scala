/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.doperations

import org.apache.spark.sql
import org.apache.spark.sql.types.{IntegerType, StructType}
import org.apache.spark.sql.{Column, ColumnName}

import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameUtils}
import io.deepsense.deeplang.doperations.TimestampDecomposer.{timeUnits, timestampColumnParamKey, timestampParts, timestampPartsParamKey}
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation, DOperation1To1, ExecutionContext}

/**
 * Operation that is able to take dataframe and split its timestamp column to many columns
 * containing timestamp parts. Client can choose timestamp parts from set:
 * {year, month, day, hour, minutes, seconds} using parameters.
 * Choosing $part value will result in adding new column with name:
 * {original_timestamp_column_name}_$part of IntegerType containing $part value.
 * If a column with that name already exists {original_timestamp_column_name}_$part_N will be used,
 * where N is first not used Int value starting from 1.
 */
class TimestampDecomposer extends DOperation1To1[DataFrame, DataFrame] {

  override val parameters = ParametersSchema(
    timestampColumnParamKey ->
      SingleColumnSelectorParameter("Timestamp column to decompose", true),
    timestampPartsParamKey ->
      MultipleChoiceParameter("Parts of the date time to select", None, true, timeUnits)
  )

  override val id: DOperation.Id = "42f2eb12-e28b-11e4-8a00-1681e6b88ec1"

  override val name: String = "Decompose Timestamp"

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {
    val timestampColumnName: String =
      dataFrame.getColumnName(parameters.getSingleColumnSelection(timestampColumnParamKey).get)
    val timestampUnitColumnCreator =
      timestampUnitColumn(dataFrame.sparkDataFrame, timestampColumnName) _
    val selectedParts: Set[String] =
      parameters.getMultipleChoice(timestampPartsParamKey).get.map(_.label).toSet
    val newColumns: Traversable[Column] = timestampParts.filter(
      p => selectedParts.contains(p.name)).map(timestampUnitColumnCreator)
    val columns: List[Column] = new ColumnName("*") :: newColumns.toList
    val newSparkDataFrame = dataFrame.sparkDataFrame.select(columns:_*)
    context.dataFrameBuilder.buildDataFrame(newSparkDataFrame)
  }

  private[this] def timestampUnitColumn(sDataFrame: sql.DataFrame, columnName: String)
      (timestampPart: TimestampDecomposer.TimestampPart): Column = {
    val newColumnName: String =
      timestampUnitColumnName(sDataFrame.schema, columnName, timestampPart.name)
    (sDataFrame(columnName).substr(timestampPart.start, timestampPart.length)
      as newColumnName cast IntegerType)
  }

  private[this] def timestampUnitColumnName(
      schema: StructType,
      originalColumnName: String,
      timestampPart: String): String = {
    val firstFreeNamesLevel = DataFrameUtils.getFirstFreeNamesLevel(schema.fieldNames.toSet,
      originalColumnName, timestampParts.map(_.name).toSet, columnName)
    columnName(originalColumnName, timestampPart, firstFreeNamesLevel)
  }

  private[this] def columnName(
      originalColumnName: String,
      timestampPart: String,
      level: Int): String = {
    val levelSuffix = if (level > 0) "_" + level else ""
    originalColumnName + "_" + timestampPart + levelSuffix
  }
}

object TimestampDecomposer {

  private case class TimestampPart(name: String, start: Int, length: Int)

  private val timestampColumnParamKey = "timestampColumn"
  private val timestampPartsParamKey = "parts"

  private val timestampParts = List(
    new TimestampPart("year", 0, 4),
    new TimestampPart("month", 6, 2),
    new TimestampPart("day", 9, 2),
    new TimestampPart("hour", 12, 2),
    new TimestampPart("minutes", 15, 2),
    new TimestampPart("seconds", 18, 2)
  )

  /**
   * Possible options for multiChoice parameter representing parts of timestamp to extract
   */
  private val timeUnits: Map[String, ParametersSchema] =
    timestampParts.map(p => p.name -> ParametersSchema()).toMap

}
