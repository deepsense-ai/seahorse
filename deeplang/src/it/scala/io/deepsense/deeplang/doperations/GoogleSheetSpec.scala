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

import org.scalatest._

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.inout._
import io.deepsense.deeplang.doperations.readwritedataframe.{FilePath, FileScheme}
import io.deepsense.deeplang.utils.DataFrameMatchers
import io.deepsense.deeplang.doperations.readwritedataframe.googlestorage._

class GoogleSheetSpec extends FreeSpec with BeforeAndAfter with BeforeAndAfterAll with LocalExecutionContext
    with Matchers with TestFiles with Logging {

  "Seahorse is integrated with Google Sheets" in {
    info("It means that once given some Dataframe")
    val someDataFrame = readCsvFileFromDriver(someCsvFile)
    info("It can be saved as a Google Sheet")
    val googleSheetId = "1vI74zw07kIIfmvmciEbWkwDnGG_DI66ErBYaE-o02MY"
    writeGoogleSheet(someDataFrame, googleSheetId)

    info("And after that it can be read again from google sheet")
    val dataFrameReadAgainFromGoogleSheet = readGoogleSheet(googleSheetId)

    DataFrameMatchers.assertDataFramesEqual(dataFrameReadAgainFromGoogleSheet, someDataFrame)
  }

  val serviceAccountCredentials =
  """
    |{
    |  "type": "service_account",
    |  "project_id": "plasma-shift-148810",
    |  "private_key_id": "4711092ec81482ecf34be4a7c5bdd99622e1ed11",
    |  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDiKcRapecJPPQF\n60zZQXCqBeuGP4CGm71TpvUJaCotNkS65ssGibplDXrYJ4W/EKhm/IsWPq1Ivnjg\n1JkvUIlxh0GU8xgDLMElhUOkV/TdwGtEOV2bysk0sqPX40sToi3W783Sz1JF6E4A\nWn2eFngE1CbqF3EFN5olJy/uZEvehjCZVi6zmj6QDDxxBNSMWUYQsVdzuN5cB/l2\nW2OHa2P4xjgi7w1TwDssVhkdbItoHbf5/OBnZUn5GoU8VKLS09zz/axq9vv1zfHN\nUjr8T11t/ztvFWr47ZTQ1Qd+XmHzlJ3xde+gyCQTHC7LTrH/cBwW+KC8oiUf7skq\nGXYg6iYxAgMBAAECggEAEv5u/y3vuVblJ5obWiOk0qKspRmwMZ3iyKO88I1O/X0R\nSKEZa/MlNFdtebnYAbPkcMHE7JlW9EUK4db5BoA9CsvRNjE0Uw/vKLeIdsTsS7Nd\nTt2CfxMI0uQt3nO4Wm3Ea8i4AHxo7HYWFvGCm0RH0CABJKfTVhzVVUl1PRAvTwDN\nqHm22iYcMdmnOHrLIGvVSvl3HkqcLkykQN95797aVb5j3Do49bOnCyeKj4nJtn/c\n+Tx1pBopabKva8oA95yvxG6WEfHXJgJGYz5tn17VosFvOdQIaiwgcbQCEOJsx0EW\nshxrGrFqycy6dccn3INJs0HYYJyc9iYnP2z74FIGAQKBgQDz6stAQk1Ik7XRV41E\n/F/al6f4B+WCv/YhTFWDwAvKpQdjqN023mifiu+bUU3l4P11KOugseHqU3TYl5jN\nAg3zYtfMuDpOZ845S6SKggv5CteTbMBHJ+Sd4qmcKCVPyEOABc/ltYpwl+P/ogL1\n1yy1ueGx8OTWBN2iZ2EVPgRz5wKBgQDtXdPklqa2XBDJgww68qCYjS/5G1+FnSXR\nlQMjh7VzKOvHVPMVt7t68RgWBrVJPEMTgn01Q6lxU/fa2p72fAuFy0VipnZNMImQ\nEFaUTQheXCDdz5OMFgh4hQhEaVzmjmSPodROzCWh1+SjI0RnJkGfXI12H5mzcT9p\nelZdSm/SJwKBgHbeG4MLTKJr6ZVFd4EBBK2Absj/AChB6G69xobYVmbBGeLFbljI\n9m+Zw78LVk0K4jnwYTQEvDX3yd+rsTlnIOlVaBlKRo/jIqrlZCBj0/XgBMLBosqK\ndG1FAqcpIVXKIKsJKhGl8PuB6giwKYUpAY7mMUkIPlzWLjRw8OzrNjxNAoGBANiG\nuc8Q2h0J2Mppv9NaFsNyL6vWm4lf8+q+OrHDjvLxBju8M07BXsVkfqtBDUg0L5/x\nbdQT19KoXTWILm/8cepnpfC6lroNJ7+CF+iKaLLi6ZxNSXQFeU6vU/5L+tHaXMNl\nRS1oLn/3V/q1JiXOERiVLfnuE6BMEyNd1MCfMWgLAoGBAJUnPYyRgDr170VsGVRb\n0zTaxAu1j+1IQIyrA96qYtPKHORhBK6dVvBPWASpqoRioeACGyLm/GdriGuaP548\nYcl3MW1UkKUjtmQYYIvvtljGtX27VuhuvNpBPkR5iePgxAgYynB3KGe+7FGDo/cR\n3xb8jwjSk1gJf8aI98re/89L\n-----END PRIVATE KEY-----\n",
    |  "client_email": "seahorse-instance@plasma-shift-148810.iam.gserviceaccount.com",
    |  "client_id": "100547855625827865994",
    |  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    |  "token_uri": "https://accounts.google.com/o/oauth2/token",
    |  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
    |  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/seahorse-instance%40plasma-shift-148810.iam.gserviceaccount.com"
    |}
  """.stripMargin

  def writeGoogleSheet(dataframe: DataFrame, googleSheetId: GoogleSheetId): Unit = {
    val write = new WriteDataFrame()
      .setStorageType(
        new OutputStorageTypeChoice.GoogleSheet()
          .setGoogleServiceAccountCredentials(serviceAccountCredentials)
          .setGoogleSheetId(googleSheetId)
      )
    write.execute(executionContext)(Vector(dataframe))
  }

  private def readGoogleSheet(googleSheetId: GoogleSheetId): DataFrame = {
    val readDF = new ReadDataFrame()
      .setStorageType(
        new InputStorageTypeChoice.GoogleSheet()
          .setGoogleSheetId(googleSheetId)
          .setGoogleServiceAccountCredentials(serviceAccountCredentials)
      )
    readDF.execute(executionContext)(Vector.empty[DOperable]).head.asInstanceOf[DataFrame]
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
    readDF.execute(executionContext)(Vector.empty[DOperable]).head.asInstanceOf[DataFrame]
  }

}
