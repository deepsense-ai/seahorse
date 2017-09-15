// Copyright (c) 2016, CodiLime Inc.

name := "deepsense-sessionmanager"

libraryDependencies ++= Dependencies.sessionmanager

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)
