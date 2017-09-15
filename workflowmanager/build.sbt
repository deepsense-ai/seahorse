import com.typesafe.sbt.SbtGit

// Copyright (c) 2015, CodiLime Inc.

name := "deepsense-workflowmanager"

libraryDependencies ++= Dependencies.workflowmanager

unmanagedClasspath in Runtime += (baseDirectory.value / "conf")

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

WorkflowExamples.defaultSettings
