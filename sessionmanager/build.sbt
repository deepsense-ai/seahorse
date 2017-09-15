// Copyright (c) 2016, CodiLime Inc.

name := "deepsense-sessionmanager"

libraryDependencies ++= Dependencies.sessionmanager
resolvers ++= Dependencies.resolvers

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

// TODO Introduce new sbt task to generate we-deps.zip

dockerBaseImage := "quay.io/deepsense_io/deepsense-spark-krb:1.6.1"
dockerExposedPorts := Seq(9082)
dockerUpdateLatest := true
