/**
 * Copyright (c) 2015, CodiLime Inc.
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
  ) ++ SettingsHelper.makeDeploymentSettings(Universal, packageBin in Universal, "zip")
}
