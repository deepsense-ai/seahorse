/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.deeplang.doperations.readwritedataframe

import scala.reflect.runtime.{universe => ru}

import org.apache.spark.sql.{DataFrame => SparkDataFrame, SaveMode}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.inout.OutputFileFormatChoice.Csv
import io.deepsense.deeplang.doperations.inout.{InputFileFormatChoice, OutputFileFormatChoice}
import io.deepsense.deeplang.doperations.readwritedataframe.csv.CsvOptions

object ClusterFiles {

  import CsvOptions._

  def read(path: FilePath, fileFormat: InputFileFormatChoice)
          (implicit context: ExecutionContext): SparkDataFrame = {
    val clusterPath = path.fullPath
    fileFormat match {
      case csv: InputFileFormatChoice.Csv => readCsv(clusterPath, csv)
      case json: InputFileFormatChoice.Json => context.sparkSession.read.json(clusterPath)
      case parquet: InputFileFormatChoice.Parquet => context.sparkSession.read.parquet(clusterPath)
    }
  }

  def write(dataFrame: DataFrame, path: FilePath, fileFormat: OutputFileFormatChoice)
           (implicit context: ExecutionContext): Unit = {
    val clusterPath = path.fullPath
    val writer = fileFormat match {
      case (csvChoice: Csv) =>
        val namesIncluded = csvChoice.getCsvNamesIncluded
        dataFrame
          .sparkDataFrame
          .write.format("com.databricks.spark.csv")
          .setCsvOptions(namesIncluded, csvChoice.getCsvColumnSeparator())
      case OutputFileFormatChoice.Parquet() =>
        // TODO: DS-1480 Writing DF in parquet format when column names contain forbidden chars
        dataFrame.sparkDataFrame.write.format("parquet")
      case OutputFileFormatChoice.Json() =>
        dataFrame.sparkDataFrame.write.format("json")
    }
    writer.mode(SaveMode.Overwrite).save(clusterPath)
  }

  private def readCsv(clusterPath: String, csvChoice: InputFileFormatChoice.Csv)
                     (implicit context: ExecutionContext) =
    context.sparkSession.read
      .format("com.databricks.spark.csv")
      .setCsvOptions(csvChoice.getCsvNamesIncluded, csvChoice.getCsvColumnSeparator())
      .load(clusterPath)

}
