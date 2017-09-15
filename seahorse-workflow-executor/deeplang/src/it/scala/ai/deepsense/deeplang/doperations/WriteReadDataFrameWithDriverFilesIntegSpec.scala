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

import java.sql.Timestamp

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalatest.BeforeAndAfter

import ai.deepsense.deeplang.{TestFiles, DeeplangIntegTestSupport}
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.inout._

class WriteReadDataFrameWithDriverFilesIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter with TestFiles {

  import DeeplangIntegTestSupport._

  val schema: StructType =
    StructType(Seq(
      StructField("boolean", BooleanType),
      StructField("double", DoubleType),
      StructField("string", StringType)
    ))

  val rows = {
    val base = Seq(
      Row(true, 0.45, "3.14"),
      Row(false, null, "\"testing...\""),
      Row(false, 3.14159, "Hello, world!"),
      // in case of CSV, an empty string is the same as null - no way around it
      Row(null, null, "")
    )
    val repeatedFewTimes = (1 to 10).flatMap(_ => base)
    repeatedFewTimes
  }

  lazy val dataFrame = createDataFrame(rows, schema)

  "WriteDataFrame and ReadDataFrame" should {
    "write and read CSV file" in {
      val wdf =
        new WriteDataFrame()
          .setStorageType(
            new OutputStorageTypeChoice.File()
              .setOutputFile(absoluteTestsDirPath.fullPath + "/test_files")
              .setFileFormat(
                new OutputFileFormatChoice.Csv()
                  .setCsvColumnSeparator(CsvParameters.ColumnSeparatorChoice.Comma())
                  .setNamesIncluded(true)))
      wdf.executeUntyped(Vector(dataFrame))(executionContext)

      val rdf =
        new ReadDataFrame()
          .setStorageType(
            new InputStorageTypeChoice.File()
              .setSourceFile(absoluteTestsDirPath.fullPath + "/test_files")
              .setFileFormat(new InputFileFormatChoice.Csv()
                .setCsvColumnSeparator(CsvParameters.ColumnSeparatorChoice.Comma())
                .setNamesIncluded(true)
                .setShouldConvertToBoolean(true)))
      val loadedDataFrame = rdf.executeUntyped(Vector())(executionContext).head.asInstanceOf[DataFrame]

      assertDataFramesEqual(loadedDataFrame, dataFrame, checkRowOrder = false)
    }

    "write and read JSON file" in {
      val wdf =
        new WriteDataFrame()
          .setStorageType(new OutputStorageTypeChoice.File()
            .setOutputFile(absoluteTestsDirPath.fullPath + "json")
            .setFileFormat(new OutputFileFormatChoice.Json()))

      wdf.executeUntyped(Vector(dataFrame))(executionContext)

      val rdf =
        new ReadDataFrame()
          .setStorageType(new InputStorageTypeChoice.File()
            .setSourceFile(absoluteTestsDirPath.fullPath + "json")
            .setFileFormat(new InputFileFormatChoice.Json()))
      val loadedDataFrame = rdf.executeUntyped(Vector())(executionContext).head.asInstanceOf[DataFrame]

      assertDataFramesEqual(loadedDataFrame, dataFrame, checkRowOrder = false)
    }
  }
}
