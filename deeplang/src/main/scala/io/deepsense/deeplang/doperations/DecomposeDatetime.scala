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

/** TODO as Transformer */
package io.deepsense.deeplang.doperations

import scala.collection.immutable.ListMap
import scala.reflect.runtime.{universe => ru}

import org.apache.spark.sql
import org.apache.spark.sql.Column
import org.apache.spark.sql.types.DoubleType

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.doperables.dataframe.DataFrame
// import io.deepsense.deeplang.doperations.DecomposeDatetime.{timeUnits, timestampParts}
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
/*
case class DecomposeDatetime() extends DOperation1To1[DataFrame, DataFrame] with OldOperation {
  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]

  val timestampColumnParam = SingleColumnSelectorParameter(
    "Timestamp column to decompose", portIndex = 0)

  val timestampPartsParam = MultipleChoiceParameter(
    "Parts of the date/time to retain", default = None, options = timeUnits)

  val timestampPrefixParam = PrefixBasedColumnCreatorParameter(
    "Common prefix for names of created columns", default = Some(""))

  override val parameters = ParametersSchema(
    "timestamp column" -> timestampColumnParam,
    "parts" -> timestampPartsParam,
    "prefix" -> timestampPrefixParam
  )

  override val id: DOperation.Id = "42f2eb12-e28b-11e4-8a00-1681e6b88ec1"

  override val name: String = "Decompose Datetime"

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {
    val decomposedColumnName: String =
      dataFrame.getColumnName(timestampColumnParam.value)

    DataFrame.assertExpectedColumnType(
      dataFrame.sparkDataFrame.schema.fields.filter(_.name == decomposedColumnName).head,
      ColumnType.timestamp)

    val selectedParts = timestampPartsParam.selections.map(_.label).toSet

    val newColumns = for {
      part <- timestampParts
      if selectedParts.contains(part.name)
    } yield timestampUnitColumn(dataFrame.sparkDataFrame, decomposedColumnName, part)

    dataFrame.withColumns(context, newColumns)
  }

  private[this] def timestampUnitColumn(
      sparkDataFrame: sql.DataFrame,
      columnName: String,
      timestampPart: DecomposeDatetime.TimestampPart): Column = {

    val newColumnName = timestampPrefixParam.value + timestampPart.name

    (sparkDataFrame(columnName).substr(timestampPart.start, timestampPart.length)
      as newColumnName cast DoubleType)
  }
}

object DecomposeDatetime {

  private case class TimestampPart(name: String, start: Int, length: Int)

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
  private val timeUnits: ListMap[String, ParametersSchema] =
    ListMap(timestampParts.map(_.name -> ParametersSchema()): _*)

  def apply(columnSelection: SingleColumnSelection,
            selectedParts: Seq[String],
            prefix: Option[String]): DecomposeDatetime = {
    val operation = new DecomposeDatetime
    operation.timestampColumnParam.value = columnSelection
    operation.timestampPartsParam.value = selectedParts
    operation.timestampPrefixParam.value = prefix
    operation
  }

}
*/
