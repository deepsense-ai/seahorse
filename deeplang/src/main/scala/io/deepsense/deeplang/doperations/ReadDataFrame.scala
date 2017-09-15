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


import java.io.IOException
import java.util.NoSuchElementException

import scala.collection.immutable.ListMap
import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{StructType, StringType, StructField}
import org.apache.spark.sql.{Row, types}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import io.deepsense.deeplang.doperations.exceptions.{DeepSenseIOException, InvalidFileException}
import io.deepsense.deeplang.parameters.FileFormat.FileFormat
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation0To1, ExecutionContext}


case class ReadDataFrame() extends DOperation0To1[DataFrame] with ReadDataFrameParameters {
  import io.deepsense.deeplang.doperations.ReadDataFrame._

  override val id: Id = "c48dd54c-6aef-42df-ad7a-42fc59a09f0e"
  override val name = "Read DataFrame"
  override val parameters = ParametersSchema(
    "source" -> sourceFileParameter,
    "format" -> formatParameter,
    "line separator" -> lineSeparatorParameter)

  override protected def _execute(context: ExecutionContext)(): DataFrame = {
    readFile(sourceFileParameter.value.get, context)
  }

  private def readFile(path: String, context: ExecutionContext): DataFrame = {
    val sparkContext = context.sqlContext.sparkContext

    try {
      FileFormat.withName(formatParameter.value.get) match {
        case FileFormat.CSV =>
          val conf = new Configuration(sparkContext.hadoopConfiguration)
          conf.set(
            ReadDataFrame.recordDelimiterSettingName,
            determineLineSeparator())
          val lines = sparkContext.newAPIHadoopFile(
            path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text], conf
          ).map { case (_, text) => text.toString }

          if (lines.isEmpty()) {
            throw new InvalidFileException(path, "empty")
          }
          dataFrameFromCSV(context, lines, categoricalColumnsParameter.value)
        case FileFormat.PARQUET =>
          context.dataFrameBuilder
            .buildDataFrame(context.sqlContext.read.parquet(path))
        case FileFormat.JSON =>
          val dataFrame = context.dataFrameBuilder
            .buildDataFrame(context.sqlContext.read.json(path))
          val schema = dataFrame.sparkDataFrame.schema

          val (categoricalColumnIndices, categoricalColumnNames) =
            getCategoricalColumns(schema, categoricalColumnsParameter.value)

          val convertedSchema = StructType(schema.zipWithIndex.map { case (column, index) =>
            if (categoricalColumnIndices.contains(index)) {
              column.copy(dataType = StringType)
            } else {
              column
            }
          })

          context.dataFrameBuilder
            .buildDataFrame(convertedSchema, dataFrame.sparkDataFrame.rdd, categoricalColumnNames)
      }
    } catch {
      case e: IOException => throw DeepSenseIOException(e)
    }
  }

  private def determineLineSeparator() : String = {
    val lineSeparator = try {
      LineSeparator.withName(lineSeparatorParameter.value.get)
    } catch {
      // Unix line separator by default
      case e: NoSuchElementException => LineSeparator.UNIX
    }

    lineSeparator match {
      case LineSeparator.WINDOWS => "\r\n"
      case LineSeparator.UNIX => "\n"
      case LineSeparator.CUSTOM => customLineSeparatorParameter.value.get
    }
  }

  // TODO https://codilime.atlassian.net/browse/DS-1351
  private def dataFrameFromCSV(
      context: ExecutionContext,
      rdd: RDD[String],
      categoricalColumnsSelection: Option[MultipleColumnSelection]): DataFrame = {

    val namesIncluded = csvNamesIncludedParameter.value.get
    val lines = splitLinesIntoColumns(rdd, csvColumnSeparatorParameter.value.get).cache()

    val (columnNames, dataLines) = if (namesIncluded) {
      val processedFirstLine = lines.first().map(_.trim).map(removeQuotes).map(sanitizeColumnName)
      (processedFirstLine, skipFirstLine(lines).cache())
    } else {
      (generateColumnNames(columnsNo = lines.first().length), lines)
    }

    val shouldConvertToBoolean = csvShouldConvertToBooleanParameter.value.get
    val linesInferences = dataLines.map(_.map {
      case cell => cellTypeInference(cell, shouldConvertToBoolean)
    })

    val inferredTypes = linesInferences.reduce{ (lInf1, lInf2) =>
      lInf1.zip(lInf2).map(reduceTypeInferences)
    }.map(_.toType)

    val schema = StructType(columnNames.zip(inferredTypes).map { case (columnName, inferredType) =>
      StructField(columnName, inferredType)
    })

    val (categoricalColumnIndices, categoricalColumnNames) =
      getCategoricalColumns(schema, categoricalColumnsSelection)

    val convertedData = dataLines.map(splitLine => Row.fromSeq(
      splitLine.zipWithIndex.zip(inferredTypes).map {
        case ((cell, index), inferredType) =>
          convertCell(
            cell,
            inferredType,
            willBeCategorical = categoricalColumnIndices.contains(index))
      }))

    val convertedSchema = StructType(schema.zipWithIndex.map { case (column, index) =>
      if (categoricalColumnIndices.contains(index)) column.copy(dataType = StringType) else column
    })

    context.dataFrameBuilder.buildDataFrame(convertedSchema, convertedData, categoricalColumnNames)
  }

  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

