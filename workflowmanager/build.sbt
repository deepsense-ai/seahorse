import com.typesafe.sbt.SbtGit

// Copyright (c) 2015, CodiLime Inc.

name := "deepsense-workflowmanager"

libraryDependencies ++= Dependencies.workflowmanager

unmanagedClasspath in Runtime += (baseDirectory.value / "conf")

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

// Docker related configuration

dockerBaseImage := "anapsix/alpine-java:jre8"
dockerUpdateLatest := true
version in Docker := SbtGit.GitKeys.gitHeadCommit.value.get

WorkflowExamples.defaultSettings
