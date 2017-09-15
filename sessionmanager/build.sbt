// Copyright (c) 2016, CodiLime Inc.

import com.typesafe.sbt.packager.docker._

name := "deepsense-sessionmanager"

libraryDependencies ++= Dependencies.sessionmanager
resolvers ++= Dependencies.resolvers

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

// If there are many `App` objects in project, docker image will crash with cryptic message
mainClass in Compile := Some("io.deepsense.sessionmanager.SessionManagerApp")

// TODO Introduce new sbt task to generate we-deps.zip

val downloadWeJar = taskKey[File]("Downloads the latest we.jar")

downloadWeJar := {
  val location = target.value / "downloads" / "we.jar"
  location.getParentFile.mkdirs()
  val scalaMajorMinorOption = CrossVersion.partialVersion(scalaVersion.value)
  val scalaMajorMinor = scalaMajorMinorOption.get._1 + "." + scalaMajorMinorOption.get._2

  val urlLoc = if (isSnapshot.value) {
    url(s"${CommonSettingsPlugin.artifactoryUrl.value}seahorse-workflowexecutor-snapshot/" +
      s"io/deepsense/workflowexecutor_${scalaMajorMinor}/${version.value}/" +
      s"${version.value}-latest/workflowexecutor_${scalaMajorMinor}-${version.value}-latest.jar")
  } else {
    url(s"${CommonSettingsPlugin.artifactoryUrl.value}seahorse-workflowexecutor-release/" +
      s"io/deepsense/workflowexecutor_${scalaMajorMinor}/${version.value}/" +
      s"workflowexecutor_${scalaMajorMinor}-${version.value}.jar")
  }
  val weLoc = sys.props.get("workflowexecutor.jar").map(url(_)).getOrElse(urlLoc)
  IO.download(weLoc, location)
  location
}

mappings in Universal += downloadWeJar.value -> "we.jar"

val preparePythonDeps = taskKey[File]("Generates we_deps.zip file with python dependencies")

preparePythonDeps := {
  "sessionmanager/prepare-deps.sh" !

  target.value / "we-deps.zip"
}

preparePythonDeps <<= preparePythonDeps dependsOn downloadWeJar

mappings in Universal += preparePythonDeps.value -> "we-deps.zip"

dockerBaseImage := "docker-repo.deepsense.codilime.com/deepsense_io/deepsense-mesos-spark"
dockerCommands ++= Seq(
  Cmd("USER", "root"),
  ExecCmd("ENTRYPOINT", "/opt/startup.sh"),
  ExecCmd("CMD", "bin/deepsense-sessionmanager")
)
dockerUpdateLatest := true
