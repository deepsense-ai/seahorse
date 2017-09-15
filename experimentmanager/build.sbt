/**
 * Copyright (c) 2015, CodiLime Inc.
 */

name := "deepsense-experimentmanager"

libraryDependencies ++= Dependencies.experimentmanager

unmanagedClasspath in Runtime += (baseDirectory.value / "conf")

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)
