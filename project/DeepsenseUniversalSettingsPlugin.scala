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

import java.util.Date

import com.typesafe.sbt.GitPlugin
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.packager.SettingsHelper
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.Universal
import sbt.Keys._
import sbt._

object DeepsenseUniversalSettingsPlugin extends AutoPlugin {

  val gitVersion = taskKey[String]("Git version")

  val gitVersionFile = taskKey[File]("Git version file")

  override def requires = CommonSettingsPlugin && UniversalPlugin && GitPlugin

  override def projectSettings = Seq(
    gitVersion := {
      git.gitHeadCommit.value.getOrElse((version in Universal).value)
    },
    gitVersionFile := {
      val location = target.value / "build-info.txt"
      location.getParentFile.mkdirs()
      IO.write(location, "BUILD DATE: " + new Date().toString + "\n")
      IO.write(location, "GIT SHA: " + gitVersion.value + "\n", append = true)
      IO.write(location, "API VERSION: " + version.value + "\n", append = true)
      location
    },
    mappings in Universal += gitVersionFile.value -> "build-info.txt"
  ) ++ Seq(
    publish := (publish dependsOn (packageBin in Universal)).value
  ) ++ SettingsHelper.makeDeploymentSettings(Universal, packageBin in Universal, "zip")
}
