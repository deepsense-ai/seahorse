// Copyright (c) 2016, CodiLime Inc.

name := "deepsense-sessionmanager"

libraryDependencies ++= Dependencies.sessionmanager

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

// Docker-related configuration

lazy val dockerRegistryAddress = "docker-registry.intra.codilime.com"
lazy val dockerNamespace = "tap"
lazy val dockerRegistry = dockerRegistryAddress + "/" + dockerNamespace

dockerBaseImage := "anapsix/alpine-java:jre8"
dockerExposedPorts := Seq(9082)
dockerRepository := Some(dockerRegistry)
dockerUpdateLatest := true
packageName in Docker := packageName.value
version in Docker := "tap-" + version.value
