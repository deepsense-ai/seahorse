// Copyright (c) 2016, CodiLime Inc.

name := "deepsense-sessionmanager"

libraryDependencies ++= Dependencies.sessionmanager
resolvers ++= Dependencies.resolvers

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

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
  IO.download(urlLoc, location)
  location
}

mappings in Universal += downloadWeJar.value -> "we.jar"

dockerBaseImage := "quay.io/deepsense_io/deepsense-spark-krb:1.6.1"
dockerExposedPorts := Seq(9082)
dockerUpdateLatest := true
