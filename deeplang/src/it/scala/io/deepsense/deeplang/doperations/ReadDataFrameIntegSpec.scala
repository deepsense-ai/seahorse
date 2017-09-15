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
import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.ReadDataFrame.LineSeparator.LineSeparator
import io.deepsense.deeplang.doperations.exceptions.InvalidFileException
import io.deepsense.deeplang.parameters.{IndexColumnSelection, MultipleColumnSelection, NameColumnSelection}
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

  val threeStringsSchema =
    schemaWithDefaultColumnNames(Seq(StringType, StringType, StringType))

  val categoricalColumnsContent = Seq(
    Row("CAT1", 1.1),
    Row("CAT2", 3.5),
    Row("CAT1", 5.7)
  )

  "ReadDataFrame" should {
    "read simple csv file with strings" in {
      val dataFrame = readDataFrame(
        fileName = "sample.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX),
        csvColumnSeparator = ",",
        csvNamesIncluded = false,
        csvConvertToBoolean = true)

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(csvContent, threeStringsSchema))
    }

    "read csv with lines separated by Windows CR+LF" in {
      val dataFrame = readDataFrame(
        fileName = "win_sample.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.WINDOWS),
        csvColumnSeparator = ",",
        csvNamesIncluded = false,
        csvConvertToBoolean = true)

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(csvContent, threeStringsSchema))
    }

    "read csv with lines separated by custom separator X" in {
      val dataFrame = readDataFrame(
        fileName = "X_separated.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.CUSTOM, Some("X")),
        csvColumnSeparator = ",",
        csvNamesIncluded = false,
        csvConvertToBoolean = true)

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          Seq(
            Row("x1", "x1", "x1"),
            Row("x2", "x2", "x2"),
            Row("x3", "x3", "x3")),
          threeStringsSchema))
    }

    "not read the csv properly if incorrect line separator is chosen" in {
      val dataFrame = readDataFrame(
        fileName = "sample.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.WINDOWS),
        csvColumnSeparator = ",",
        csvNamesIncluded = false,
        csvConvertToBoolean = true)

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          Seq(Row("a1", "b1", "c1\na2", "b2", "c2\na3", "b3", "c3")),
          schemaWithDefaultColumnNames(types = for (i <- 0 until 7) yield StringType)))
    }

    "read csv with column names" in {
      val dataFrame = readDataFrame(
        fileName = "with_column_names.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX),
        csvColumnSeparator = ",",
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
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX),
        csvColumnSeparator = ",",
        csvNamesIncluded = true,
        csvConvertToBoolean = true)

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          Seq(
            Row(" a2", "b2", "c2"),
            Row(" a3", "b3", "c3")
          ),
          schemaOf(Seq(" a1", "b1", "c1"),
            Seq(StringType, StringType, StringType)))
      )
    }

    "throw on empty csv file" in {
      an[InvalidFileException] should be thrownBy readDataFrame(
        fileName = "empty.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX),
        csvColumnSeparator = ",",
        csvNamesIncluded = false,
        csvConvertToBoolean = true)
    }

    "infer column types with conversion to Boolean" in {
      val dataFrame = readDataFrame(
        fileName = "with_inferable_columns.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX),
        csvColumnSeparator = ",",
        csvNamesIncluded = false,
        csvConvertToBoolean = true
      )

      import DateTimeConverter.{parseTimestamp => toTimestamp}

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          Seq(
            Row(2.0, true, null, "1.1", " hello world ", toTimestamp("2015-08-21T19:40:56.823Z")),
            Row(3.2, false, null, "1.2", "unquoted string", null),
            Row(1.1, true, null, "1", "\"quoted string\"", toTimestamp("2015-08-20T09:40:56.823Z"))
          ),
          schemaWithDefaultColumnNames(
            Seq(DoubleType, BooleanType, BooleanType, StringType, StringType, TimestampType)
          )
        ))
    }

    "infer column types without conversion to Boolean" in {
      val dataFrame = readDataFrame(
        fileName = "with_inferable_columns.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX),
        csvColumnSeparator = ",",
        csvNamesIncluded = false,
        csvConvertToBoolean = false
      )

      import DateTimeConverter.{parseTimestamp => toTimestamp}

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          Seq(
            Row(2.0, 1.0, null, "1.1", " hello world ", toTimestamp("2015-08-21T19:40:56.823Z")),
            Row(3.2, 0.0, null, "1.2", "unquoted string", null),
            Row(1.1, 1.0, null, "1", "\"quoted string\"", toTimestamp("2015-08-20T09:40:56.823Z"))
          ),
          schemaWithDefaultColumnNames(
            Seq(DoubleType, DoubleType, DoubleType, StringType, StringType, TimestampType)
          )
        ))
    }

    "read file so that obtained dataframe can provide report without throwing an exception" in {
      // This test case was introduced because Row allow to put anything into it,
      // disregarding schema. Incorrectly built DataFrames were throwing exceptions later
      // - while generating reports.
      val dataFrame = readDataFrame(
        fileName = "with_inferable_columns.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX),
        csvColumnSeparator = ",",
        csvNamesIncluded = false,
        csvConvertToBoolean = true
      )
      dataFrame.report(executionContext) shouldBe an[Report]
    }

    "read categorical columns provided by index" in {
      val dataFrame = readDataFrame(
        fileName = "with_categorical_columns.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX),
        csvColumnSeparator = ",",
        csvNamesIncluded = true,
        csvConvertToBoolean = true,
        csvCategoricalColumns = Some(MultipleColumnSelection(
          Vector(IndexColumnSelection(Set(0, 1)))
        ))
      )

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          categoricalColumnsContent,
          schemaOf(
            Seq("col1", "col2"),
            Seq(StringType, DoubleType)
          ),
          Seq("col1", "col2"))
      )
    }

    "read categorical columns provided by name" in {
      val dataFrame = readDataFrame(
        fileName = "with_categorical_columns.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX),
        csvColumnSeparator = ",",
        csvNamesIncluded = true,
        csvConvertToBoolean = true,
        csvCategoricalColumns = Some(MultipleColumnSelection(
          Vector(NameColumnSelection(Set("col1", "col2")))
        ))
      )

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          categoricalColumnsContent,
          schemaOf(
            Seq("col1", "col2"),
            Seq(StringType, DoubleType)
          ),
          Seq("col1", "col2"))
      )
    }
  }

  def readDataFrame(
      fileName: String,
      lineSeparator: (LineSeparator, Option[String]),
      csvColumnSeparator: String,
      csvNamesIncluded: Boolean,
      csvConvertToBoolean: Boolean,
      csvCategoricalColumns: Option[MultipleColumnSelection] = None) : DataFrame = {
    val operation = ReadDataFrame(
      absoluteTestsDirPath + "/" + fileName,
      lineSeparator,
      csvColumnSeparator,
      csvNamesIncluded,
      csvConvertToBoolean,
      csvCategoricalColumns)

    operation
      .execute(executionContext)(Vector.empty[DOperable])
      .head
      .asInstanceOf[DataFrame]
  }

  def expectedDataFrame(rows: Seq[Row], schema: StructType) : DataFrame =
    createDataFrame(rows, schema)

  def expectedDataFrame(
      rows: Seq[Row],
      schema: StructType,
      categoricalColumns: Seq[String]) : DataFrame =
    createDataFrame(rows, schema, categoricalColumns)

  def lineSep(
    lineSeparator: LineSeparator,
    customValue: Option[String] = None): (LineSeparator, Option[String]) =
    (lineSeparator, customValue)

  def generatedColumnNames(n: Int): Seq[String] = for (i <- 0 until n) yield s"column_$i"

  def schemaOf(columns: Seq[String], types: Seq[DataType]) = StructType(
    columns.zip(types).map { case (colName, colType) => StructField(colName, colType)})

  def schemaWithDefaultColumnNames(types: Seq[DataType]) : StructType =
    schemaOf(generatedColumnNames(types.length), types)
}
