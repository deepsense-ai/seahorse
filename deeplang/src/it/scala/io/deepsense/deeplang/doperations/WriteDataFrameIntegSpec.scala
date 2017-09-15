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

import java.io.File

import scala.io.Source

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructField, StructType}
import org.scalatest.BeforeAndAfter

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoriesMapping, MappingMetadataConverter}
import io.deepsense.deeplang.doperations.WriteDataFrame.{CSV, FILE}
import io.deepsense.deeplang.parameters.ColumnType

class WriteDataFrameIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter {

  val testDataDir = testsDir + "/WriteDataFrameTest"

  val absoluteTestDataPath = new java.io.File(testDataDir).getAbsoluteFile.toString

  val timestamp = DateTimeConverter.now

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

  val q = "\""

  val rowsAsCsv = (sep: String) => Seq(
    s"1${sep}A${sep}0.45${sep}3.14${sep}${DateTimeConverter.toString(timestamp)}",
    s"0${sep}B${sep}${sep}${q}${q}${q}testing...${q}${q}${q}${sep}",
    s"0${sep}C${sep}3.14159${sep}${q}Hello, world!${q}${sep}"
      + s"${DateTimeConverter.toString(timestamp)}",
    s"${sep}${sep}${sep}${sep}"
  )

  val dataframe = createDataFrame(rows, schema)

  before {
    fileSystemClient.delete(testsDir)
    new java.io.File(testsDir + "/id").getParentFile.mkdirs()
    executionContext.fsClient.copyLocalFile(getClass.getResource("/csv/").getPath, testsDir)
  }

  "WriteDataFrame" should {
    "write CSV file without header" in {
      WriteDataFrame(CSV, ",", false, FILE, absoluteTestDataPath + "/without-header")
        .execute(executionContext)(Vector(dataframe))

      verifySavedDataFrame("/without-header", rows, false)
    }

    "write CSV file with header" in {
      WriteDataFrame(CSV, ",", true, FILE, absoluteTestDataPath + "/with-header")
        .execute(executionContext)(Vector(dataframe))
      verifySavedDataFrame("/with-header", rows, true)
    }

    "write CSV file with custom separator" in {
      WriteDataFrame(CSV, ";", false, FILE, absoluteTestDataPath + "/custom-separator")
        .execute(executionContext)(Vector(dataframe))

      verifySavedDataFrame("/custom-separator", rows, false, ";")
    }
  }

  private def verifySavedDataFrame(savedFile: String, rows: Seq[Row],
     withHeader: Boolean, separator: String = ",") {

    implicit def bool2int(b: Boolean) = if (b) 1 else 0

    val parts = new File(absoluteTestDataPath + savedFile).listFiles
      .map(_.getName).filter(_.startsWith("part-")).sorted
    val lines = parts.map(part => Source.fromFile(absoluteTestDataPath + savedFile + "/" + part)
      .getLines()).flatMap(x => x.toArray[String])

    if (withHeader) {
      lines(0) shouldBe schema.fieldNames
        .mkString(s"${separator}")
    }
    for (idx <- 0 until rows.length) {
      lines(idx + withHeader) shouldBe rowsAsCsv(separator)(idx)
    }
  }
}
