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

import java.sql.Timestamp

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.file.File
import io.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException
import io.deepsense.deeplang.parameters.{IndexColumnSelection, MultipleColumnSelection, NameColumnSelection}

class FileToDataFrameIntegSpec extends DeeplangIntegTestSupport {

  val separator = ","
  val categoricalName = "category"
  val categoricalDoubleName = "Id"
  val categoricalId = 3
  val categoricalDoubleId = 0

  "FileToDataFrame" should {
    "infer columns types" in {
      val (file, expectedDataFrame) = fixture(namedColumns = false, mappedCategory = false)
      val dataFrame = executeFileToDataFrame(
        namesIncluded = false,
        separator,
        file,
        "CSV",
        None)
      assertDataFramesEqual(dataFrame, expectedDataFrame)
    }
    "categorize selected columns by index" in {
      val (file, expectedDataFrame) = fixture(namedColumns = false, mappedCategory = true)
      val dataFrame = executeFileToDataFrame(
        namesIncluded = false,
        separator,
        file,
        "CSV",
        Some(MultipleColumnSelection(Vector(
          IndexColumnSelection(Set(categoricalId, categoricalDoubleId))))
        )
      )
      assertDataFramesEqual(dataFrame, expectedDataFrame)
    }
    "categorize selected columns by name" in {
      val (file, expectedDataFrame) = fixture(namedColumns = true, mappedCategory = true)
      val dataFrame = executeFileToDataFrame(
        namesIncluded = true,
        separator,
        file,
        "CSV",
        Some(MultipleColumnSelection(Vector(
          NameColumnSelection(Set(categoricalName, categoricalDoubleName)))
        ))
      )

      assertDataFramesEqual(dataFrame, expectedDataFrame)
    }
    "name columns using names from the first row" in {
      val (file, expectedDataFrame) = fixture(namedColumns = true, mappedCategory = false)
      val dataFrame = executeFileToDataFrame(
        namesIncluded = true,
        separator,
        file,
        "CSV",
        None)

      assertDataFramesEqual(dataFrame, expectedDataFrame)
    }
    "fail" when {
      "non-existing columns are selected as categoricals" in {
        val (file, _) = fixture(namedColumns = false, mappedCategory = true)
        a[ColumnsDoNotExistException] should be thrownBy executeFileToDataFrame(
          namesIncluded = true,
          separator,
          file,
          "CSV",
          Some(MultipleColumnSelection(Vector(
            NameColumnSelection(Set("non-existing column name")))
          ))
        )
      }
    }
  }

  private def executeFileToDataFrame(
      namesIncluded: Boolean,
      separator: String,
      file: File,
      fileTypeName: String,
      categoricalColumnsSelection: Option[MultipleColumnSelection]): DataFrame = {
    val operation = new FileToDataFrame

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

    categoricalColumnsParam.value = categoricalColumnsSelection

    operation.execute(executionContext)(Vector(file)).head.asInstanceOf[DataFrame]
  }

  private def linesToFile(lines: Seq[String]): File = {
    val linesRdd = sqlContext.sparkContext.parallelize(lines)
    new File(Some(linesRdd), Some(Map()))
  }

  private def generateName(i: Int) = s"column_$i"

  val namesFromFile = Seq(
    categoricalDoubleName, q("Msg"), "Val", categoricalName,
    q("Enabled"), "Null.Only", q(q("SomeDate")), "Mixed")

  val expectedNamesFromFile = Seq(
    categoricalDoubleName, "Msg", "Val", categoricalName,
    "Enabled", "Null.Only", q("SomeDate"), "Mixed")

  val columnsIndices = 0 until namesFromFile.size
  val namesGenerated = columnsIndices.map(generateName)

  val datetimes = Seq.fill(6)(DateTimeConverter.now)
  val timestamps = datetimes.map(dt => new Timestamp(dt.getMillis))
  val strTimestamps = datetimes.map(DateTimeConverter.toString)

  val lines = Seq(
    Seq("",    "a test A",  "1.1",  "CAT1", "1", "", strTimestamps(0),         "1"),
    Seq(" 2 ", "",          "1.2",  "CAT2", "1", "", s"${strTimestamps(1)} ",  "2.2"),
    Seq(" 3",  q("oh,no"),  "",     "CAT3", "0", "", "",                       strTimestamps(5)),
    Seq("4 ",  " test C  ", q("1"), "",     "1", "", s" ${strTimestamps(2)}",  ""),
    Seq("5.4", "a test D ", "1"  ,  "CAT2", "",  "", strTimestamps(3),         q(q("x"))),
    Seq("6",   q(" t E"),   "1.6",  "CAT3", "0", "", s" ${strTimestamps(4)} ", "0")
  )
  val rows = Seq(
    Row(null, "a test A", "1.1", "CAT1", true,  null, timestamps(0), "1"),
    Row(2.0,  "",         "1.2", "CAT2", true,  null, timestamps(1), "2.2"),
    Row(3.0,  "oh,no",    "",    "CAT3", false, null, null,          strTimestamps(5)),
    Row(4.0,  "test C",   "1",   "",     true,  null, timestamps(2), ""),
    Row(5.4,  "a test D", "1",   "CAT2", null,  null, timestamps(3), q("x")),
    Row(6.0,  " t E",     "1.6", "CAT3", false, null, timestamps(4), "0")
  )
  val rowsPreparedToBeCategorized = rows.zip(Seq("", "2", "3", "4", "5.4", "6")).map{
    case (row, elem) => Row.fromSeq(elem +: row.toSeq.tail)
  }

  val namedLines = Seq(namesFromFile) ++ lines
  val types = Seq(
    DoubleType,
    StringType,
    StringType,
    StringType,
    BooleanType,
    BooleanType,
    TimestampType,
    StringType)
  val typesPreparedToBeCategorized = StringType +: types.tail

  private def fixture(namedColumns: Boolean, mappedCategory: Boolean): (File, DataFrame) = {
    val names = if (namedColumns) expectedNamesFromFile else namesGenerated
    val inputData = if (namedColumns) namedLines else lines
    val outputData = if (mappedCategory) rowsPreparedToBeCategorized else rows
    val outputTypes = if (mappedCategory) typesPreparedToBeCategorized else types
    val schema =
      StructType(columnsIndices.map(i => StructField(names(i).replace('.', '_'), outputTypes(i))))
    val categoricalColumns = if (mappedCategory) {
      Seq(names(0), names(3))
    } else {
      Seq.empty
    }
    val expectedDataFrame = createDataFrame(outputData, schema, categoricalColumns)

    val file = linesToFile(inputData.map{ l => l.mkString(separator) })

    (file, expectedDataFrame)
  }

  private def q(s: String): String = "\"" + s + "\""
}
