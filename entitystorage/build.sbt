// Copyright (c) 2015, CodiLime Inc.

name := "deepsense-entitystorage"

libraryDependencies ++= Dependencies.entitystorage

unmanagedClasspath in Runtime += (baseDirectory.value / "conf")

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)
