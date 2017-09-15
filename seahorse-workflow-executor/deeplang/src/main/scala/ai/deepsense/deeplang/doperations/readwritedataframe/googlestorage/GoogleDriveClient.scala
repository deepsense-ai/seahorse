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

import java.io.{ByteArrayInputStream, FileOutputStream}
import java.util

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.model.File
import com.google.api.services.drive.{Drive, DriveScopes}

import ai.deepsense.commons.resources.ManagedResource
import ai.deepsense.commons.utils.LoggerForCallerClass
import ai.deepsense.deeplang.doperations.inout.CsvParameters.ColumnSeparatorChoice

private[googlestorage] object GoogleDriveClient {

  val logger = LoggerForCallerClass()

  val googleSheetCsvSeparator = ColumnSeparatorChoice.Comma()

  private val ApplicationName = "Seahorse"

  private val Scopes = util.Arrays.asList(DriveScopes.DRIVE)

  def uploadCsvFileAsGoogleSheet(
      credentials: GoogleCretendialsJson,
      sheetId: GoogleSheetId,
      filePath: String
    ): Unit = {
    val fileMetadata = new File().setMimeType("application/vnd.google-apps.spreadsheet")
    val mediaContent = new FileContent("text/csv", new java.io.File(filePath))

    driveService(credentials).files.update(sheetId, fileMetadata, mediaContent).execute
  }

  def downloadGoogleSheetAsCsvFile(
      credentials: GoogleCretendialsJson,
      sheetId: GoogleSheetId,
      filePath: String
    ): Unit = {
    val file = new java.io.File(filePath)
    file.getParentFile.mkdirs()

    ManagedResource(new FileOutputStream(file)) { fos =>
      driveService(credentials).files().export(sheetId, "text/csv").executeMediaAndDownloadTo(fos)
      logger.info(s"Downloaded google sheet id=$sheetId to the file $filePath")
    }
  }

  private def driveService(serviceAccountCredentials: String): Drive = {
    val credential = {
      val in = new ByteArrayInputStream(serviceAccountCredentials.getBytes)
      GoogleCredential.fromStream(in).createScoped(Scopes)
    }
    new Drive.Builder(
      GoogleNetHttpTransport.newTrustedTransport(),
      jsonFactory,
      credential
    ).setApplicationName(ApplicationName).build
  }

  // Default choice is JacksonFactory. However spark depends on Jackson as well
  // and google/spark jackson versions are binary incompatible with each other.
  private val jsonFactory = GsonFactory.getDefaultInstance

}
