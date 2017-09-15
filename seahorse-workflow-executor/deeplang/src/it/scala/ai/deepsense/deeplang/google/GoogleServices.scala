/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
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
package ai.deepsense.deeplang.google

import java.io.File

import scala.io.Source

object GoogleServices {

  val googleSheetForTestsId = "1yllfTlFK6RkJfVxYp_hEnCikdwOuB0kc1v2XcDqANeo"

  def serviceAccountJson: Option[String] = {
    val file = new File(credentialsJsonFilePath())
    if(file.exists()) {
      Some(Source.fromFile(file).mkString)
    } else {
      None
    }
  }

  def credentialsJsonFilePath(): String =
    userHomePath() + "/.credentials/seahorse-tests/google-service-account.json"

  private def userHomePath() = System.getProperty("user.home")

  def serviceAccountNotExistsException() = new IllegalStateException(
    s"""Google service account json does not exist.
        |Create file with google service credential json under path:
        |${GoogleServices.credentialsJsonFilePath()}""".stripMargin
  )

}