trait ReadDataFrameParameters {
  import io.deepsense.deeplang.doperations.ReadDataFrame._

  val csvColumnSeparatorParameter = StringParameter(
    "Column separator",
    default = Some(","),
    required = true,
    validator = new AcceptAllRegexValidator)

  val csvNamesIncludedParameter = BooleanParameter(
    "Does the first row include column names?",
    default = Some(true),
    required = true)

  val csvShouldConvertToBooleanParameter = BooleanParameter(
    "Should columns containing only 0 and 1 be converted to Boolean?",
    default = Some(false),
    required = true)

  val categoricalColumnsParameter = ColumnSelectorParameter(
    "Categorical columns in the input file",
    required = false,
    portIndex = 0)

  val formatParameter = ChoiceParameter(
    "Format of the input file",
    default = Some(FileFormat.CSV.toString),
    required = true,
    options = ListMap(
      FileFormat.CSV.toString -> ParametersSchema(
        "separator" -> csvColumnSeparatorParameter,
        "names included" -> csvNamesIncludedParameter,
        "convert to boolean" -> csvShouldConvertToBooleanParameter,
        "categorical columns" -> categoricalColumnsParameter),
      FileFormat.PARQUET.toString -> ParametersSchema(),
      FileFormat.JSON.toString -> ParametersSchema(
        "categorical columns" -> categoricalColumnsParameter)
    ))

  val sourceFileParameter = StringParameter(
    "Path to the DataFrame file",
    default = None,
    required = true,
    validator = new AcceptAllRegexValidator())

  val customLineSeparatorParameter = StringParameter(
    "Custom line separator",
    default = None,
    required = true,
    validator = new AcceptAllRegexValidator())

  val lineSeparatorParameter = ChoiceParameter(
    "Line separator",
    default = Some(LineSeparator.UNIX.toString),
    required = true,
    options = ListMap(
      LineSeparator.UNIX.toString -> ParametersSchema(),
      LineSeparator.WINDOWS.toString -> ParametersSchema(),
      LineSeparator.CUSTOM.toString -> ParametersSchema(
        LineSeparator.CUSTOM.toString -> customLineSeparatorParameter)))
}

object ReadDataFrame {
  val recordDelimiterSettingName = "textinputformat.record.delimiter"

  object LineSeparator extends Enumeration {
    type LineSeparator = Value
    val WINDOWS = Value("Windows line separator")
    val UNIX = Value("Unix line separator")
    val CUSTOM = Value("Custom line separator")
  }

  def apply(
      filePath: String,
      format: FileFormat,
      categoricalColumns: Option[MultipleColumnSelection] = None): ReadDataFrame = {
    val operation = new ReadDataFrame()
    operation.sourceFileParameter.value = Some(filePath)
    operation.formatParameter.value = Some(format.toString)
    operation.categoricalColumnsParameter.value = categoricalColumns
    operation
  }

  def apply(
      filePath: String,
      lineSeparator: (LineSeparator.LineSeparator, Option[String]),
      csvColumnSeparator: String,
      csvNamesIncluded: Boolean,
      csvShouldConvertToBoolean: Boolean,
      categoricalColumns: Option[MultipleColumnSelection]): ReadDataFrame = {

    val operation = new ReadDataFrame()

    operation.formatParameter.value = Some("CSV")
    operation.lineSeparatorParameter.value = Some(lineSeparator._1.toString)
    if (lineSeparator._2.isDefined) {
      operation.customLineSeparatorParameter.value = lineSeparator._2
    }
    operation.sourceFileParameter.value = Some(filePath)
    operation.csvColumnSeparatorParameter.value = Some(csvColumnSeparator)
    operation.csvNamesIncludedParameter.value = Some(csvNamesIncluded)
    operation.csvShouldConvertToBooleanParameter.value = Some(csvShouldConvertToBoolean)
    operation.categoricalColumnsParameter.value = categoricalColumns
    operation
  }

