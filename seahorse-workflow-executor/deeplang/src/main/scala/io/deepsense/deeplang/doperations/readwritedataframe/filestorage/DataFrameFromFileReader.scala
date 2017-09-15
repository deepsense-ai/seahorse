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

import org.apache.spark.sql._

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperations.inout.InputFileFormatChoice.Csv
import io.deepsense.deeplang.doperations.inout.{InputFileFormatChoice, InputStorageTypeChoice}
import io.deepsense.deeplang.doperations.readwritedataframe._
import io.deepsense.deeplang.doperations.readwritedataframe.filestorage.csv.CsvSchemaInferencerAfterReading

object DataFrameFromFileReader {

  def readFromFile(fileChoice: InputStorageTypeChoice.File)
                  (implicit context: ExecutionContext): DataFrame = {
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
  (implicit context: ExecutionContext): DataFrame =
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

}
