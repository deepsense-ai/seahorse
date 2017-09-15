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

import au.com.bytecode.opencsv.CSVParser
import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Row, types}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import io.deepsense.deeplang.doperations.CsvParameters.ColumnSeparator
import io.deepsense.deeplang.doperations.exceptions.{DeepSenseIOException, InvalidFileException}
import io.deepsense.deeplang.parameters.FileFormat.FileFormat
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation0To1, ExecutionContext, FileSystemClient}

case class ReadDataFrame() extends DOperation0To1[DataFrame] with ReadDataFrameParameters {
  import io.deepsense.deeplang.doperations.ReadDataFrame._

  override val id: Id = "c48dd54c-6aef-42df-ad7a-42fc59a09f0e"
  override val name = "Read DataFrame"
  override val parameters = ParametersSchema(
    "source" -> sourceFileParameter,
    "format" -> formatParameter,
    "line separator" -> lineSeparatorParameter)

  override protected def _execute(context: ExecutionContext)(): DataFrame = {
    val path = FileSystemClient.replaceLeadingTildeWithHomeDirectory(sourceFileParameter.value)

    try {
      FileFormat.withName(formatParameter.value) match {
        case FileFormat.CSV =>
          val conf = new Configuration(context.sparkContext.hadoopConfiguration)
          conf.set(
            ReadDataFrame.recordDelimiterSettingName,
            determineLineSeparator())
          val lines = context.sparkContext.newAPIHadoopFile(
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
      LineSeparator.withName(lineSeparatorParameter.value)
    } catch {
      // Unix line separator by default
      case e: NoSuchElementException => LineSeparator.UNIX
    }

    lineSeparator match {
      case LineSeparator.WINDOWS => "\r\n"
      case LineSeparator.UNIX => "\n"
      case LineSeparator.CUSTOM => customLineSeparatorParameter.value
    }
  }

  // TODO https://codilime.atlassian.net/browse/DS-1351
  private def dataFrameFromCSV(
      context: ExecutionContext,
      rdd: RDD[String],
      categoricalColumnsSelection: MultipleColumnSelection): DataFrame = {

    val namesIncluded = csvNamesIncludedParameter.value
    val lines = splitLinesIntoColumns(rdd, determineColumnSeparator()).cache()

    val (columnNames, dataLines) = if (namesIncluded) {
      val processedFirstLine = lines.first().map(_.trim).map(removeQuotes).map(sanitizeColumnName)
      (renameUnnamed(processedFirstLine), skipFirstLine(lines).cache())
    } else {
      (generateColumnNames(columnsNo = lines.first().length), lines)
    }

    val shouldConvertToBoolean = csvShouldConvertToBooleanParameter.value
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

trait ReadDataFrameParameters extends CsvParameters {
  import io.deepsense.deeplang.doperations.ReadDataFrame._

  val csvShouldConvertToBooleanParameter = BooleanParameter(
    "Should columns containing only 0 and 1 be converted to Boolean?",
    default = Some(false))

  val categoricalColumnsParameter = ColumnSelectorParameter(
    "Categorical columns in the input file",
    portIndex = 0,
    default = Some(MultipleColumnSelection.emptySelection))

  val formatParameter = ChoiceParameter(
    "Format of the input file",
    default = Some(FileFormat.CSV.toString),
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
    validator = new AcceptAllRegexValidator())

  val customLineSeparatorParameter = StringParameter(
    "Custom line separator",
    default = None,
    validator = new AcceptAllRegexValidator())

  val lineSeparatorParameter = ChoiceParameter(
    "Line separator",
    default = Some(LineSeparator.UNIX.toString),
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
    operation.sourceFileParameter.value = filePath
    operation.formatParameter.value = format.toString
    operation.categoricalColumnsParameter.value = categoricalColumns
    operation
  }

  def apply(
      filePath: String,
      lineSeparator: (LineSeparator.LineSeparator, Option[String]),
      csvColumnSeparator: (ColumnSeparator.ColumnSeparator, Option[String]),
      csvNamesIncluded: Boolean,
      csvShouldConvertToBoolean: Boolean,
      categoricalColumns: Option[MultipleColumnSelection]): ReadDataFrame = {

    val operation = new ReadDataFrame()

    operation.formatParameter.value = "CSV"
    operation.lineSeparatorParameter.value = lineSeparator._1.toString
    operation.customLineSeparatorParameter.value = lineSeparator._2
    operation.sourceFileParameter.value = filePath
    operation.csvColumnSeparatorParameter.value = csvColumnSeparator._1.toString
    operation.csvCustomColumnSeparatorParameter.value = csvColumnSeparator._2
    operation.csvNamesIncludedParameter.value = csvNamesIncluded
    operation.csvShouldConvertToBooleanParameter.value = csvShouldConvertToBoolean
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
      categoricalColumnsSelection: MultipleColumnSelection): (Set[Int], Seq[String]) = {
    val columnNames: Seq[String] = schema.fields.map(_.name)
    val categoricalColumnNames =
      DataFrameColumnsGetter.getColumnNames(schema, categoricalColumnsSelection)
    val categoricalColumnNamesSet = categoricalColumnNames.toSet
    val categoricalColumnIndices = (for {
      (name, index) <- columnNames.zipWithIndex if categoricalColumnNamesSet.contains(name)
    } yield index).toSet

    (categoricalColumnIndices, categoricalColumnNames)
  }


  /**
   * Splits string lines by separator, but escapes separator within double quotes.
   * For example, line "a,b,\"x,y,z\"" will be split into ["a","b","\"x,y,z\""].
   */
  private def splitLinesIntoColumns(lines: RDD[String], separator: Char) = {
    lines.map(
      new CSVParser(separator, CSVParser.NULL_CHARACTER, CSVParser.NULL_CHARACTER)
        .parseLine(_).toSeq
    )
  }

  private def skipFirstLine[T : ClassTag](rdd: RDD[T]): RDD[T] =
    rdd.mapPartitionsWithIndex { (i, iterator) =>
      if (i == 0 && iterator.hasNext) {
        iterator.next()
      }
      iterator
    }

  private def generateColumnNames(columnsNo: Int): Seq[String] =
    (0 until columnsNo).map(generateColumnName)

  private def generateColumnName(columnNo: Int): String = s"unnamed_$columnNo"

  private def renameUnnamed(maybeEmptyNames: Seq[String]): Seq[String] = {
    val (columnNames, _) = maybeEmptyNames.foldLeft((Seq[String](), maybeEmptyNames.toSet)) {
      case ((columnsNames, usedNames), inputColumnName) =>
        val columnName = if (StringUtils.isBlank(inputColumnName)) {
          generateUniqueName(usedNames)
        } else {
          inputColumnName
        }
        (columnName +: columnsNames, usedNames + columnName)
    }
    columnNames.reverse
  }

  private def generateUniqueName(usedNames: Set[String]): String = {
    def nameUnique(name: String) = !usedNames.contains(name)
    val lastIndex = usedNames.size + 1
    (0 to lastIndex).collectFirst {
      case i if nameUnique(generateColumnName(i)) => generateColumnName(i)
    }.get
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

    if (willBeCategorical) {
      /*
       * TODO be careful while merging DS-1692 Change CSV reading logic
       * if-else is responsible for converting empty categorical strings to nulls
       */
      val cellWithRemovedQuotes = removeQuotes(trimmedCell)
      if (cellWithRemovedQuotes.isEmpty) null else cellWithRemovedQuotes
    } else if (targetType == types.StringType) {
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
