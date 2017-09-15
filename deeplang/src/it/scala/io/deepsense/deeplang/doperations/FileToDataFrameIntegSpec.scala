/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import java.sql.Timestamp

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.Ignore

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.file.File
import io.deepsense.deeplang.doperations.FileToDataFrame.CSV
import io.deepsense.deeplang.parameters.{IndexColumnSelection, MultipleColumnSelection, NameColumnSelection}

@Ignore
class FileToDataFrameIntegSpec extends DeeplangIntegTestSupport {

  val separator = ","
  val categoricalName = "category"
  val categoricalId = 3
  val categoricalDoubleId = 2

  "FileToDataFrame" should {
    "infer columns types" in {
      val (file, expectedDataFrame) = fixture(namedColumns = false, mappedCategory = false)
      val dataFrame =
        executeFileToDataFrame(namesIncluded = false, separator, file, Set.empty, Set.empty)
      assertDataFramesEqual(dataFrame, expectedDataFrame)
    }
    "categorize selected columns by index" in {
      val (file, expectedDataFrame) = fixture(namedColumns = false, mappedCategory = true)
      val dataFrame = executeFileToDataFrame(
        namesIncluded = false,
        separator,
        file,
        Set.empty,
        Set(categoricalId, categoricalDoubleId))
      assertDataFramesEqual(dataFrame, expectedDataFrame)
    }
    "categorize selected columns by name" in {
      val (file, expectedDataFrame) = fixture(namedColumns = true, mappedCategory = true)
      val dataFrame = executeFileToDataFrame(
        namesIncluded = true,
        separator,
        file,
        Set(categoricalName),
        Set.empty)

      assertDataFramesEqual(dataFrame, expectedDataFrame)
    }
    "name columns using names from the first row" in {
      val (file, expectedDataFrame) = fixture(namedColumns = true, mappedCategory = false)
      val dataFrame = executeFileToDataFrame(
        namesIncluded = true,
        separator,
        file,
        Set.empty,
        Set.empty)

      assertDataFramesEqual(dataFrame, expectedDataFrame)
    }
    "fail when file type is different than CSV" in {
      val file: File = emptyFile
      an[NotImplementedError] should be thrownBy executeFileToDataFrame(
        namesIncluded = true,
        separator,
        file,
        Set.empty,
        Set.empty)
    }
  }

  private def executeFileToDataFrame(
      namesIncluded: Boolean,
      separator: String,
      file: File,
      categoricalNames: Set[String],
      categoricalIds: Set[Int]): DataFrame = {
    val operation = new FileToDataFrame
    val fileTypeName = CSV.name

    val formatParameter = operation.parameters.getChoiceParameter(FileToDataFrame.formatParameter)
    formatParameter.value = Some(fileTypeName)
    val fileTypeParameters = formatParameter.options(fileTypeName)
    fileTypeParameters
      .getStringParameter(FileToDataFrame.separatorParameter)
      .value = Some(separator)
    fileTypeParameters
      .getBooleanParameter(FileToDataFrame.namesIncludedParameter)
      .value = Some(namesIncluded)

    val categoricalColumnsParam = operation.parameters
      .getColumnSelectorParameter(FileToDataFrame.categoricalColumnsParameter)

    categoricalColumnsParam.value = Some(MultipleColumnSelection(Vector(
      NameColumnSelection(categoricalNames),
      IndexColumnSelection(categoricalIds))))

    operation.execute(executionContext)(Vector(file)).head.asInstanceOf[DataFrame]
  }

  private def linesToFile(lines: Seq[String]): File = {
    val linesRdd = sqlContext.sparkContext.parallelize(lines)
    new File(Some(linesRdd), Some(Map()))
  }

  private def emptyFile: File = linesToFile(Seq.empty)
  private def generateName(i: Int) = s"column_$i"

  val categories = Map("CAT1" -> 0, "CAT2" -> 1, "CAT3" -> 2)
  val doubleCategories = Map("1.2" -> 1, "1.1" -> 0, "1.5" -> 4, "1.4" -> 2, "1.0" -> 3)

  val namesFromFile =
    Seq("Id", "Msg", "Val", categoricalName, "Enabled", "NullOnly", "SomeDate", "Mixed")
  val columnsIndices = 0 until namesFromFile.size
  val namesGenerated = columnsIndices.map(generateName)

  val rows = Seq(
    Row(Seq(null, "This is a testA", 1.1,  "CAT1", false, null, generateTimestamp, 1)),
    Row(Seq(2,    null,              1.2,  "CAT2", true,  null, generateTimestamp, 2.2)),
    Row(Seq(3,    "This is a testB", null, "CAT3", false, null, null, generateTimestamp)),
    Row(Seq(4,    "This is a testC", 1.4,  null,   true,  null, generateTimestamp, null)),
    Row(Seq(5,    "This is a testD", 1  ,  "CAT2", null,  null, generateTimestamp, "4.5")),
    Row(Seq(6,    "This is a testE", 1.6,  "CAT3", true,  null, generateTimestamp, true))
  )
  val namedRows = Seq(Row(namesFromFile)) ++ rows
  val typesWithoutCategorical = Seq(
    DoubleType,
    StringType,
    DoubleType,
    StringType,
    BooleanType,
    StringType,
    TimestampType,
    StringType)
  val typesWithCategorical = Seq(
    DoubleType,
    StringType,
    IntegerType,
    IntegerType, // String mapped to int.
    BooleanType,
    StringType,
    TimestampType,
    StringType)

  val mappedRows = rows.map { r =>
    val category = r.getString(categoricalId)
    val categoryMapped = if (category == null) null else categories(category)

    val value = r.getDouble(categoricalDoubleId).toString
    val valueMapped = if (value == null) null else doubleCategories(value)

    val rowValues = r.toSeq
      .updated(categoricalId, categoryMapped)
      .updated(categoricalDoubleId, valueMapped)
    Row(rowValues: _*)
  }

  private def fixture(namedColumns: Boolean, mappedCategory: Boolean) = {
    val names = if (namedColumns) namesFromFile else namesGenerated
    val types = if (mappedCategory) typesWithCategorical else typesWithoutCategorical
    val data = if (mappedCategory) mappedRows else rows

    val schema = StructType(columnsIndices.map(i => StructField(names(i), types(i))))
    val expectedDataFrame = createDataFrame(data, schema)

    val lines = data.map(rowToCSV)
    val file = linesToFile(lines)

    (file, expectedDataFrame)
  }

  private def generateTimestamp: Timestamp = new Timestamp(DateTime.now.getMillis)

  private def rowToCSV(row: Row): String = {
    row.toSeq.map {
      case s: String => quote(s)
      case timestamp: Timestamp =>
        val datetime = DateTimeConverter.fromMillis(timestamp.getTime)
        quote(DateTimeConverter.toString(datetime))
      case b: Boolean => if (b) 1 else 0
      case x => x
    }.map(Option(_).getOrElse("").toString)
      .mkString(separator)
  }

  private def quote(s: String): String = "\"" + s + "\""
}
