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

package ai.deepsense.deeplang.doperations.readwritedataframe.googlestorage

import java.util.UUID

import ai.deepsense.commons.utils.{FileOperations, LoggerForCallerClass}
import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.inout.OutputStorageTypeChoice.GoogleSheet
import ai.deepsense.deeplang.doperations.inout.{OutputFileFormatChoice, OutputStorageTypeChoice}
import ai.deepsense.deeplang.doperations.readwritedataframe.filestorage.DataFrameToFileWriter
import ai.deepsense.deeplang.doperations.readwritedataframe.{FilePath, FileScheme}

object DataFrameToGoogleSheetWriter {

  val logger = LoggerForCallerClass()

  def writeToGoogleSheet(
      googleSheetChoice: GoogleSheet,
      context: ExecutionContext,
      dataFrame: DataFrame
    ): Unit = {
    val localTmpFile: FilePath = saveDataFrameAsDriverCsvFile(
      googleSheetChoice, context, dataFrame
    )
    GoogleDriveClient.uploadCsvFileAsGoogleSheet(
      googleSheetChoice.getGoogleServiceAccountCredentials(),
      googleSheetChoice.getGoogleSheetId(),
      localTmpFile.pathWithoutScheme
    )
  }

  private def saveDataFrameAsDriverCsvFile(
      googleSheetChoice: GoogleSheet,
      context: ExecutionContext,
      dataFrame: DataFrame): FilePath = {
    val sheetId = googleSheetChoice.getGoogleSheetId()

    val localTmpFile = FilePath(
      FileScheme.File, s"/tmp/seahorse/google_sheet_${sheetId}__${UUID.randomUUID()}.csv"
    )

    FileOperations.mkdirsParents(new java.io.File(localTmpFile.pathWithoutScheme))

    val localTmpFileParams = new OutputStorageTypeChoice.File()
      .setOutputFile(localTmpFile.fullPath)
      .setFileFormat(new OutputFileFormatChoice.Csv()
        .setCsvColumnSeparator(GoogleDriveClient.googleSheetCsvSeparator)
        .setNamesIncluded(googleSheetChoice.getNamesIncluded)
      )
    DataFrameToFileWriter.writeToFile(localTmpFileParams, context, dataFrame)
    localTmpFile
  }
}
