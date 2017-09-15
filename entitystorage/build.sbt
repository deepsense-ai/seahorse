// Copyright (c) 2015, CodiLime Inc.

name := "deepsense-entitystorage"

libraryDependencies ++= Dependencies.entitystorage

unmanagedClasspath in Runtime += (baseDirectory.value / "conf")

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, UniversalDeployPlugin)

// Disable tgz as set by UniversalDeployPlugin
packagedArtifacts in Universal := {
  (packagedArtifacts in Universal).value.filterNot { case (artifact, _) =>
    artifact.extension == "tgz" }
}
