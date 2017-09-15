/**
 * Copyright (c) 2015, CodiLime Inc.
 */

import com.typesafe.sbt.GitPlugin
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.Universal
import sbt.Keys._
import sbt._

object DeepsenseUniversalSettingsPlugin extends AutoPlugin {

  val gitVersion = taskKey[String]("Git version")

  val gitVersionData = taskKey[File]("Git version file")

  override def requires = UniversalPlugin && GitPlugin

  override def projectSettings = Seq(
    // Disable tgz as set by UniversalDeployPlugin
    packagedArtifacts in Universal := {
      (packagedArtifacts in Universal).value.filterNot { case (artifact, _) =>
        artifact.extension == "tgz" }
    },
    gitVersion := {
      git.gitHeadCommit.value.getOrElse((version in Universal).value)
    },
    gitVersionData := {
      val location = target.value / gitVersion.value
      location.getParentFile.mkdirs()
      IO.write(location, "")
      location
    },
    mappings in Universal += gitVersionData.value -> gitVersion.value
  )
}
