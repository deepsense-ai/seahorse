/**
 * Copyright 2016, deepsense.ai
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

package io.deepsense.deeplang.doperations.readwritedataframe.filestorage

import java.io.{File, IOException, PrintWriter}

import scala.io.Source
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.execution.datasources.csv.{DataframeToDriverCsvFileWriter, RawCsvRDDToDataframe}
import org.apache.spark.sql.{SaveMode, DataFrame => SparkDataFrame}
import io.deepsense.commons.resources.ManagedResource
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.inout.{InputFileFormatChoice, OutputFileFormatChoice}
import io.deepsense.deeplang.doperations.readwritedataframe.filestorage.csv.CsvOptions
import io.deepsense.deeplang.doperations.readwritedataframe.{FilePath, FileScheme}
import io.deepsense.sparkutils.SQL

object DriverFiles {

  def read(driverPath: String, fileFormat: InputFileFormatChoice)
          (implicit context: ExecutionContext): SparkDataFrame = fileFormat match {
    case csv: InputFileFormatChoice.Csv => readCsv(driverPath, csv)
    case json: InputFileFormatChoice.Json => readJson(driverPath)
    case parquet: InputFileFormatChoice.Parquet => throw ParquetNotSupported
  }

  def write(dataFrame: DataFrame, path: FilePath, fileFormat: OutputFileFormatChoice, saveMode: SaveMode)
           (implicit context: ExecutionContext): Unit = {
    path.verifyScheme(FileScheme.File)
    if (saveMode == SaveMode.ErrorIfExists && new File(path.pathWithoutScheme).exists()){
      throw new IOException(s"Output file ${path.fullPath} already exists")
    }
    fileFormat match {
      case csv: OutputFileFormatChoice.Csv => writeCsv(path, csv, dataFrame)
      case json: OutputFileFormatChoice.Json => writeJson(path, dataFrame)
      case parquet: OutputFileFormatChoice.Parquet => throw ParquetNotSupported
    }
  }

  private def readCsv
      (driverPath: String, csvChoice: InputFileFormatChoice.Csv)
      (implicit context: ExecutionContext): SparkDataFrame = {
    val params = CsvOptions.map(csvChoice.getNamesIncluded, csvChoice.getCsvColumnSeparator())
    val lines = Source.fromFile(driverPath).getLines().toStream
    val fileLinesRdd = context.sparkContext.parallelize(lines)

    RawCsvRDDToDataframe.parse(fileLinesRdd, context.sparkSQLSession, params)
  }

  private def readJson(driverPath: String)(implicit context: ExecutionContext) = {
    val lines = Source.fromFile(driverPath).getLines().toStream
    val fileLinesRdd = context.sparkContext.parallelize(lines)
    context.sparkSQLSession.read.json(fileLinesRdd)
  }

  private def writeCsv
      (path: FilePath, csvChoice: OutputFileFormatChoice.Csv, dataFrame: DataFrame)
      (implicit context: ExecutionContext): Unit = {
    val params = CsvOptions.map(csvChoice.getNamesIncluded, csvChoice.getCsvColumnSeparator())

    DataframeToDriverCsvFileWriter.write(
      dataFrame.sparkDataFrame,
      params,
      dataFrame.schema.get,
      path.pathWithoutScheme
    )
  }

  private def writeJson(path: FilePath, dataFrame: DataFrame)
                       (implicit context: ExecutionContext): Unit = {
    val rawJsonLines: RDD[String] = SQL.dataFrameToJsonRDD(dataFrame.sparkDataFrame)
    writeRddToDriverFile(path.pathWithoutScheme, rawJsonLines)
  }

  private def writeRddToDriverFile(driverPath: String, lines: RDD[String]): Unit = {
    val recordSeparator = System.getProperty("line.separator", "\n")
    ManagedResource(new PrintWriter(driverPath)) { writer =>
      lines.collect().foreach(line => writer.write(line + recordSeparator))
    }
  }

}
