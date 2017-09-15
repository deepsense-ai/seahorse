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

package ai.deepsense.deeplang.doperations.readwritedataframe.filestorage

import scala.reflect.runtime.{universe => ru}

import org.apache.spark.sql.{SaveMode, DataFrame => SparkDataFrame}

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.inout.OutputFileFormatChoice.Csv
import ai.deepsense.deeplang.doperations.inout.{InputFileFormatChoice, OutputFileFormatChoice}
import ai.deepsense.deeplang.doperations.readwritedataframe.FilePath
import ai.deepsense.deeplang.doperations.readwritedataframe.filestorage.csv.CsvOptions

private[filestorage] object ClusterFiles {

  import CsvOptions._

  def read(path: FilePath, fileFormat: InputFileFormatChoice)
          (implicit context: ExecutionContext): SparkDataFrame = {
    val clusterPath = path.fullPath
    fileFormat match {
      case csv: InputFileFormatChoice.Csv => readCsv(clusterPath, csv)
      case json: InputFileFormatChoice.Json => context.sparkSQLSession.read.json(clusterPath)
      case parquet: InputFileFormatChoice.Parquet => context.sparkSQLSession.read.parquet(clusterPath)
    }
  }

  def write(dataFrame: DataFrame, path: FilePath, fileFormat: OutputFileFormatChoice, saveMode: SaveMode)
           (implicit context: ExecutionContext): Unit = {
    val clusterPath = path.fullPath
    val writer = fileFormat match {
      case (csvChoice: Csv) =>
        val namesIncluded = csvChoice.getNamesIncluded
        dataFrame
          .sparkDataFrame
          .write.format("com.databricks.spark.csv")
          .options(CsvOptions.map(namesIncluded, csvChoice.getCsvColumnSeparator()))
      case _: OutputFileFormatChoice.Parquet =>
        // TODO: DS-1480 Writing DF in parquet format when column names contain forbidden chars
        dataFrame.sparkDataFrame.write.format("parquet")
      case _: OutputFileFormatChoice.Json =>
        dataFrame.sparkDataFrame.write.format("json")
    }
    writer.mode(saveMode).save(clusterPath)
  }

  private def readCsv(clusterPath: String, csvChoice: InputFileFormatChoice.Csv)
                     (implicit context: ExecutionContext) =
    context.sparkSQLSession.read
      .format("com.databricks.spark.csv")
      .setCsvOptions(csvChoice.getNamesIncluded, csvChoice.getCsvColumnSeparator())
      .load(clusterPath)

}
