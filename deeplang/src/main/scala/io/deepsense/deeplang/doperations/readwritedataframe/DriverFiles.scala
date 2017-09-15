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

import java.io.PrintWriter

import scala.io.Source
import scala.reflect.runtime.{universe => ru}

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.execution.datasources.csv.{DataframeToDriverCsvFileWriter, RawCsvRDDToDataframe}
import org.apache.spark.sql.{DataFrame => SparkDataFrame}

import io.deepsense.commons.resources.ManagedResource
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.inout.{InputFileFormatChoice, OutputFileFormatChoice}
import io.deepsense.deeplang.doperations.readwritedataframe.csv.CsvOptions

object DriverFiles {

  def read(driverPath: String, fileFormat: InputFileFormatChoice)
          (implicit context: ExecutionContext): SparkDataFrame = fileFormat match {
    case csv: InputFileFormatChoice.Csv => readCsv(driverPath, csv)
    case json: InputFileFormatChoice.Json => readJson(driverPath)
    case parquet: InputFileFormatChoice.Parquet => throw ParquetNotSupported
  }

  def write(dataFrame: DataFrame, path: FilePath, fileFormat: OutputFileFormatChoice)
           (implicit context: ExecutionContext): Unit = {
    path.verifyScheme(FileScheme.File)
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

    RawCsvRDDToDataframe.parse(fileLinesRdd, context.sparkSession, params)
  }

  private def readJson(driverPath: String)(implicit context: ExecutionContext) = {
    val lines = Source.fromFile(driverPath).getLines().toStream
    val fileLinesRdd = context.sparkContext.parallelize(lines)
    context.sparkSession.read.json(fileLinesRdd)
  }

  private def writeCsv
      (path: FilePath, csvChoice: OutputFileFormatChoice.Csv, dataFrame: DataFrame)
      (implicit context: ExecutionContext): Unit = {
    val params = CsvOptions.map(csvChoice.getNamesIncluded, csvChoice.getCsvColumnSeparator())

    val data = dataFrame.sparkDataFrame.rdd.collect()

    DataframeToDriverCsvFileWriter.write(
      data,
      params,
      dataFrame.schema.get,
      path
    )
  }

  private def writeJson(path: FilePath, dataFrame: DataFrame)
                       (implicit context: ExecutionContext): Unit = {
    val rawJsonLines = dataFrame.sparkDataFrame.toJSON
    writeRddToDriverFile(path.pathWithoutScheme, rawJsonLines.rdd)
  }

  private def writeRddToDriverFile(driverPath: String, lines: RDD[String]): Unit = {
    val recordSeparator = System.getProperty("line.separator", "\n")
    ManagedResource(new PrintWriter(driverPath)) { writer =>
      lines.collect().foreach(line => writer.write(line + recordSeparator))
    }
  }

}