  case class TypeInference(canBeBoolean: Boolean, canBeNumeric: Boolean, canBeTimestamp: Boolean) {

    def toType: types.DataType = if (canBeBoolean) {
      types.BooleanType
    } else if (canBeNumeric) {
      types.DoubleType
    } else if (canBeTimestamp) {
      types.TimestampType
    } else {
      types.StringType
    }
  }

  private def reduceTypeInferences(inferences: (TypeInference, TypeInference)): TypeInference =
    TypeInference(
      canBeBoolean = inferences._1.canBeBoolean && inferences._2.canBeBoolean,
      canBeNumeric = inferences._1.canBeNumeric && inferences._2.canBeNumeric,
      canBeTimestamp = inferences._1.canBeTimestamp && inferences._2.canBeTimestamp)

  private def cellTypeInference(cell: String, convertToBoolean: Boolean): TypeInference = {
    val trimmedCell = cell.trim
    if (trimmedCell.isEmpty) {
      return TypeInference(
        canBeBoolean = convertToBoolean, canBeNumeric = true, canBeTimestamp = true)
    }

    try {
      val d = trimmedCell.toDouble
      val canBeBoolean = convertToBoolean && (d == 0 || d == 1)
      TypeInference(canBeBoolean = canBeBoolean, canBeNumeric = true, canBeTimestamp = false)
    } catch {
      case e: NumberFormatException => try {
        DateTimeConverter.parseDateTime(trimmedCell)
        TypeInference(canBeBoolean = false, canBeNumeric = false, canBeTimestamp = true)
      } catch {
        case e: IllegalArgumentException =>
          TypeInference(canBeBoolean = false, canBeNumeric = false, canBeTimestamp = false)
      }
    }
  }

  /**
   * Designates indices and names of columns selected to be categorized.
   * @return Tuple including set of indices and sequence of names.
   */
  private def getCategoricalColumns(
      schema: StructType,
      categoricalColumnsSelection: Option[MultipleColumnSelection]): (Set[Int], Seq[String]) = {

    val columnNames : Seq[String] = schema.fields.map( _.name )

    categoricalColumnsSelection match {
      case Some(selection) =>
        val categoricalColumnNames = DataFrameColumnsGetter.getColumnNames(schema, selection)
        val categoricalColumnNamesSet = categoricalColumnNames.toSet

        val categoricalColumnIndices = (for {
          (name, index) <- columnNames.zipWithIndex if categoricalColumnNamesSet.contains(name)
        } yield index).toSet

        (categoricalColumnIndices, categoricalColumnNames)

      case None => (Set.empty, Seq.empty)
    }
  }

  /**
   * Splits string lines by separator, but escapes separator within double quotes.
   * For example, line "a,b,\"x,y,z\"" will be split into ["a","b","\"x,y,z\""].
   */
  private def splitLinesIntoColumns(lines: RDD[String], separator: String) = {
    lines.map{ line => line.split(separator + """(?=([^"]*"[^"]*")*[^"]*$)""".r, -1).toSeq }
  }

  private def skipFirstLine[T : ClassTag](rdd: RDD[T]): RDD[T] =
    rdd.mapPartitionsWithIndex { (i, iterator) =>
      if (i == 0 && iterator.hasNext) {
        iterator.next()
      }
      iterator
    }

  private def generateColumnNames(columnsNo: Int): Seq[String] = {
    (0 until columnsNo).map(i => s"column_$i")
  }

  /**
   * Removes double quotes from front and end (if any).
   */
  private def removeQuotes(s: String): String = s.replaceAll("^\"|\"$", "")

  // TODO: remove replace when spark upgraded to 1.4. DS-635
  private def sanitizeColumnName(name: String): String = name.replace(".", "_")

  private def convertCell(
      cell: String,
      targetType: types.DataType,
      willBeCategorical: Boolean): Any = {
    val trimmedCell = cell.trim

    if (targetType == types.StringType || willBeCategorical) {
      removeQuotes(trimmedCell)

    } else if (trimmedCell.isEmpty) {
      null

    } else {
      targetType match {
        case types.BooleanType => trimmedCell.toDouble == 1
        case types.DoubleType => trimmedCell.toDouble
        case types.TimestampType => DateTimeConverter.parseTimestamp(trimmedCell)
      }
    }
  }

}
