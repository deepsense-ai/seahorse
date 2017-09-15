/**
 * Copyright (c) 2015, CodiLime Inc.
 */

name := "deepsense-experimentmanager"

libraryDependencies ++= Dependencies.experimentmanager

unmanagedClasspath in Runtime += (baseDirectory.value / "conf")

unmanagedClasspath in Compile += (baseDirectory.value / "conf")

Revolver.settings

Seq(filterSettings: _*)
CommonSettingsPlugin.setUpFiltersPlugin

enablePlugins(JavaAppPackaging)
