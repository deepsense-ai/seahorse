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

import java.io.{File, IOException, PrintWriter}

import scala.io.Source

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.execution.datasources.csv.{DataframeToDriverCsvFileWriter, RawCsvRDDToDataframe}
import org.apache.spark.sql.{Dataset, Encoders, Row, SaveMode, DataFrame => SparkDataFrame}
import ai.deepsense.commons.resources.ManagedResource
import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.inout.{InputFileFormatChoice, OutputFileFormatChoice}
import ai.deepsense.deeplang.doperations.readwritedataframe.filestorage.csv.CsvOptions
import ai.deepsense.deeplang.doperations.readwritedataframe.{FilePath, FileScheme}
import ai.deepsense.deeplang.readjsondataset.JsonReader
import ai.deepsense.sparkutils.SQL

object DriverFiles extends JsonReader {

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

    RawCsvRDDToDataframe.parse(fileLinesRdd, context.sparkSQLSession.sparkSession, params)
  }

  private def readJson(driverPath: String)(implicit context: ExecutionContext) = {
    val lines = Source.fromFile(driverPath).getLines().toStream
    val fileLinesRdd = context.sparkContext.parallelize(lines)
    val sparkSession = context.sparkSQLSession.sparkSession
    readJsonFromRdd(fileLinesRdd, sparkSession)
  }

  private def writeCsv
      (path: FilePath, csvChoice: OutputFileFormatChoice.Csv, dataFrame: DataFrame)
      (implicit context: ExecutionContext): Unit = {
    val params = CsvOptions.map(csvChoice.getNamesIncluded, csvChoice.getCsvColumnSeparator())

    DataframeToDriverCsvFileWriter.write(
      dataFrame.sparkDataFrame,
      params,
      dataFrame.schema.get,
      path.pathWithoutScheme,
      context.sparkSQLSession.sparkSession
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
