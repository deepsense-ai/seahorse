/**
 * Copyright (c) 2015, CodiLime Inc.
 */

name := "deepsense-experimentmanager"

libraryDependencies ++= Dependencies.experimentmanager

unmanagedClasspath in Runtime += (baseDirectory.value / "conf")

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, UniversalDeployPlugin)

// Disable tgz as set by UniversalDeployPlugin
packagedArtifacts in Universal := {
  (packagedArtifacts in Universal).value.filterNot { case (artifact, _) =>
    artifact.extension == "tgz" }
}
