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

import java.io.File
import java.sql.Timestamp

import scala.io.Source

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructField, StructType}
import org.scalatest.BeforeAndAfter

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoriesMapping, MappingMetadataConverter}
import io.deepsense.deeplang.doperations.inout.CsvParameters.ColumnSeparator
import io.deepsense.deeplang.doperations.inout.CsvParameters.ColumnSeparator._

class WriteDataFrameIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter {

  val absoluteWriteDataFrameTestPath = absoluteTestsDirPath + "/WriteDataFrameTest"

  val dateTime = DateTimeConverter.now
  val timestamp = new Timestamp(dateTime.getMillis)

  val schema: StructType = StructType(Seq(
    StructField("boolean",
      SparkConversions.columnTypeToSparkColumnType(ColumnType.boolean)),
    StructField("categorical",
      SparkConversions.columnTypeToSparkColumnType(ColumnType.categorical),
      metadata = MappingMetadataConverter.mappingToMetadata(CategoriesMapping(Seq("A", "B", "C")))),
    StructField("numeric",
      SparkConversions.columnTypeToSparkColumnType(ColumnType.numeric)),
    StructField("string",
      SparkConversions.columnTypeToSparkColumnType(ColumnType.string)),
    StructField("timestamp",
      SparkConversions.columnTypeToSparkColumnType(ColumnType.timestamp))
  ))

  val rows = Seq(
    Row(true, 0, 0.45, "3.14", timestamp),
    Row(false, 1, null, "\"testing...\"", null),
    Row(false, 2, 3.14159, "Hello, world!", timestamp),
    Row(null, null, null, null, null)
  )

  val quoteChar = "\""

  def quote(value: String, sep: String): String = {
    def escapeQuotes(x: Any): String =
      s"$x".replace(quoteChar, quoteChar + quoteChar)

    def optionallyQuote(x: String): String = {
      if (x.contains(sep) || x.contains(quoteChar)) {
        s"$quoteChar$x$quoteChar"
      } else {
        x
      }
    }

    (escapeQuotes _ andThen optionallyQuote)(value)
  }

  def rowsAsCsv(sep: String): Seq[String] = {
    val rows = Seq(
      Seq("1", "A", "0.45", "3.14", DateTimeConverter.toString(dateTime)),
      Seq("0", "B", "", "\"testing...\"", ""),
      Seq("0", "C", "3.14159", "Hello, world!", DateTimeConverter.toString(dateTime)),
      Seq("", "", "", "", "")
    ).map { row => row.map(quote(_, sep)).mkString(sep) }

    // this is something that Spark-CSV writer does.
    // It's compliant with CSV standard, although unnecessary
    rows.map {
      row => if (row.startsWith(sep)) {
        "\"\"" + row
      } else {
        row
      }
    }
  }

  val dataframe = createDataFrame(rows, schema)

  before {
    fileSystemClient.delete(testsDir)
    new java.io.File(testsDir + "/id").getParentFile.mkdirs()
    executionContext.fsClient.copyLocalFile(getClass.getResource("/csv/").getPath, testsDir)
  }

  "WriteDataFrame" should {
    "write CSV file without header" in {
      val wdf = WriteDataFrame(
        columnSep(ColumnSeparator.COMMA),
        writeHeader = false,
        absoluteWriteDataFrameTestPath + "/without-header")
      wdf.execute(executionContext)(Vector(dataframe))
      verifySavedDataFrame("/without-header", rows, withHeader = false, ",")
    }

    "write CSV file with header" in {
      val wdf = WriteDataFrame(
        columnSep(ColumnSeparator.COMMA),
        writeHeader = true,
        absoluteWriteDataFrameTestPath + "/with-header")
      wdf.execute(executionContext)(Vector(dataframe))
      verifySavedDataFrame("/with-header", rows, withHeader = true, ",")
    }

    "write CSV file with semicolon separator" in {
      val wdf = WriteDataFrame(
        columnSep(ColumnSeparator.SEMICOLON),
        writeHeader = false,
        absoluteWriteDataFrameTestPath + "/semicolon-separator")
      wdf.execute(executionContext)(Vector(dataframe))
      verifySavedDataFrame("/semicolon-separator", rows, withHeader = false, ";")
    }

    "write CSV file with colon separator" in {
      val wdf = WriteDataFrame(
        columnSep(ColumnSeparator.COLON),
        writeHeader = false,
        absoluteWriteDataFrameTestPath + "/colon-separator")
      wdf.execute(executionContext)(Vector(dataframe))
      verifySavedDataFrame("/colon-separator", rows, withHeader = false, ":")
    }

    // This fails due to a bug in Spark-CSV
    "write CSV file with space separator" is pending
//    in {
//      val wdf = WriteDataFrame(
//        columnSep(ColumnSeparator.SPACE),
//        writeHeader = false,
//        absoluteWriteDataFrameTestPath + "/space-separator")
//      wdf.execute(executionContext)(Vector(dataframe))
//      verifySavedDataFrame("/space-separator", rows, withHeader = false, " ")
//    }

    // This fails due to a bug in Spark-CSV
    "write CSV file with tab separator" is pending
//    in {
//      val wdf = WriteDataFrame(
//        columnSep(ColumnSeparator.TAB),
//        writeHeader = false,
//        absoluteWriteDataFrameTestPath + "/tab-separator")
//      wdf.execute(executionContext)(Vector(dataframe))
//      verifySavedDataFrame("/tab-separator", rows, withHeader = false, "\t")
//    }

    "write CSV file with custom separator" in {
      val wdf = WriteDataFrame(
        columnSep(ColumnSeparator.CUSTOM, Some("X")),
        writeHeader = false,
        absoluteWriteDataFrameTestPath + "/custom-separator")
      wdf.execute(executionContext)(Vector(dataframe))
      verifySavedDataFrame("/custom-separator", rows, withHeader = false, "X")
    }
  }

  private def columnSep(
      columnSeparator: ColumnSeparator,
      customSeparator: Option[String] = None): (ColumnSeparator, Option[String]) =
    (columnSeparator, customSeparator)

  private def verifySavedDataFrame(
      savedFile: String,
      rows: Seq[Row],
      withHeader: Boolean,
      separator: String): Unit = {

    def linesFromFile(fileName: String): Array[String] =
      Source.fromFile(absoluteWriteDataFrameTestPath + savedFile + "/" + fileName)
        .getLines()
        .toArray

    val partsLines =
      new File(absoluteWriteDataFrameTestPath + savedFile)
        .listFiles
        .map(_.getName)
        .filter(_.startsWith("part-"))
        .sorted
        .map { partFileName => linesFromFile(partFileName) }

    val lines = partsLines.flatMap { singlePartLines =>
      if (withHeader) singlePartLines.tail else singlePartLines
    }

    if (withHeader) {
      val headers = partsLines.map { _.head }

      for (h <- headers) {
        h shouldBe schema.fieldNames.mkString(s"$separator")
      }
    }

    for (idx <- rows.indices) {
      lines(idx) shouldBe rowsAsCsv(separator)(idx)
    }
  }
}
