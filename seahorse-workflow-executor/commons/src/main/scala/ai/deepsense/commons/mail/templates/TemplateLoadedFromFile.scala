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

package ai.deepsense.commons.mail.templates

import java.io.File

import scala.util.Try

/**
 * This class represents the templates that are loaded from File.
 *
 * @tparam T template class
 */
trait TemplateLoadedFromFile[T] extends Template[T] {

  def resolveTemplateName(templateName: String): Try[File]

  def loadTemplateFromFile(file: File): Try[T]

  override def loadTemplate(templateName: String): Try[T] = {
    resolveTemplateName(templateName)
      .flatMap(loadTemplateFromFile)
  }

}
