/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

name := "deepsense-experimentmanager"

libraryDependencies ++= Dependencies.experimentmanager

unmanagedClasspath in Runtime += (baseDirectory.value / "conf")

unmanagedClasspath in Compile += (baseDirectory.value / "conf")

Revolver.settings

test in IntegrationTest <<= (test in IntegrationTest) dependsOn (assembly in LocalProject("graphexecutor"))

Seq(filterSettings: _*)
CommonSettingsPlugin.setUpFiltersPlugin

enablePlugins(JavaAppPackaging)
