// Copyright (c) 2016, CodiLime Inc.

name := "seahorse-e2etests"

libraryDependencies ++= Dependencies.integrationtests

Revolver.settings

enablePlugins(GitVersioning, DeepsenseUniversalSettingsPlugin)
