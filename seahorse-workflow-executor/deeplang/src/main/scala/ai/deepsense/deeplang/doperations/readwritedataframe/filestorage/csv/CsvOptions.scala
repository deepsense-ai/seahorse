/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations.readwritedataframe.filestorage.csv

import org.apache.spark.sql.{DataFrameReader, Row, DataFrameWriter}

import ai.deepsense.deeplang.doperations.inout.CsvParameters
import ai.deepsense.deeplang.doperations.inout.CsvParameters.ColumnSeparatorChoice

object CsvOptions {

  def map(
      namesIncluded: Boolean,
      columnSeparator: ColumnSeparatorChoice): Map[String, String] = {
    val headerFlag = if (namesIncluded) "true" else "false"
    Map(
      "header" -> headerFlag,
      "delimiter" -> CsvParameters.determineColumnSeparatorOf(columnSeparator).toString,
      "inferSchema" -> "false"
    )
  }

  // Unfortunately, making analogous RichDataFrameWriter is awkward, if not impossible.
  // This is because between Spark 1.6 and 2.0 DataFrameWriter became parametrized
  implicit class RichDataFrameReader(self: DataFrameReader) {
    def setCsvOptions(
        namesIncluded: Boolean,
        columnSeparator: ColumnSeparatorChoice): DataFrameReader = {
      val paramMap = map(namesIncluded, columnSeparator)
      paramMap.foldLeft(self) { case (reader, (key, value)) =>
        reader.option(key, value)
      }
    }
  }


}
