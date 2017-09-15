/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.doperations

import org.apache.spark.sql
import org.apache.spark.sql.Column
import org.apache.spark.sql.types.IntegerType

import io.deepsense.deeplang.doperables.dataframe.DataFrame
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
      SingleColumnSelectorParameter("Timestamp column to decompose", required = true),
    timestampPartsParamKey ->
      MultipleChoiceParameter("Parts of the date time to select", None, required = true, timeUnits)
  )

  override val id: DOperation.Id = "42f2eb12-e28b-11e4-8a00-1681e6b88ec1"

  override val name: String = "Decompose Timestamp"

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {
    val decomposedColumnName: String =
      dataFrame.getColumnName(parameters.getSingleColumnSelection(timestampColumnParamKey).get)
    val firstFreeNamesLevel = dataFrame.getFirstFreeNamesLevel(
      decomposedColumnName, timestampParts.map(_.name).toSet)

    val selectedParts = parameters.getMultipleChoice(timestampPartsParamKey).get.map(_.label).toSet

    val newColumns = for {
      part <- timestampParts
      if selectedParts.contains(part.name)
    } yield timestampUnitColumn(
        dataFrame.sparkDataFrame, decomposedColumnName, part, firstFreeNamesLevel)

    dataFrame.withColumns(context, newColumns)
  }

  private[this] def timestampUnitColumn(
      sparkDataFrame: sql.DataFrame,
      columnName: String,
      timestampPart: TimestampDecomposer.TimestampPart,
      level: Int): Column = {

    val newColumnName = DataFrame.createColumnName(columnName, timestampPart.name, level)
    (sparkDataFrame(columnName).substr(timestampPart.start, timestampPart.length)
      as newColumnName cast IntegerType)
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
