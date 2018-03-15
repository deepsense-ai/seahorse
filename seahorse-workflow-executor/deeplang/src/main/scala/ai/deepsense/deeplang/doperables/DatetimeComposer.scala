/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import ai.deepsense.commons.types.ColumnType
import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import ai.deepsense.deeplang.params.choice.{Choice, MultipleChoiceParam}
import ai.deepsense.deeplang.params.selections.SingleColumnSelection
import ai.deepsense.deeplang.params.{SingleColumnCreatorParam, SingleColumnSelectorParam}
import org.apache.spark.sql.functions.{concat_ws, format_string, lit, unix_timestamp}
import org.apache.spark.sql.types.{DoubleType, StructField, StructType, TimestampType}


case class DatetimeComposer() extends Transformer {

  import DatetimeComposer._

  val timestampColumnsParam = MultipleChoiceParam[TimestampPartColumnChoice](
    name = "parts",
    description = Some("Columns containing timestamp parts.")
  )
  setDefault(timestampColumnsParam, Set.empty: Set[TimestampPartColumnChoice])

  def getTimestampColumns(): Set[TimestampPartColumnChoice] = $(timestampColumnsParam)

  def setTimestampColumns(timestampParts: Set[TimestampPartColumnChoice]): this.type =
    set(timestampColumnsParam, timestampParts)

  val outputColumnParam = SingleColumnCreatorParam(
    name = "output column",
    description = Some("Column to save results to.")
  )
  setDefault(outputColumnParam, "Timestamp")

  def getOutputColumn(): String = $(outputColumnParam)

  def setOutputColumn(outputColumn: String): this.type =
    set(outputColumnParam, outputColumn)

  override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array(timestampColumnsParam, outputColumnParam)


  override def applyTransform(context: ExecutionContext, dataFrame: DataFrame): DataFrame = {
    val sparkDataFrame = dataFrame.sparkDataFrame
    val dataColumns = getTimestampColumns().map(p => p.name -> p.getTimestampColumn).toMap
    // this will never fail, as _transformSchema is always ran before _transform
    val newSchema = applyTransformSchema(sparkDataFrame.schema).get

    val partColumns = for {part <- orderedTimestampParts}
      yield dataColumns.get(part.name) match {
        case Some(singleColumnSelect) =>
          format_string(
            part.formatString,
            sparkDataFrame(dataFrame.getColumnName(singleColumnSelect)) cast DoubleType
          )
        case None => lit(part.formatString.format(part.defaultValue.toDouble))
      }

    val newColumn = unix_timestamp(
      concat_ws(" ",
        concat_ws("-", partColumns(0), partColumns(1), partColumns(2)),
        concat_ws(":", partColumns(3), partColumns(4), partColumns(5))
      )
    ) cast TimestampType

    // have to create dataFrame using schema for timestamp column to be nullable
    val appendedFrame = sparkDataFrame.withColumn(getOutputColumn(), newColumn)
    DataFrame.fromSparkDataFrame(context.sparkSQLSession.createDataFrame(appendedFrame.rdd, newSchema))
  }

  override def applyTransformSchema(schema: StructType): Option[StructType] = {
    assertCorrectColumnTypes(schema)
    val newColumn = StructField(getOutputColumn(), TimestampType, nullable = true)
    val inferredSchema = StructType(schema.fields :+ newColumn)
    Some(inferredSchema)
  }

  private def assertCorrectColumnTypes(schema: StructType): Unit = {
    for {timestampPart <- getTimestampColumns()}
      yield DataFrameColumnsGetter.assertExpectedColumnType(
        schema,
        timestampPart.getTimestampColumn(),
        ColumnType.numeric
      )
  }

}

object DatetimeComposer {

  import TimestampPartColumnChoice._

  lazy val orderedTimestampParts = List(
    Year,
    Month,
    Day,
    Hour,
    Minutes,
    Seconds
  )

  sealed trait TimestampPartColumnChoice extends Choice {
    override val name: String
    val defaultValue: Int
    val formatString: String

    val timestampColumnSelectorParam = SingleColumnSelectorParam(
      name = name + " column",
      description = Some("Column containing " + name),
      portIndex = 0
    )

    def getTimestampColumn(): SingleColumnSelection = $(timestampColumnSelectorParam)

    def setTimestampColumn(timestampColumn: SingleColumnSelection): this.type =
      set(timestampColumnSelectorParam, timestampColumn)

    override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array(timestampColumnSelectorParam)

    override val choiceOrder: List[Class[_ <: Choice]] = orderedTimestampParts.map(p => p.getClass)
  }

  object TimestampPartColumnChoice {

    case object Year extends TimestampPartColumnChoice {
      override lazy val name = "year"
      override val defaultValue = 1970
      override val formatString = "%04.0f"
    }

    case object Month extends TimestampPartColumnChoice {
      override lazy val name = "month"
      override val defaultValue = 1
      override val formatString = "%02.0f"
    }

    case object Day extends TimestampPartColumnChoice {
      override lazy val name = "day"
      override val defaultValue = 1
      override val formatString = "%02.0f"
    }

    case object Hour extends TimestampPartColumnChoice {
      override lazy val name = "hour"
      override val defaultValue = 0
      override val formatString = "%02.0f"
    }

    case object Minutes extends TimestampPartColumnChoice {
      override lazy val name = "minutes"
      override val defaultValue = 0
      override val formatString = "%02.0f"
    }

    case object Seconds extends TimestampPartColumnChoice {
      override lazy val name = "seconds"
      override val defaultValue = 0
      override val formatString = "%02.0f"
    }

  }

}
