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

package ai.deepsense.deeplang.doperations.readwritedataframe.filestorage.csv

import org.apache.commons.lang3.StringUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{DataType, StructField, StructType}
import org.apache.spark.sql.{DataFrame => SparkDataFrame, Row, types}

import ai.deepsense.commons.datetime.DateTimeConverter
import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperations.inout.InputFileFormatChoice

/**
  * In CSV there are no type hints/formats. Everything is plain text between separators.
  *
  * Schema immediately after reading is all strings.
  *
  * Logic here looks for the most strict common subtype of all values per each column
  * and converts schema to have more rich types than strings.
  */
object CsvSchemaInferencerAfterReading {

  def postprocess
    (csvChoice: InputFileFormatChoice.Csv)
    (sparkDataFrame: SparkDataFrame)
    (implicit context: ExecutionContext): SparkDataFrame = {

    val rawStringData = sparkDataFrame.rdd.map {
      row => Row.fromSeq(row.toSeq.map(Option(_).getOrElse("")))
    }

    val inferredTypes = inferTypes(csvChoice)(rawStringData)
    val columnNames = determineColumnNames(csvChoice)(sparkDataFrame.schema)
    val schema = buildSchema(columnNames, inferredTypes)

    val convertedData = rawStringData.map(row =>
      Row.fromSeq {
        row.toSeq.zipWithIndex.zip(inferredTypes).map {
          case ((cell: String, index), inferredType) => convertCell(cell, inferredType)
        }
      })

    context.sparkSQLSession.createDataFrame(convertedData, schema)
  }

  private def inferTypes
    (csvChoice: InputFileFormatChoice.Csv)
    (data: RDD[Row]): Seq[DataType] = {

    val shouldConvertToBoolean = csvChoice.getShouldConvertToBoolean

    val linesInferences =
      data.map(_.toSeq.map {
        case cell: String => cellTypeInference(cell, shouldConvertToBoolean)
      })

    linesInferences.reduce { (
      lInf1, lInf2) => lInf1 zip lInf2 map { case (i1, i2) => i1 reduce i2 }
    }.map(_.toType)
  }

  private def determineColumnNames
    (csvChoice: InputFileFormatChoice.Csv)
    (schema: StructType): Seq[String] = {

    val sanitizedNames = schema map { field =>
      // TODO: remove replace when spark upgraded to 1.4. DS-635
      if (csvChoice.getNamesIncluded) {
        field.name.trim.replace(".", "_")
      } else {
        ""
      }
    }

    renameUnnamed(sanitizedNames)
  }

  private def buildSchema(
      columnNames: Seq[String],
      inferredTypes: Seq[DataType]): StructType = {

    StructType(columnNames zip inferredTypes map {
      case (columnName, inferredType) => StructField(columnName, inferredType)
    })
  }

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

  private def convertCell(cell: String, cellType: types.DataType): Any = {
    val trimmedCell = cell.trim

    if (cellType == types.StringType) {
      cell
    } else if (trimmedCell.isEmpty) {
      null
    } else {
      cellType match {
        case types.BooleanType => trimmedCell.toDouble == 1
        case types.DoubleType => trimmedCell.toDouble
        case types.TimestampType => DateTimeConverter.parseTimestamp(trimmedCell)
      }
    }
  }

  private case class TypeInference(
     canBeBoolean: Boolean,
     canBeNumeric: Boolean,
     canBeTimestamp: Boolean
   ) {

    def toType: types.DataType = if (canBeBoolean) {
      types.BooleanType
    } else if (canBeNumeric) {
      types.DoubleType
    } else if (canBeTimestamp) {
      types.TimestampType
    } else {
      types.StringType
    }

    def reduce(other: TypeInference): TypeInference =
      TypeInference(
        canBeBoolean = canBeBoolean && other.canBeBoolean,
        canBeNumeric = canBeNumeric && other.canBeNumeric,
        canBeTimestamp = canBeTimestamp && other.canBeTimestamp)
  }
}
