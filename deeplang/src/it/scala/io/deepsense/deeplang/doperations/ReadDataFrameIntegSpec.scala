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

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.doperables.dataframe.DataFrame
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
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX.toString),
        csvColumnSeparator = ",",
        csvNamesIncluded = false)

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(csvContent, threeStringsSchema))
    }

    "read csv with lines separated by Windows CR+LF" in {
      val dataFrame = readDataFrame(
        fileName = "win_sample.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.WINDOWS.toString),
        csvColumnSeparator = ",",
        csvNamesIncluded = false)

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(csvContent, threeStringsSchema))
    }

    "read csv with lines separated by custom separator X" in {
      val dataFrame = readDataFrame(
        fileName = "X_separated.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.CUSTOM.toString, Some("X")),
        csvColumnSeparator = ",",
        csvNamesIncluded = false)

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
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.WINDOWS.toString),
        csvColumnSeparator = ",",
        csvNamesIncluded = false)

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          Seq(Row("a1", "b1", "c1\na2", "b2", "c2\na3", "b3", "c3")),
          schemaWithDefaultColumnNames(types = for (i <- 0 until 7) yield StringType)))
    }

    "read csv with column names" in {
      val dataFrame = readDataFrame(
        fileName = "with_column_names.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX.toString),
        csvColumnSeparator = ",",
        csvNamesIncluded = true)

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

    "throw on empty csv file" in {
      an[InvalidFileException] should be thrownBy readDataFrame(
        fileName = "empty.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX.toString),
        csvColumnSeparator = ",",
        csvNamesIncluded = false)
    }

    "infer column types" in {
      val dataFrame = readDataFrame(
        fileName = "with_inferable_columns.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX.toString),
        csvColumnSeparator = ",",
        csvNamesIncluded = false
      )

      assertDataFramesEqual(
        dataFrame,
        expectedDataFrame(
          Seq(
            Row(2.0, true, null, "1.1", " hello world ", asTimeStamp("2015-08-21T19:40:56.823Z")),
            Row(3.2, false, null, "1.2", "unquoted string", null),
            Row(1.1, true, null, "1", "\"quoted string\"", asTimeStamp("2015-08-20T09:40:56.823Z"))
          ),
          schemaWithDefaultColumnNames(
            Seq(DoubleType, BooleanType, BooleanType, StringType, StringType, TimestampType)
          )
        ))
    }

    "read categorical columns provided by index" in {
      val dataFrame = readDataFrame(
        fileName = "with_categorical_columns.csv",
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX.toString),
        csvColumnSeparator = ",",
        csvNamesIncluded = true,
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
        lineSeparator = lineSep(ReadDataFrame.LineSeparator.UNIX.toString),
        csvColumnSeparator = ",",
        csvNamesIncluded = true,
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
      lineSeparator: (String, Option[String]),
      csvColumnSeparator: String,
      csvNamesIncluded: Boolean,
      csvCategoricalColumns: Option[MultipleColumnSelection] = None) : DataFrame = {
    val operation = new ReadDataFrame()

    operation.pathParameter.value = Some(testsDir + "/" + fileName)
    operation.formatParameter.value = Some("CSV")
    operation.lineSeparatorParameter.value = Some(lineSeparator._1)
    if (lineSeparator._2.isDefined) {
      operation.customLineSeparatorParameter.value = lineSeparator._2
    }
    operation.csvColumnSeparatorParameter.value = Some(csvColumnSeparator)
    operation.csvNamesIncludedParameter.value = Some(csvNamesIncluded)
    operation.csvCategoricalColumnsParameter.value = csvCategoricalColumns

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

  def lineSep(label: String, customValue: Option[String] = None): (String, Option[String]) =
    (label, customValue)

  def generatedColumnNames(n: Int): Seq[String] = for (i <- 0 until n) yield s"column_$i"

  def schemaOf(columns: Seq[String], types: Seq[DataType]) = StructType(
    columns.zip(types).map { case (colName, colType) => StructField(colName, colType)})

  def schemaWithDefaultColumnNames(types: Seq[DataType]) : StructType =
    schemaOf(generatedColumnNames(types.length), types)

  def asTimeStamp(s: String) : DateTime = DateTimeConverter.parseDateTime(s)
}
