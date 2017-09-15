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

package io.deepsense.deeplang.doperations.inout

import io.deepsense.deeplang.doperations.readwritedataframe.googlestorage._
import io.deepsense.deeplang.params.{Params, StringParam}

trait GoogleSheetParams { this: Params =>

  val googleSheetId = StringParam(
    name = "Google Sheet Id",
    description = "Id of the google sheet.")

  def getGoogleSheetId(): GoogleSheetId = $(googleSheetId)
  def setGoogleSheetId(value: GoogleSheetId): this.type = set(googleSheetId, value)

  val serviceAccountCredentials = StringParam(
    name = "Google Service Account credentials JSON",
    description = "Json file representing google service account credentials to be used for accessing " +
      "google sheet.")

  def getGoogleServiceAccountCredentials(): GoogleCretendialsJson = $(serviceAccountCredentials)
  def setGoogleServiceAccountCredentials(value: GoogleCretendialsJson): this.type =
    set(serviceAccountCredentials, value)

}
