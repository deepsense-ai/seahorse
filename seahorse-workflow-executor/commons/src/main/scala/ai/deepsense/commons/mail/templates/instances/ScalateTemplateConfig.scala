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

package ai.deepsense.commons.mail.templates.instances

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

class ScalateTemplateConfig(val masterConfig: Config) {

  import ScalateTemplateConfig._
  import ai.deepsense.commons.utils.ConfigWithDirListsImplicits._

  def this() = this(ConfigFactory.load())

  val templatesDirs: Seq[File] = masterConfig.getConfig(scalateTemplateSubConfigPath).getDirList("templates-dirs")

}

object ScalateTemplateConfig {

  def apply(masterConfig: Config): ScalateTemplateConfig = new ScalateTemplateConfig(masterConfig)
  def apply(): ScalateTemplateConfig = new ScalateTemplateConfig()

  val scalateTemplateSubConfigPath = "scalate-templates"
}
