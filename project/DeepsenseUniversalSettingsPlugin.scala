/**
 * Copyright (c) 2015, CodiLime Inc.
 */

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

  override def requires = UniversalPlugin && GitPlugin

  override def projectSettings = Seq(
    gitVersion := {
      git.gitHeadCommit.value.getOrElse((version in Universal).value)
    },
    gitVersionFile := {
      val location = target.value / gitVersion.value
      location.getParentFile.mkdirs()
      IO.write(location, "")
      location
    },
    mappings in Universal += gitVersionFile.value -> gitVersion.value
  ) ++ SettingsHelper.makeDeploymentSettings(Universal, packageBin in Universal, "zip")
}
