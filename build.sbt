// Copyright (c) 2015, CodiLime Inc.

name := "ds-seahorse"

lazy val commons                = project
lazy val `entitystorage-client` = project dependsOn `entitystorage-model`
lazy val `entitystorage-model`  = project dependsOn commons
lazy val reportlib              = project
lazy val workflowexecutor       = project
