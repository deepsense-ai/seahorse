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

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalatest.BeforeAndAfter

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.report.Report
import io.deepsense.deeplang.doperations.inout.CsvParameters
import io.deepsense.deeplang.{DOperable, DeeplangIntegTestSupport}

class ReadDataFrameIntegSpec extends DeeplangIntegTestSupport with BeforeAndAfter {

  before {
    fileSystemClient.delete(testsDir)
    new java.io.File(testsDir + "/id").getParentFile.mkdirs()
    executionContext.fsClient.copyLocalFile(getClass.getResource("/csv/").getPath, testsDir)
  }

  val csvContent = Seq(
    Row("a1", "b1", "c1"),
    Row("a2", "b2", "c2"),
    Row("a3", "b3", "c3"))

  val csvContentWithEmptyStrings = Seq(
    Row("", "b1", "c1"),
    Row("a2", "", "c2"),
    Row("a3", "b3", ""))

  val threeStringsSchema =
    schemaWithDefaultColumnNames(Seq(StringType, StringType, StringType))

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
        fileName = "with_inferable_columns.csv",
        csvColumnSeparator = CsvParameters.ColumnSeparatorChoice.Comma(),
        csvNamesIncluded = false,
        csvConvertToBoolean = true
      )

      import DateTimeConverter.{parseTimestamp => toTimestamp}

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          Seq(
            Row(2.0, true, null, 1.1, " hello,\\ world ", toTimestamp("2015-08-21T19:40:56.823Z")),
            Row(3.2, false, null, 1.2, " unquoted string", null),
            Row(1.1, true, null, 1.0, "\"quoted string\"", toTimestamp("2015-08-20T09:40:56.823Z"))
          ),
          schemaWithDefaultColumnNames(
            Seq(DoubleType, BooleanType, BooleanType, DoubleType, StringType, TimestampType)
          )
        ))
    }

    "infer column types without conversion to Boolean" in {
      val dataFrame = readDataFrame(
        fileName = "with_inferable_columns.csv",
        csvColumnSeparator = CsvParameters.ColumnSeparatorChoice.Comma(),
        csvNamesIncluded = false,
        csvConvertToBoolean = false
      )

      import DateTimeConverter.{parseTimestamp => toTimestamp}

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          Seq(
            Row(2.0, 1.0, null, 1.1, " hello,\\ world ", toTimestamp("2015-08-21T19:40:56.823Z")),
            Row(3.2, 0.0, null, 1.2, " unquoted string", null),
            Row(1.1, 1.0, null, 1.0, "\"quoted string\"", toTimestamp("2015-08-20T09:40:56.823Z"))
          ),
          schemaWithDefaultColumnNames(
            Seq(DoubleType, DoubleType, DoubleType, DoubleType, StringType, TimestampType)
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
      dataFrame.report shouldBe an[Report]
    }
  }

  def readDataFrame(
      fileName: String,
      csvColumnSeparator: CsvParameters.ColumnSeparatorChoice,
      csvNamesIncluded: Boolean,
      csvConvertToBoolean: Boolean) : DataFrame = {
    ReadDataFrame(
      absoluteTestsDirPath + "/" + fileName,
      csvColumnSeparator,
      csvNamesIncluded,
      csvConvertToBoolean
    ).execute(executionContext)(Vector.empty[DOperable])
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
