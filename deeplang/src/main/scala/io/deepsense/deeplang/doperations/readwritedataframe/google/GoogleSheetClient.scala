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

package io.deepsense.deeplang.doperations.readwritedataframe.google

import java.io.{File => _, _}
import java.util
import java.util.UUID

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.{Drive, DriveScopes}

import io.deepsense.commons.resources.ManagedResource
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.doperations.inout.{CsvParameters, InputFileFormatChoice, InputStorageTypeChoice}
import io.deepsense.deeplang.doperations.inout.InputStorageTypeChoice.GoogleSheet
import io.deepsense.deeplang.doperations.readwritedataframe.{FilePath, FileScheme}

object GoogleSheetClient extends Logging {

  private val ApplicationName = "Seahorse"

  // TODO Consider handling both DRIVER and DRIVER_ONLY if user wants read-only access.
  private val Scopes = util.Arrays.asList(DriveScopes.DRIVE)

  def reduceGoogleSheetToDriverFile(googleSheet: GoogleSheet): InputStorageTypeChoice.File = {
    val id = googleSheet.getGoogleSheetId()
    val tmpPath = s"/tmp/seahorse/google_sheet_${id}__${UUID.randomUUID()}.csv"

    val file = new java.io.File(tmpPath)
    file.getParentFile.mkdirs()

    ManagedResource(new FileOutputStream(file)) { fos =>
      driveService(
        googleSheet.getGoogleServiceAccountCredentials()
      ).files().export(id, "text/csv").executeMediaAndDownloadTo(fos)
      logger.info(s"Downloaded google sheet id=$id to the temporary path $tmpPath")
    }

    val downloadedCsvPath = FilePath(FileScheme.File, tmpPath)

    new InputStorageTypeChoice.File()
      .setFileFormat(
        new InputFileFormatChoice.Csv()
          .setCsvColumnSeparator(new CsvParameters.ColumnSeparatorChoice.Comma())
          .setShouldConvertToBoolean(googleSheet.getShouldConvertToBoolean)
          .setNamesIncluded(googleSheet.getNamesIncluded)
      )
      .setSourceFile(downloadedCsvPath.fullPath)
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
