// Copyright (c) 2016, CodiLime Inc.

name := "deepsense-sessionmanager"

libraryDependencies ++= Dependencies.sessionmanager
resolvers ++= Dependencies.resolvers

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

// Docker-related configuration

dockerBaseImage := "anapsix/alpine-java:jre8"
dockerExposedPorts := Seq(9082)
dockerUpdateLatest := true
