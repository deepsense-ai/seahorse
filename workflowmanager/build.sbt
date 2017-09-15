// Copyright (c) 2015, CodiLime Inc.

name := "deepsense-workflowmanager"

libraryDependencies ++= Dependencies.workflowmanager

unmanagedClasspath in Runtime += (baseDirectory.value / "conf")

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

// Docker related configuration

dockerBaseImage := "docker-repo.deepsense.codilime.com/intel/tap-base-java:java8-jessie"
dockerExposedPorts := Seq(9080)
dockerUpdateLatest := true
