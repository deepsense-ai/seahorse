// Copyright (c) 2015, CodiLime Inc.

name := "deepsense-deploy-model-service"

libraryDependencies ++= Dependencies.deploymodelservice

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, UniversalDeployPlugin)
enablePlugins(DeepsenseUniversalSettingsPlugin)
