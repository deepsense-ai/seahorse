/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.params

import spray.json._

/**
 * Represents language of code snippet
 * (it could be used for syntax validation and syntax highlighting in frontend).
 */
@SerialVersionUID(1)
case class CodeSnippetLanguage(language: CodeSnippetLanguage.CodeSnippetLanguage) {

  final def toJson: JsObject = {
    import spray.json.DefaultJsonProtocol._
    JsObject("name" -> language.toString.toJson)
  }
}

object CodeSnippetLanguage extends Enumeration {
  type CodeSnippetLanguage = Value
  val python = Value("python")
  val sql = Value("sql")
  val r = Value("r")
}
