// Copyright (c) 2015, CodiLime Inc.

name := "ds-seahorse"

lazy val commons                = project
lazy val deeplang               = project dependsOn (
  commons,
  `entitystorage-client`,
  `entitystorage-model`,
  reportlib)
lazy val `entitystorage-client` = project dependsOn `entitystorage-model`
lazy val `entitystorage-model`  = project dependsOn commons
lazy val graph                  = project dependsOn (commons, deeplang, reportlib)
lazy val graphjson              = project dependsOn (commons, deeplang, graph, models)
lazy val models                 = project dependsOn (commons, graph)
lazy val reportlib              = project
lazy val workflowexecutor       = project
