/**
 * Copyright 2015, CodiLime Inc.
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


import java.util.NoSuchElementException
import scala.reflect.runtime._
import scala.reflect.runtime.{universe => ru}


import scala.collection.immutable.ListMap
import scala.reflect.ClassTag

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Row, types}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import io.deepsense.deeplang.doperations.exceptions.InvalidFileException
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation0To1, ExecutionContext}


case class ReadDataFrame() extends DOperation0To1[DataFrame] with ReadDataFrameParameters {
  import ReadDataFrame._

  override val id: Id = "c48dd54c-6aef-42df-ad7a-42fc59a09f0e"
  override val name = "Read DataFrame"
  override val parameters = ParametersSchema(
    "source" -> sourceParameter,
    "format" -> formatParameter,
    "line separator" -> lineSeparatorParameter)

  override protected def _execute(context: ExecutionContext)(): DataFrame = {
    val path = pathParameter.value.get
    val sparkContext = context.sqlContext.sparkContext

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

    FileFormat.withName(formatParameter.value.get) match {
      case FileFormat.CSV => dataFrameFromCSV(context, lines, csvCategoricalColumnsParameter.value)
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

  private def dataFrameFromCSV(
      context: ExecutionContext,
      rdd: RDD[String],
      categoricalColumnsSelection: Option[MultipleColumnSelection]): DataFrame = {

    val namesIncluded = csvNamesIncludedParameter.value.get
    val lines = splitLinesIntoColumns(rdd, csvColumnSeparatorParameter.value.get).cache()

    val (columnNames, dataLines) = if (namesIncluded) {
      val processedFirstLine = lines.first().map(removeQuotes).map(sanitizeColumnName)
      (processedFirstLine, skipFirstLine(lines).cache())
    } else {
      (generateColumnNames(columnsNo = lines.first().length), lines)
    }

    val linesInferences = dataLines.map(_.map(cellTypeInference))

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

  override val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

trait ReadDataFrameParameters {
  import ReadDataFrame._

  val csvColumnSeparatorParameter = StringParameter(
    "Column separator",
    default = Some(","),
    required = true,
    validator = new AcceptAllRegexValidator)

  val csvNamesIncludedParameter = BooleanParameter(
    "Does the first row include column names?",
    default = Some(true),
    required = true)

  val csvCategoricalColumnsParameter = ColumnSelectorParameter(
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
        "categorical columns" -> csvCategoricalColumnsParameter)))

  val pathParameter = StringParameter(
    "Path",
    default = None,
    required = true,
    validator = new AcceptAllRegexValidator)

  val sourceParameter = ChoiceParameter(
    "Source of the DataFrame",
    default = None,
    required = true,
    options = ListMap(
      FileSource.LOCAL.toString -> ParametersSchema(
        "path" -> pathParameter)))

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

  object FileFormat extends Enumeration {
    val CSV = Value("CSV")
  }

  object FileSource extends Enumeration {
    val LOCAL = Value("local")
  }

  object LineSeparator extends Enumeration {
    val WINDOWS = Value("Windows line separator")
    val UNIX = Value("Unix line separator")
    val CUSTOM = Value("Custom line separator")
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

  private def cellTypeInference(cell: String): TypeInference = {
    val trimmedCell = cell.trim
    if (trimmedCell.isEmpty) {
      return TypeInference(canBeBoolean = true, canBeNumeric = true, canBeTimestamp = true)
    }

    try {
      val d = trimmedCell.toDouble
      TypeInference(canBeBoolean = d == 0 || d == 1, canBeNumeric = true, canBeTimestamp = false)
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
        case types.TimestampType => DateTimeConverter.parseDateTime(trimmedCell)
      }
    }
  }

}
