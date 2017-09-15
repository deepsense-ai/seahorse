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

package ai.deepsense.deeplang.doperations

import org.scalatest._

import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang._
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.inout._
import ai.deepsense.deeplang.doperations.readwritedataframe.{FilePath, FileScheme}
import ai.deepsense.deeplang.utils.DataFrameMatchers
import ai.deepsense.deeplang.doperations.readwritedataframe.googlestorage._
import ai.deepsense.deeplang.google.GoogleServices

class GoogleSheetSpec extends FreeSpec with BeforeAndAfter with BeforeAndAfterAll with LocalExecutionContext
  with Matchers with TestFiles with Logging {

  "Seahorse is integrated with Google Sheets" in {
    info("It means that once given some Dataframe")
    val someDataFrame = readCsvFileFromDriver(someCsvFile)
    info("It can be saved as a Google Sheet")
    val googleSheetId = GoogleServices.googleSheetForTestsId
    writeGoogleSheet(someDataFrame, googleSheetId)

    info("And after that it can be read again from google sheet")
    val dataFrameReadAgainFromGoogleSheet = readGoogleSheet(googleSheetId)

    DataFrameMatchers.assertDataFramesEqual(dataFrameReadAgainFromGoogleSheet, someDataFrame)
  }

  private def credentials: String = GoogleServices.serviceAccountJson match {
    case Some(credentials) => credentials
    case None if Jenkins.isRunningOnJenkins => throw GoogleServices.serviceAccountNotExistsException()
    case None if !Jenkins.isRunningOnJenkins => cancel(GoogleServices.serviceAccountNotExistsException())
  }

  private def writeGoogleSheet(dataframe: DataFrame, googleSheetId: GoogleSheetId): Unit = {
    val write = new WriteDataFrame()
      .setStorageType(
        new OutputStorageTypeChoice.GoogleSheet()
          .setGoogleServiceAccountCredentials(credentials)
          .setGoogleSheetId(googleSheetId)
      )
    write.executeUntyped(Vector(dataframe))(executionContext)
  }

  private def readGoogleSheet(googleSheetId: GoogleSheetId): DataFrame = {
    val readDF = new ReadDataFrame()
      .setStorageType(
        new InputStorageTypeChoice.GoogleSheet()
          .setGoogleSheetId(googleSheetId)
          .setGoogleServiceAccountCredentials(credentials)
      )
    readDF.executeUntyped(Vector.empty[DOperable])(executionContext).head.asInstanceOf[DataFrame]
  }

  private def readCsvFileFromDriver(filePath: FilePath) = {
    require(filePath.fileScheme == FileScheme.File)
    val readDF = new ReadDataFrame()
      .setStorageType(
        new InputStorageTypeChoice.File()
          .setSourceFile(filePath.fullPath)
          .setFileFormat(new InputFileFormatChoice.Csv()
            .setNamesIncluded(true)
          )
      )
    readDF.executeUntyped(Vector.empty[DOperable])(executionContext).head.asInstanceOf[DataFrame]
  }

}
