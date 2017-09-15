// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Jacek Laskowski

name := "deepsense-backend"

lazy val commons           = project
lazy val models            = project dependsOn graph
lazy val deeplang          = project dependsOn commons
lazy val entitystorage     = project dependsOn (commons, commons % "test->test", deeplang, models)
lazy val experimentmanager = project dependsOn (
  commons,
  commons % "test->test",
  deeplang,
  graphjson,
  graphexecutor,
  models)
lazy val graph             = project dependsOn (commons, deeplang)
lazy val graphexecutor     = project dependsOn (commons, graph, entitystorage, deeplang, models)
lazy val graphjson         = project dependsOn (commons, graph, deeplang)
