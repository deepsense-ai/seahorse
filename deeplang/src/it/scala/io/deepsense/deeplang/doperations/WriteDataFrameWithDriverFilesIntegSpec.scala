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

import java.sql.Timestamp

import scala.io.Source

import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.scalatest.BeforeAndAfter

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.doperations.exceptions.UnsupportedColumnTypeException
import io.deepsense.deeplang.doperations.inout._
import io.deepsense.deeplang.doperations.readwritedataframe.{FileScheme, ParquetNotSupported}
import io.deepsense.deeplang.{DKnowledge, DeeplangIntegTestSupport, TestFiles}

class WriteDataFrameWithDriverFilesIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter
  with TestFiles {

  val dateTime = DateTimeConverter.now
  val timestamp = new Timestamp(dateTime.getMillis)

  val schema: StructType = StructType(Seq(
    StructField("boolean", BooleanType),
    StructField("double", DoubleType),
    StructField("string", StringType),
    StructField("timestamp", TimestampType)))

  val rows = Seq(
    Row(true, 0.45, "3.14", timestamp),
    Row(false, null, "\"testing...\"", null),
    Row(false, 3.14159, "Hello, world!", timestamp),
    Row(null, null, null, null)
  )

  val quoteChar = "\""

  def quote(value: String, sep: String): String = {
    def escapeQuotes(x: Any): String =
      s"$x".replace(quoteChar, "\\" + quoteChar)

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
      Seq("1", "0.45", "3.14", DateTimeConverter.toString(dateTime)),
      Seq("0", "", "\"testing...\"", ""),
      Seq("0", "3.14159", "Hello, world!", DateTimeConverter.toString(dateTime)),
      Seq("", "", "", "")
    ).map { row => row.map(quote(_, sep)).mkString(sep) }
    rows
  }

  lazy val dataframe = createDataFrame(rows, schema)

  val arrayDataFrameRows = Seq(
    Row(Seq(1.0, 2.0, 3.0)),
    Row(Seq(4.0, 5.0, 6.0)),
    Row(Seq(7.0, 8.0, 9.0)))

  val arrayDataFrameSchema = StructType(Seq(
    StructField("v", ArrayType(DoubleType, false), false)))

  lazy val arrayDataFrame = createDataFrame(arrayDataFrameRows, arrayDataFrameSchema)

  "WriteDataFrame" should {
    "write CSV file without header" in {
      val wdf =
        new WriteDataFrame()
          .setStorageType(
            OutputStorageTypeChoice.File()
              .setOutputFile(absoluteTestsDirPath.fullPath + "without-header")
              .setFileFormat(
                OutputFileFormatChoice.Csv()
                  .setCsvColumnSeparator(CsvParameters.ColumnSeparatorChoice.Comma())
                  .setCsvNamesIncluded(false)))
      wdf.execute(executionContext)(Vector(dataframe))
      verifySavedDataFrame("without-header", rows, withHeader = false, ",")
    }

    "write CSV file with header" in {
      val wdf =
        new WriteDataFrame()
          .setStorageType(
            OutputStorageTypeChoice.File()
              .setOutputFile(absoluteTestsDirPath.fullPath + "with-header")
              .setFileFormat(
                OutputFileFormatChoice.Csv()
                  .setCsvColumnSeparator(CsvParameters.ColumnSeparatorChoice.Comma())
                  .setCsvNamesIncluded(true)))
      wdf.execute(executionContext)(Vector(dataframe))
      verifySavedDataFrame("with-header", rows, withHeader = true, ",")
    }

    "write CSV file with semicolon separator" in {
      val wdf =
        new WriteDataFrame()
          .setStorageType(
            OutputStorageTypeChoice.File()
              .setOutputFile(absoluteTestsDirPath.fullPath + "semicolon-separator")
              .setFileFormat(
                OutputFileFormatChoice.Csv()
                  .setCsvColumnSeparator(CsvParameters.ColumnSeparatorChoice.Semicolon())
                  .setCsvNamesIncluded(false)))
      wdf.execute(executionContext)(Vector(dataframe))
      verifySavedDataFrame("semicolon-separator", rows, withHeader = false, ";")
    }

    "write CSV file with colon separator" in {
      val wdf =
        new WriteDataFrame()
          .setStorageType(
            OutputStorageTypeChoice.File()
              .setOutputFile(absoluteTestsDirPath.fullPath + "colon-separator")
              .setFileFormat(
                OutputFileFormatChoice.Csv()
                  .setCsvColumnSeparator(CsvParameters.ColumnSeparatorChoice.Colon())
                  .setCsvNamesIncluded(false)))
      wdf.execute(executionContext)(Vector(dataframe))
      verifySavedDataFrame("colon-separator", rows, withHeader = false, ":")
    }

    // This fails due to a bug in Spark-CSV
    "write CSV file with space separator" is pending
//    in {
//      val wdf = WriteDataFrame(
//        columnSep(ColumnSeparator.SPACE),
//        writeHeader = false,
//        absoluteWriteDataFrameTestPath + "space-separator")
//      wdf.execute(executionContext)(Vector(dataframe))
//      verifySavedDataFrame("space-separator", rows, withHeader = false, " ")
//    }

    // This fails due to a bug in Spark-CSV
    "write CSV file with tab separator" is pending
//    in {
//      val wdf = WriteDataFrame(
//        columnSep(ColumnSeparator.TAB),
//        writeHeader = false,
//        absoluteWriteDataFrameTestPath + "tab-separator")
//      wdf.execute(executionContext)(Vector(dataframe))
//      verifySavedDataFrame("tab-separator", rows, withHeader = false, "\t")
//    }

    "write CSV file with custom separator" in {
      val wdf =
        new WriteDataFrame()
          .setStorageType(
            OutputStorageTypeChoice.File()
              .setOutputFile(absoluteTestsDirPath.fullPath + "custom-separator")
              .setFileFormat(
                OutputFileFormatChoice.Csv()
                  .setCsvColumnSeparator(
                    CsvParameters.ColumnSeparatorChoice.Custom()
                      .setCustomColumnSeparator("X"))
                  .setCsvNamesIncluded(false)))
      wdf.execute(executionContext)(Vector(dataframe))
      verifySavedDataFrame("custom-separator", rows, withHeader = false, "X")
    }

    "write ArrayType to Json" in {
      val wdf =
        new WriteDataFrame()
          .setStorageType(
            OutputStorageTypeChoice.File()
              .setOutputFile(absoluteTestsDirPath.fullPath + "json-array")
              .setFileFormat(OutputFileFormatChoice.Json()))
      wdf.execute(executionContext)(Vector(arrayDataFrame))
    }

    "throw an exception when writing ArrayType to CSV" in {
      val wdf =
        new WriteDataFrame()
          .setStorageType(
            OutputStorageTypeChoice.File()
              .setOutputFile(absoluteTestsDirPath.fullPath + "csv-array")
              .setFileFormat(
                OutputFileFormatChoice.Csv()
                  .setCsvColumnSeparator(
                    CsvParameters.ColumnSeparatorChoice.Comma())
                  .setCsvNamesIncluded(false)))
      an [UnsupportedColumnTypeException] shouldBe thrownBy {
        wdf.execute(executionContext)(Vector(arrayDataFrame))
      }
    }

    "throw exception at inference time when using parquet with local driver files" in {
      val wdf = new WriteDataFrame()
        .setStorageType(
          OutputStorageTypeChoice.File()
            .setOutputFile(FileScheme.File.pathPrefix + "/some_path/some_file.parquet")
            .setFileFormat(OutputFileFormatChoice.Parquet()))

      an [ParquetNotSupported.type] shouldBe thrownBy {
        wdf.inferKnowledge(executionContext.inferContext)(
            Vector(DKnowledge(dataframe)))
      }
    }

    "overwrite file when it already exists" in {
      val outputName = "some-name"
      val wdf =
        new WriteDataFrame()
          .setStorageType(
            OutputStorageTypeChoice.File()
              .setOutputFile(absoluteTestsDirPath.fullPath + outputName)
              .setFileFormat(
                OutputFileFormatChoice.Csv()
                  .setCsvColumnSeparator(CsvParameters.ColumnSeparatorChoice.Comma())
                  .setCsvNamesIncluded(false)))
      wdf.execute(executionContext)(Vector(dataframe))

      val wdf1 =
        new WriteDataFrame()
          .setStorageType(
            OutputStorageTypeChoice.File()
              .setOutputFile(absoluteTestsDirPath.fullPath + outputName)
              .setFileFormat(
                OutputFileFormatChoice.Csv()
                  .setCsvColumnSeparator(CsvParameters.ColumnSeparatorChoice.Comma())
                  .setCsvNamesIncluded(true)))
      wdf1.execute(executionContext)(Vector(dataframe))
      verifySavedDataFrame(outputName, rows, withHeader = true, ",")
    }
  }

  private def verifySavedDataFrame(
      savedFile: String,
      rows: Seq[Row],
      withHeader: Boolean,
      separator: String): Unit = {

    val linesFromFile = Source.fromFile(
      absoluteTestsDirPath.pathWithoutScheme + savedFile
    ).getLines().toArray

    val lines = if (withHeader) linesFromFile.tail else linesFromFile

    if (withHeader) {
      val headersLine = linesFromFile.head
      headersLine shouldBe schema.fieldNames.mkString(s"$separator")
    }

    for (idx <- rows.indices) {
      val got = lines(idx)
      val expected = rowsAsCsv(separator)(idx)
      got shouldEqual expected
    }
  }
}
