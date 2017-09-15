/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import scala.collection.immutable.ListMap
import scala.reflect.ClassTag

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Row, types}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import io.deepsense.deeplang.doperables.file.File
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}

case class FileToDataFrame() extends DOperation1To1[File, DataFrame] {
  import io.deepsense.deeplang.doperations.FileToDataFrame._
  override val name: String = "File To DataFrame"
  override val id: Id = "83bad450-f87c-11e4-b939-0800200c9a66"
  override val parameters: ParametersSchema = ParametersSchema(
    formatParameter -> ChoiceParameter(
      "Format of the input file",
      Some(CSV.name),
      required = true,
      ListMap(CSV.name -> ParametersSchema(
        separatorParameter -> StringParameter(
          "Column separator",
          Some(","),
          required = true,
          new AcceptAllRegexValidator
        ),
        namesIncludedParameter -> BooleanParameter(
          "Does the first row include column names?",
          Some(true),
          required = true
        )
      ))),
    categoricalColumnsParameter -> ColumnSelectorParameter(
      "Categorical columns in the input File",
      required = false,
      portIndex = 0
    )
  )

  override protected def _execute(context: ExecutionContext)(file: File): DataFrame = {
    val categoricalColumnsSelection = parameters.getColumnSelection(categoricalColumnsParameter)
    val formatChoice = parameters.getChoice(formatParameter).get
    FileType.forName(formatChoice.label) match {
      case CSV => dataFrameFromCSV(
        context, file, formatChoice.selectedSchema, categoricalColumnsSelection)
    }
  }

  private def dataFrameFromCSV(
      context: ExecutionContext,
      file: File,
      params: ParametersSchema,
      categoricalColumnsSelection: Option[MultipleColumnSelection]): DataFrame = {

    val separator = params.getString(separatorParameter).get
    val namesIncluded = params.getBoolean(namesIncludedParameter).get
    val lines = splitLines(file.rdd.get, separator).cache()
    val firstLine = lines.first()
    val columnsNo = firstLine.length
    val (columnNames, dataLines) = if (namesIncluded) {
      val processedFirstLine = firstLine.map(removeQuotes).map(safeColumnName)
      (processedFirstLine, removeFirstLine(lines).cache())
    } else {
      (generateColumnNames(columnsNo), lines)
    }

    val linesInferences = dataLines.map(_.map(cellTypeInference))

    val inferredTypes = linesInferences.reduce{(lInf1, lInf2) =>
      lInf1.zip(lInf2).map(reduceTypeInferences)
    }.map(_.toType)

    val schema = StructType(columnNames.zip(inferredTypes).map { case (columnName, inferredType) =>
      StructField(columnName, inferredType)
    })

    val (categoricalColumnIndices, categoricalColumnNames) =
      getCategoricalColumns(schema, columnNames, categoricalColumnsSelection)

    val convertedData = dataLines.map(splitLine => Row.fromSeq(
      splitLine.zipWithIndex.zip(inferredTypes).map {
        case ((cell, index), inferredType) =>
          convertCell(
            cell,
            inferredType,
            willBeCategorical = categoricalColumnIndices.contains(index))
      }
    ))

    val convertedSchema = StructType(schema.zipWithIndex.map { case (column, index) =>
      if (categoricalColumnIndices.contains(index)) column.copy(dataType = StringType) else column
    })
    context.dataFrameBuilder.buildDataFrame(convertedSchema, convertedData, categoricalColumnNames)
  }

  // TODO: remove replace when spark upgraded to 1.4. DS-635
  private def safeColumnName(name: String): String = name.replace(".", "_")

  /**
   * Designates indices and names of columns selected to be categorized.
   * @return Tuple including set of indices and sequence of names.
   */
  private def getCategoricalColumns(
      schema: StructType,
      columnNames: Seq[String],
      categoricalColumnsSelection: Option[MultipleColumnSelection]): (Set[Int], Seq[String]) = {
    categoricalColumnsSelection match {
      case Some(selection) =>
        val categoricalColumnNames = DataFrameColumnsGetter.getColumnNames(schema, selection)
        val categoricalColumnNamesSet = categoricalColumnNames.toSet
        val categoricalColumnIndices = (for {
          (columnName, index) <- columnNames.zipWithIndex
          if categoricalColumnNamesSet.contains(columnName)
        } yield index).toSet
        (categoricalColumnIndices, categoricalColumnNames)
      case None => (Set.empty, Seq.empty)
    }
  }

  /**
   * Splits string lines by separator, but escapes separator within double quotes.
   * For example, line "a,b,\"x,y,z\"" will be split into ["a","b","\"x,y,z\""].
   */
  private def splitLines(lines: RDD[String], separator: String) = {
    lines.map{ line => line.split(separator + """(?=([^"]*"[^"]*")*[^"]*$)""".r, -1).toSeq }
  }

  private def removeFirstLine[T : ClassTag](rdd: RDD[T]): RDD[T] =
    rdd.mapPartitionsWithIndex { (i, iterator) =>
      if (i == 0 && iterator.hasNext) {
        iterator.next()
      }
      iterator
    }

  private def generateColumnNames(columnsNo: Int): Seq[String] = {
    (0 until columnsNo).map(i => s"column_$i")
  }

  private def cellTypeInference(cell: String): TypeInference = {
    val trimmedCell = cell.trim
    if (trimmedCell == "") {
      return TypeInference(canBeBoolean = true, canBeNumeric = true, canBeTimestamp = true)
    }
    try {
      val d = trimmedCell.toDouble
      TypeInference(
        canBeBoolean = d == 0 || d == 1,
        canBeNumeric = true,
        canBeTimestamp = false)
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

  private def reduceTypeInferences(inferences: (TypeInference, TypeInference)): TypeInference =
    TypeInference(
      canBeBoolean = inferences._1.canBeBoolean && inferences._2.canBeBoolean,
      canBeNumeric = inferences._1.canBeNumeric && inferences._2.canBeNumeric,
      canBeTimestamp = inferences._1.canBeTimestamp && inferences._2.canBeTimestamp)

  private def convertCell(
      cell: String,
      targetType: types.DataType,
      willBeCategorical: Boolean): Any = {

    val trimmedCell = cell.trim
    if (targetType == types.StringType || willBeCategorical) {
      removeQuotes(trimmedCell)
    } else if (trimmedCell == "") {
      null
    } else {
      targetType match {
        case types.BooleanType => trimmedCell.toDouble == 1
        case types.DoubleType => trimmedCell.toDouble
        case types.TimestampType => DateTimeConverter.parseDateTime(trimmedCell)
      }
    }
  }

  /**
   * Removes double quotes from front and end (if any).
   */
  private def removeQuotes(s: String): String = s.replaceAll("^\"|\"$", "")
}

object FileToDataFrame {
  val formatParameter = "format"
  val separatorParameter = "separator"
  val categoricalColumnsParameter = "categorical columns"
  val namesIncludedParameter = "names included"

  sealed abstract class FileType(val name: String)

  object FileType {
    def forName(n: String): FileType = n.toLowerCase match {
      case "csv" => CSV
      case _ => ???
    }
  }

  case object CSV extends FileType("CSV")

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

  def apply(
      fileType: FileType,
      columnSeparator: String,
      namesIncluded: Boolean = false,
      categoricalNames: Set[String] = Set.empty,
      categoricalIds: Set[Int] = Set.empty): FileToDataFrame = {
    val fileToDataFrame = new FileToDataFrame
    val params = fileToDataFrame.parameters
    val formatParam = params.getChoiceParameter(formatParameter)
    formatParam.value = Option(fileType.toString)
    val formatOptions = formatParam.options(fileType.toString)
    formatOptions.getStringParameter(separatorParameter).value = Some(columnSeparator)
    formatOptions.getBooleanParameter(namesIncludedParameter).value = Some(namesIncluded)
    params.getColumnSelectorParameter(categoricalColumnsParameter)
      .value = Some(MultipleColumnSelection(Vector(
        NameColumnSelection(categoricalNames),
        IndexColumnSelection(categoricalIds)
    )))
    fileToDataFrame
  }
}
