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

import org.apache.spark.sql
import org.apache.spark.sql.Column
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}

import ai.deepsense.commons.types.ColumnType
import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.DatetimeDecomposer.TimestampPart
import ai.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import ai.deepsense.deeplang.params.choice.{Choice, MultipleChoiceParam}
import ai.deepsense.deeplang.params.selections.SingleColumnSelection
import ai.deepsense.deeplang.params.{Param, PrefixBasedColumnCreatorParam, SingleColumnSelectorParam}

/**
 * Operation that is able to take dataframe and split its timestamp column to many columns
 * containing timestamp parts. Client can choose timestamp parts from set:
 * {year, month, day, hour, minutes, seconds} using parameters.
 * Choosing &#36;part value will result in adding new column with name:
 * {original_timestamp_column_name}_&#36;part of IntegerType containing &#36;part value.
 * If a column with that name already exists {original_timestamp_column_name}_&#36;part_N will be used,
 * where N is first not used Int value starting from 1.
 */
case class DatetimeDecomposer() extends Transformer {

  val timestampColumnParam = SingleColumnSelectorParam(
    name = "timestamp column",
    description = Some("Timestamp column to decompose."),
    portIndex = 0
  )

  def getTimestampColumn: SingleColumnSelection = $(timestampColumnParam)
  def setTimestampColumn(timestampColumn: SingleColumnSelection): this.type =
    set(timestampColumnParam, timestampColumn)

  val timestampPartsParam = MultipleChoiceParam[TimestampPart](
    name = "parts",
    description = Some("Parts of the date/time to retain.")
  )

  def getTimestampParts: Set[TimestampPart] = $(timestampPartsParam)
  def setTimestampParts(timestampParts: Set[TimestampPart]): this.type =
    set(timestampPartsParam, timestampParts)

  val timestampPrefixParam = PrefixBasedColumnCreatorParam(
    name = "prefix",
    description = Some("Common prefix for names of created columns.")
  )
  setDefault(timestampPrefixParam, "")

  def getTimestampPrefix: String = $(timestampPrefixParam)
  def setTimestampPrefix(timestampPrefix: String): this.type =
    set(timestampPrefixParam, timestampPrefix)

  override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array(
    timestampColumnParam, timestampPartsParam, timestampPrefixParam)

  override def applyTransform(context: ExecutionContext, dataFrame: DataFrame): DataFrame = {
    DataFrameColumnsGetter.assertExpectedColumnType(
      dataFrame.sparkDataFrame.schema,
      getTimestampColumn,
      ColumnType.timestamp)

    val decomposedColumnName: String = dataFrame.getColumnName(getTimestampColumn)

    val newColumns = for {
      range <- DatetimeDecomposer.timestampPartRanges
      if getTimestampParts.contains(range.part)
    } yield timestampUnitColumn(dataFrame.sparkDataFrame, decomposedColumnName, range)

    dataFrame.withColumns(context, newColumns)
  }

  private[this] def timestampUnitColumn(
      sparkDataFrame: sql.DataFrame,
      columnName: String,
      timestampPart: DatetimeDecomposer.TimestampPartRange): Column = {

    val newColumnName = getTimestampPrefix + timestampPart.part.name

    (sparkDataFrame(columnName).substr(timestampPart.start, timestampPart.length)
      as newColumnName cast DoubleType)
  }

  override def applyTransformSchema(schema: StructType): Option[StructType] = {
    DataFrameColumnsGetter.assertExpectedColumnType(
      schema,
      getTimestampColumn,
      ColumnType.timestamp)

    val newColumns = for {
      range <- DatetimeDecomposer.timestampPartRanges
      if getTimestampParts.contains(range.part)
    } yield StructField(getTimestampPrefix + range.part.name, DoubleType)

    val inferredSchema = StructType(schema.fields ++ newColumns)
    Some(inferredSchema)
  }
}

object DatetimeDecomposer {

  import TimestampPart._

  sealed trait TimestampPart extends Choice {

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Year],
      classOf[Month],
      classOf[Day],
      classOf[Hour],
      classOf[Minutes],
      classOf[Seconds])
  }

  object TimestampPart {
    case class Year() extends TimestampPart {
      override val name: String = "year"
      override val params: Array[Param[_]] = Array()
    }

    case class Month() extends TimestampPart {
      override val name: String = "month"
      override val params: Array[Param[_]] = Array()
    }

    case class Day() extends TimestampPart {
      override val name: String = "day"
      override val params: Array[Param[_]] = Array()
    }

    case class Hour() extends TimestampPart {
      override val name: String = "hour"
      override val params: Array[Param[_]] = Array()
    }

    case class Minutes() extends TimestampPart {
      override val name: String = "minutes"
      override val params: Array[Param[_]] = Array()
    }

    case class Seconds() extends TimestampPart {
      override val name: String = "seconds"
      override val params: Array[Param[_]] = Array()
    }
  }

  private case class TimestampPartRange(part: TimestampPart, start: Int, length: Int)

  private val timestampPartRanges = List(
    TimestampPartRange(Year(), 0, 4),
    TimestampPartRange(Month(), 6, 2),
    TimestampPartRange(Day(), 9, 2),
    TimestampPartRange(Hour(), 12, 2),
    TimestampPartRange(Minutes(), 15, 2),
    TimestampPartRange(Seconds(), 18, 2)
  )
}
