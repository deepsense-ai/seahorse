// Copyright (c) 2015, CodiLime Inc.

name := "deepsense-workflowmanager"

libraryDependencies ++= Dependencies.workflowmanager

unmanagedClasspath in Runtime += (baseDirectory.value / "conf")

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

// Docker related configuration

lazy val dockerRegistryAddress = "docker-registry.intra.codilime.com"
lazy val dockerNamespace = "tap"
lazy val dockerRegistry = dockerRegistryAddress + "/" + dockerNamespace

dockerBaseImage := "anapsix/alpine-java:jre8"
dockerExposedPorts := Seq(9080)
dockerRepository := Some(dockerRegistry)
dockerUpdateLatest := true
packageName in Docker := packageName.value
version in Docker := "tap-" + version.value
