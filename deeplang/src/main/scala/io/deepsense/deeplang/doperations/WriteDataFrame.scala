/**
 * Copyright 2015, deepsense.io
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

import java.io.IOException
import java.util.Properties

import scala.reflect.runtime.{universe => ru}
import org.apache.spark.SparkException
import org.apache.spark.sql.SaveMode

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.{DeepSenseIOException, WriteFileException}
import io.deepsense.deeplang.doperations.inout.OutputFileFormatChoice.Csv
import io.deepsense.deeplang.doperations.inout.OutputFileFormatChoice._
import io.deepsense.deeplang.doperations.inout.OutputStorageTypeChoice.File
import io.deepsense.deeplang.doperations.inout._
import io.deepsense.deeplang.doperations.readwritedataframe._
import io.deepsense.deeplang.doperations.readwritedataframe.csv.{CsvSchemaInferencerAfterReading, CsvSchemaStringifierBeforeCsvWriting}
import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.Params
import io.deepsense.deeplang.params.choice.ChoiceParam
import io.deepsense.deeplang._
import io.deepsense.deeplang.documentation.OperationDocumentation
import io.deepsense.deeplang.doperations.readwritedataframe.validators.{FilePathHasValidFileScheme, ParquetSupportedOnClusterOnly}

case class WriteDataFrame()
  extends DOperation1To0[DataFrame]
  with Params
  with OperationDocumentation {

  override val id: Id = "9e460036-95cc-42c5-ba64-5bc767a40e4e"
  override val name: String = "Write DataFrame"
  override val description: String = "Writes a DataFrame to a file or database"

  override val since: Version = Version(0, 4, 0)

  val storageType = ChoiceParam[OutputStorageTypeChoice](
    name = "data storage type",
    description = "Storage type.")

  def getStorageType(): OutputStorageTypeChoice = $(storageType)
  def setStorageType(value: OutputStorageTypeChoice): this.type = set(storageType, value)

  val params: Array[io.deepsense.deeplang.params.Param[_]] = Array(storageType)
  setDefault(storageType, OutputStorageTypeChoice.File())

  override protected def execute(dataFrame: DataFrame)(context: ExecutionContext): Unit = {
    import OutputStorageTypeChoice._
    try {
      getStorageType() match {
        case jdbcChoice: Jdbc => writeToJdbc(jdbcChoice, context, dataFrame)
        case fileChoice: File => writeToFile(fileChoice, context, dataFrame)
      }
    } catch {
      case e: IOException =>
        logger.error(s"WriteDataFrame error. Could not write file to designated storage", e)
        throw DeepSenseIOException(e)
    }
  }

  override protected def inferKnowledge(k0: DKnowledge[DataFrame])(context: InferContext): (Unit, InferenceWarnings) = {
    FilePathHasValidFileScheme.validate(this)
    ParquetSupportedOnClusterOnly.validate(this)
    super.inferKnowledge(k0)(context)
  }

  private def writeToJdbc(
      jdbcChoice: OutputStorageTypeChoice.Jdbc,
      context: ExecutionContext,
      dataFrame: DataFrame): Unit = {
    val properties = new Properties()
    properties.setProperty("driver", jdbcChoice.getJdbcDriverClassName)

    val jdbcUrl = jdbcChoice.getJdbcUrl
    val jdbcTableName = jdbcChoice.getJdbcTableName

    dataFrame.sparkDataFrame.write.jdbc(jdbcUrl, jdbcTableName, properties)
  }

  private def writeToFile(
      fileChoice: OutputStorageTypeChoice.File,
      context: ExecutionContext,
      dataFrame: DataFrame): Unit = {
    implicit val ctx = context

    val path = FileSystemClient.replaceLeadingTildeWithHomeDirectory(fileChoice.getOutputFile)
    val filePath = FilePath(path)

    try {
      val preprocessed = fileChoice.getFileFormat() match {
        case csv: Csv => CsvSchemaStringifierBeforeCsvWriting.preprocess(dataFrame)
        case other => dataFrame
      }
      writeUsingProvidedFileScheme(fileChoice, preprocessed, filePath)
    } catch {
      case e: SparkException =>
        logger.error(s"WriteDataFrame error: Spark problem. Unable to write file to $path", e)
        throw WriteFileException(path, e)
    }
  }

  private def writeUsingProvidedFileScheme(
      fileChoice: File, dataFrame: DataFrame, path: FilePath)
      (implicit context: ExecutionContext): Unit = {
    import FileScheme._
    path.fileScheme match {
      case Library =>
        val filePath = FilePathFromLibraryPath(path)
        val FilePath(_, libraryPath) = filePath
        new java.io.File(libraryPath).getParentFile.mkdirs()
        writeUsingProvidedFileScheme(fileChoice, dataFrame, filePath)
      case FileScheme.File => DriverFiles.write(dataFrame, path, fileChoice.getFileFormat())
      case HDFS => ClusterFiles.write(dataFrame, path, fileChoice.getFileFormat())
      case HTTP | HTTPS | FTP => throw NotSupportedScheme(path.fileScheme)
    }
  }

  case class NotSupportedScheme(fileScheme: FileScheme)
    extends DeepLangException(s"Not supported file scheme ${fileScheme.pathPrefix}")
}
