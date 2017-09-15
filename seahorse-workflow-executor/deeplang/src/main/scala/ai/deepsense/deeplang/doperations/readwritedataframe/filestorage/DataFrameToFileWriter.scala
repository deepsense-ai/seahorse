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

import org.apache.spark.SparkException
import ai.deepsense.commons.utils.LoggerForCallerClass
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.exceptions.WriteFileException
import ai.deepsense.deeplang.doperations.inout.OutputFileFormatChoice.Csv
import ai.deepsense.deeplang.doperations.inout.OutputStorageTypeChoice
import ai.deepsense.deeplang.doperations.readwritedataframe.{FilePath, FilePathFromLibraryPath, FileScheme}
import ai.deepsense.deeplang.doperations.readwritedataframe.filestorage.csv.CsvSchemaStringifierBeforeCsvWriting
import ai.deepsense.deeplang.exceptions.DeepLangException
import ai.deepsense.deeplang.{ExecutionContext, FileSystemClient}
import org.apache.spark.sql.SaveMode

object DataFrameToFileWriter {

  val logger = LoggerForCallerClass()

  def writeToFile(
      fileChoice: OutputStorageTypeChoice.File,
      context: ExecutionContext,
      dataFrame: DataFrame): Unit = {
    implicit val ctx = context

    val path = FileSystemClient.replaceLeadingTildeWithHomeDirectory(fileChoice.getOutputFile())
    val filePath = FilePath(path)
    val saveMode = if (fileChoice.getShouldOverwrite) SaveMode.Overwrite else SaveMode.ErrorIfExists

    try {
      val preprocessed = fileChoice.getFileFormat() match {
        case csv: Csv => CsvSchemaStringifierBeforeCsvWriting.preprocess(dataFrame)
        case other => dataFrame
      }
      writeUsingProvidedFileScheme(fileChoice, preprocessed, filePath, saveMode)
    } catch {
      case e: SparkException =>
        logger.error(s"WriteDataFrame error: Spark problem. Unable to write file to $path", e)
        throw WriteFileException(path, e)
    }
  }

  private def writeUsingProvidedFileScheme(
      fileChoice: OutputStorageTypeChoice.File, dataFrame: DataFrame, path: FilePath, saveMode: SaveMode
    )(implicit context: ExecutionContext): Unit = {
    import FileScheme._
    path.fileScheme match {
      case Library =>
        val filePath = FilePathFromLibraryPath(path)
        val FilePath(_, libraryPath) = filePath
        new java.io.File(libraryPath).getParentFile.mkdirs()
        writeUsingProvidedFileScheme(fileChoice, dataFrame, filePath, saveMode)
      case FileScheme.File => DriverFiles.write(dataFrame, path, fileChoice.getFileFormat(), saveMode)
      case HDFS => ClusterFiles.write(dataFrame, path, fileChoice.getFileFormat(), saveMode)
      case HTTP | HTTPS | FTP => throw NotSupportedScheme(path.fileScheme)
    }
  }

  case class NotSupportedScheme(fileScheme: FileScheme)
    extends DeepLangException(s"Not supported file scheme ${fileScheme.pathPrefix}")

}
