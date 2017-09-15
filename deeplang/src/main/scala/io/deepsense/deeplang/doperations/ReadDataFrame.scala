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

package io.deepsense.deeplang.doperations

import java.io._
import java.net.UnknownHostException
import java.nio.file.{Files, Paths}
import java.util.UUID

import scala.reflect.runtime.{universe => ru}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
import org.apache.spark.sql.{Row, DataFrame => SparkDataFrame}

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.ReadDataFrame.ReadDataFrameParameters
import io.deepsense.deeplang.doperations.exceptions.{DeepSenseIOException, DeepSenseUnknownHostException}
import io.deepsense.deeplang.doperations.inout.InputFileFormatChoice.Csv
import io.deepsense.deeplang.doperations.inout._
import io.deepsense.deeplang.doperations.readwritedataframe._
import io.deepsense.deeplang.doperations.readwritedataframe.csv.CsvSchemaInferencerAfterReading
import io.deepsense.deeplang.doperations.readwritedataframe.validators.{FilePathHasValidFileScheme, ParquetSupportedOnClusterOnly}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.Params
import io.deepsense.deeplang.params.choice.ChoiceParam
import io.deepsense.deeplang.{DKnowledge, DOperation0To1, ExecutionContext}

case class ReadDataFrame()
    extends DOperation0To1[DataFrame]
    with ReadDataFrameParameters
    with Params {

  override val id: Id = "c48dd54c-6aef-42df-ad7a-42fc59a09f0e"
  override val name: String = "Read DataFrame"
  override val description: String =
    "Reads a DataFrame from a file or database"

  override val since: Version = Version(0, 4, 0)

  val params = declareParams(storageType)
  setDefault(storageType, InputStorageTypeChoice.File())

  override protected def _execute(context: ExecutionContext)(): DataFrame = {
    implicit val ec = context

    try {
      val dataframe = getStorageType() match {
        case jdbcChoice: InputStorageTypeChoice.Jdbc => readFromJdbc(jdbcChoice)
        case fileChoice: InputStorageTypeChoice.File => readFromFile(fileChoice)
      }
      DataFrame.fromSparkDataFrame(dataframe)
    } catch {
      case e: UnknownHostException => throw DeepSenseUnknownHostException(e)
      case e: IOException => throw DeepSenseIOException(e)
    }
  }

  override protected def _inferKnowledge(context: InferContext)():
      (DKnowledge[DataFrame], InferenceWarnings) = {
    FilePathHasValidFileScheme.validate(this)
    ParquetSupportedOnClusterOnly.validate(this)
    super._inferKnowledge(context)()
  }

  private def readFromFile(fileChoice: InputStorageTypeChoice.File)
                          (implicit context: ExecutionContext) = {
    val path = FilePath(fileChoice.getSourceFile)
    val rawDataFrame = readUsingProvidedFileScheme(path, fileChoice.getFileFormat)
    val postprocessed = fileChoice.getFileFormat match {
      case csv: Csv => CsvSchemaInferencerAfterReading.postprocess(csv)(rawDataFrame)
      case other => rawDataFrame
    }
    postprocessed
  }

  private def readUsingProvidedFileScheme
      (path: FilePath, fileFormat: InputFileFormatChoice)
      (implicit context: ExecutionContext): SparkDataFrame =
    path.fileScheme match {
      case FileScheme.Library =>
        val filePath = FilePathFromLibraryPath(path)
        readUsingProvidedFileScheme(filePath, fileFormat)
      case FileScheme.File => DriverFiles.read(path.pathWithoutScheme, fileFormat)
      case FileScheme.HTTP | FileScheme.HTTPS | FileScheme.FTP =>
        val downloadedPath = FileDownloader.downloadFile(path.fullPath)
        readUsingProvidedFileScheme(downloadedPath, fileFormat)
      case FileScheme.HDFS => ClusterFiles.read(path, fileFormat)
    }

  private def readFromJdbc
      (jdbcChoice: InputStorageTypeChoice.Jdbc)
      (implicit context: ExecutionContext): SparkDataFrame =
    context.sparkSession
      .read.format("jdbc")
      .option("driver", jdbcChoice.getJdbcDriverClassName)
      .option("url", jdbcChoice.getJdbcUrl)
      .option("dbtable", jdbcChoice.getJdbcTableName)
      .load()

  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

object ReadDataFrame {
  val recordDelimiterSettingName = "textinputformat.record.delimiter"

  trait ReadDataFrameParameters {
    this: Params =>

    val storageType = ChoiceParam[InputStorageTypeChoice](
      name = "data storage type",
      description = "Storage type.")

    def getStorageType(): InputStorageTypeChoice = $(storageType)
    def setStorageType(value: InputStorageTypeChoice): this.type = set(storageType, value)
  }

  def apply(
      fileName: String,
      csvColumnSeparator: CsvParameters.ColumnSeparatorChoice,
      csvNamesIncluded: Boolean,
      csvConvertToBoolean: Boolean) : ReadDataFrame = {
    new ReadDataFrame()
      .setStorageType(
        InputStorageTypeChoice.File()
          .setSourceFile(fileName)
          .setFileFormat(
            InputFileFormatChoice.Csv()
              .setCsvColumnSeparator(csvColumnSeparator)
              .setCsvNamesIncluded(csvNamesIncluded)
              .setShouldConvertToBoolean(csvConvertToBoolean)))
  }
}
