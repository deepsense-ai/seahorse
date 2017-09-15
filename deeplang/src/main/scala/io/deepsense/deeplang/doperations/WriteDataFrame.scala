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

import java.io.{IOException, StringWriter}
import java.sql.Timestamp

import scala.collection.immutable.ListMap
import scala.reflect.runtime.{universe => ru}

import au.com.bytecode.opencsv.CSVWriter
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.spark.sql.Row

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoricalMapper, CategoricalMetadata}
import io.deepsense.deeplang.doperations.CsvParameters.ColumnSeparator
import io.deepsense.deeplang.doperations.exceptions.DeepSenseIOException
import io.deepsense.deeplang.parameters.FileFormat.FileFormat
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation1To0, ExecutionContext}

case class WriteDataFrame() extends DOperation1To0[DataFrame] with CsvParameters {

  override val id: Id = "9e460036-95cc-42c5-ba64-5bc767a40e4e"

  override val name: String = "Write DataFrame"

  val formatParameter = ChoiceParameter(
    description = "Format of the output file",
    default = Some(FileFormat.CSV.toString),
    required = true,
    options = ListMap(
      FileFormat.CSV.toString -> ParametersSchema(
        "column separator" -> csvColumnSeparatorParameter,
        "write header" -> csvNamesIncludedParameter),
      FileFormat.PARQUET.toString -> ParametersSchema(),
      FileFormat.JSON.toString -> ParametersSchema()
    )
  )

  val outputFileParameter = StringParameter(
    description = "Output file path",
    default = None,
    required = true,
    validator = new AcceptAllRegexValidator())

  override val parameters = ParametersSchema(
    "format" -> formatParameter,
    "output file" -> outputFileParameter
  )

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): Unit = {
    val path = outputFileParameter.value.get

    FileFormat.withName(formatParameter.value.get) match {
      case FileFormat.CSV =>
        val categoricalMetadata = CategoricalMetadata(dataFrame.sparkDataFrame)
        val csv = dataFrame.sparkDataFrame.rdd
          .map(rowToStringArray(categoricalMetadata) andThen convertToCsv)

        val result = if (csvNamesIncludedParameter.value.get) {
          context.sparkContext.parallelize(Seq(
            convertToCsv (dataFrame.sparkDataFrame.schema.fieldNames)
          )).union(csv)
        } else {
          csv
        }

        try {
          result.saveAsTextFile(path)
        } catch {
          case e: IOException => throw DeepSenseIOException (e)
        }
      case FileFormat.PARQUET =>
        // TODO: DS-1480 Writing DF in parquet format when column names contain forbiden characters
        dataFrame.sparkDataFrame.write.parquet(path)
      case FileFormat.JSON =>
        val mapper = CategoricalMapper(dataFrame, context.dataFrameBuilder)
        val uncategorizedDataFrame = mapper.uncategorized(dataFrame)
        uncategorizedDataFrame.sparkDataFrame.write.json(path)
    }
  }

  private def contains(o: Option[String], value: String): Boolean = {
    o match {
      case None => false
      case Some(x) => x == value
    }
  }

  private def rowToStringArray(categoricalMetadata: CategoricalMetadata) = (row: Row) => {
    val zippedWithIndex = row.toSeq.zipWithIndex

    val withCategoricals = zippedWithIndex.map { case (value, index) =>
      if (value != null && categoricalMetadata.isCategorical(index)) {
        categoricalMetadata.mapping(index).idToValue(value.asInstanceOf[Int])
      } else {
        value
      }
    }

    withCategoricals.toArray.map {
      case null => ""
      case true => "1"
      case false => "0"
      case s: String => StringEscapeUtils.escapeCsv(s)
      case t: Timestamp => DateTimeConverter.toString(DateTimeConverter.fromMillis(t.getTime))
      case x => x.toString
    }
  }

  val convertToCsv = (data: Array[String]) => {
    import scala.collection.JavaConversions._
    val buf = new StringWriter
    val writer = new CSVWriter(buf, determineColumnSeparator(),
      CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER, "")
    writer.writeAll(List(data))
    buf.toString
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

object WriteDataFrame {
  def apply(
      fileFormat: FileFormat,
      path: String): WriteDataFrame = {

    val writeDataFrame = WriteDataFrame()
    writeDataFrame.formatParameter.value = Some(fileFormat.toString)
    writeDataFrame.outputFileParameter.value = Some(path)
    writeDataFrame
  }

  /**
   * WriteDataFrame: CSV file type.
   */
  def apply(
      columnSeparator: (ColumnSeparator.ColumnSeparator, Option[String]),
      writeHeader: Boolean,
      path: String): WriteDataFrame = {

    val (separator, customSeparator) = columnSeparator
    val writeDataFrame = WriteDataFrame()
    writeDataFrame.formatParameter.value = Some(FileFormat.CSV.toString)
    writeDataFrame.csvColumnSeparatorParameter.value = Some(separator.toString)
    writeDataFrame.csvCustomColumnSeparatorParameter.value = customSeparator
    writeDataFrame.csvNamesIncludedParameter.value = Some(writeHeader)
    writeDataFrame.outputFileParameter.value = Some(path)
    writeDataFrame
  }
}
