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

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.doperations.inout.CsvParameters
import io.deepsense.deeplang.doperations.readwritedataframe.{FilePath, FileScheme};

object GoogleSheetClient extends Logging {

  private val ApplicationName = "Seahorse"

  // TODO Consider handling both DRIVER and DRIVER_ONLY if user wants read-only access.
  private val Scopes = util.Arrays.asList(DriveScopes.DRIVE)

  val googleCsvColumnSeparator = new CsvParameters.ColumnSeparatorChoice.Comma()

  // TODO Handle 'unauthorized' and other google api exceptions
  def downloadGoogleSheetAsCsv(id: String): FilePath = {
    val path = s"/tmp/seahorse/google_sheet_${id}__${UUID.randomUUID()}.csv"
    val file = new java.io.File(path)
    file.getParentFile.mkdirs()
    val fos = new FileOutputStream(file)
    driveService().files().export(id, "text/csv").executeMediaAndDownloadTo(fos)
    logger.info(s"Downloaded google sheet id=$id to the temporary path $path")
    FilePath(FileScheme.File, path)
  }

  private def driveService(): Drive = {
    // TODO Revoke service account and add google credentials on every jenkins node
    val credential = {
      val in: InputStream = GoogleSheetClient.getClass.getResourceAsStream("/service_account.json")
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
