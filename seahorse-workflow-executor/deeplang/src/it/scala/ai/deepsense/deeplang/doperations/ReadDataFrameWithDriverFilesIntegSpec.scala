/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalatest.BeforeAndAfter

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.report.Report
import ai.deepsense.deeplang.doperations.exceptions.BacktickInColumnNameException
import ai.deepsense.deeplang.doperations.inout.{CsvParameters, InputFileFormatChoice, InputStorageTypeChoice}
import ai.deepsense.deeplang.doperations.readwritedataframe.filestorage.ParquetNotSupported
import ai.deepsense.deeplang.doperations.readwritedataframe.{FileScheme, UnknownFileSchemaForPath}
import ai.deepsense.deeplang.{DOperable, DeeplangIntegTestSupport, TestFiles}

class ReadDataFrameWithDriverFilesIntegSpec
  extends DeeplangIntegTestSupport with BeforeAndAfter with TestFiles {

  import DeeplangIntegTestSupport._

  val csvContent = Seq(
    Row("a1", "b1", "c1"),
    Row("a2", "b2", "c2"),
    Row("a3", "b3", "c3"))

  val csvContentWithEmptyStrings = Seq(
    Row("", "b1", "c1"),
    Row("a2", "", "c2"),
    Row("a3", "b3", ""))

  val threeStringsSchema = schemaWithDefaultColumnNames(Seq(StringType, StringType, StringType))

  "ReadDataFrame" should {
    "read simple csv file with strings" in {
      val dataFrame = readDataFrame(
        fileName = "sample.csv",
        csvColumnSeparator = CsvParameters.ColumnSeparatorChoice.Comma(),
        csvNamesIncluded = false,
        csvConvertToBoolean = true)

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(csvContent, threeStringsSchema))
    }

    "read simple csv file containing empty strings" in {
      val dataFrame = readDataFrame(
        fileName = "sample_with_empty_strings.csv",
        csvColumnSeparator = CsvParameters.ColumnSeparatorChoice.Comma(),
        csvNamesIncluded = false,
        csvConvertToBoolean = true)

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(csvContentWithEmptyStrings, threeStringsSchema))
    }

    "read csv with lines separated by Windows CR+LF" in {
      val dataFrame = readDataFrame(
        fileName = "win_sample.csv",
        csvColumnSeparator = CsvParameters.ColumnSeparatorChoice.Comma(),
        csvNamesIncluded = false,
        csvConvertToBoolean = true)

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(csvContent, threeStringsSchema))
    }

    "read csv with column names" in {
      val dataFrame = readDataFrame(
        fileName = "with_column_names.csv",
        csvColumnSeparator = CsvParameters.ColumnSeparatorChoice.Comma(),
        csvNamesIncluded = true,
        csvConvertToBoolean = true)

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          csvContent,
          schemaOf(
            Seq("column_A", "column_B", "column_C"),
            Seq(StringType, StringType, StringType)
          )
        ))
    }

    "throw on csv with column names containing backticks" in {
      a[BacktickInColumnNameException] should be thrownBy readDataFrame(
        fileName = "with_column_names_containing_backticks.csv",
        csvColumnSeparator = CsvParameters.ColumnSeparatorChoice.Comma(),
        csvNamesIncluded = true,
        csvConvertToBoolean = true)
    }

    "trim white spaces for not quoted column names and values in CSV" in {
      val dataFrame = readDataFrame(
        fileName = "with_white_spaces.csv",
        csvColumnSeparator = CsvParameters.ColumnSeparatorChoice.Comma(),
        csvNamesIncluded = true,
        csvConvertToBoolean = true)

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          Seq(
            Row(" a2", " b2 ", " c2"),
            Row(" a3", " b3 ", " c3")
          ),
          schemaOf(Seq("a1", "b1", "c1"),
            Seq(StringType, StringType, StringType)))
      )
    }

    "throw on empty csv file" in {
      a[RuntimeException] should be thrownBy readDataFrame(
        fileName = "empty.csv",
        csvColumnSeparator = CsvParameters.ColumnSeparatorChoice.Comma(),
        csvNamesIncluded = false,
        csvConvertToBoolean = true)
    }

    "infer column types with conversion to Boolean" in {
      val dataFrame = readDataFrame(
        fileName = "with_convertible_to_boolean.csv",
        csvColumnSeparator = CsvParameters.ColumnSeparatorChoice.Comma(),
        csvNamesIncluded = false,
        csvConvertToBoolean = true
      )

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          Seq(
            Row(true),
            Row(false),
            Row(true)
          ),
          schemaWithDefaultColumnNames(
            Seq(BooleanType)
          )
        ))
    }

    "infer column types without conversion to Boolean" in {
      val dataFrame = readDataFrame(
        fileName = "with_convertible_to_boolean.csv",
        csvColumnSeparator = CsvParameters.ColumnSeparatorChoice.Comma(),
        csvNamesIncluded = false,
        csvConvertToBoolean = false
      )

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          Seq(
            Row(1.0),
            Row(0.0),
            Row(1.0)
          ),
          schemaWithDefaultColumnNames(
            Seq(DoubleType)
          )
        ))
    }

    "read file so that obtained dataframe can provide report without throwing an exception" in {
      // This test case was introduced because Row allow to put anything into it,
      // disregarding schema. Incorrectly built DataFrames were throwing exceptions later
      // - while generating reports.
      val dataFrame = readDataFrame(
        fileName = "with_inferable_columns.csv",
        csvColumnSeparator = CsvParameters.ColumnSeparatorChoice.Comma(),
        csvNamesIncluded = false,
        csvConvertToBoolean = true
      )
      dataFrame.report() shouldBe a[Report]
    }

    "read CSV file with escaped quotes without throwing an exception" in {
      val dataFrame = readDataFrame(
        fileName = "with_escaped_quotes.csv",
        csvColumnSeparator = CsvParameters.ColumnSeparatorChoice.Comma(),
        csvNamesIncluded = true,
        csvConvertToBoolean = false
      )
      dataFrame.sparkDataFrame.count() shouldEqual 2
    }

    "throw exception at inference time when using parquet with local driver files" in {
      val rdf = ReadDataFrame()
        .setStorageType(
          new InputStorageTypeChoice.File()
            .setSourceFile(FileScheme.File.pathPrefix + "/some_path/some_file.parquet")
            .setFileFormat(new InputFileFormatChoice.Parquet()))

      an [ParquetNotSupported.type] shouldBe thrownBy {
        rdf.inferKnowledgeUntyped(Vector())(executionContext.inferContext)
      }
    }

    "throw exception at inference time when using invalid file scheme" in {
      val rdf = ReadDataFrame()
        .setStorageType(
          new InputStorageTypeChoice.File()
            .setSourceFile("invalidscheme://some_path/some_file.json")
            .setFileFormat(new InputFileFormatChoice.Json()))

      an [UnknownFileSchemaForPath] shouldBe thrownBy {
        rdf.inferKnowledgeUntyped(Vector())(executionContext.inferContext)
      }
    }

  }

  def readDataFrame(
      fileName: String,
      csvColumnSeparator: CsvParameters.ColumnSeparatorChoice,
      csvNamesIncluded: Boolean,
      csvConvertToBoolean: Boolean) : DataFrame = {
    ReadDataFrame(
      absoluteTestsDirPath.fullPath + fileName,
      csvColumnSeparator,
      csvNamesIncluded,
      csvConvertToBoolean
    ).executeUntyped(Vector.empty[DOperable])(executionContext)
      .head
      .asInstanceOf[DataFrame]
  }

  def expectedDataFrame(rows: Seq[Row], schema: StructType) : DataFrame =
    createDataFrame(rows, schema)

  def generatedColumnNames(n: Int): Seq[String] = for (i <- 0 until n) yield s"unnamed_$i"

  def schemaOf(columns: Seq[String], types: Seq[DataType]): StructType = StructType(
    columns.zip(types).map { case (colName, colType) => StructField(colName, colType)})

  def schemaWithDefaultColumnNames(types: Seq[DataType]): StructType =
    schemaOf(generatedColumnNames(types.length), types)
}
