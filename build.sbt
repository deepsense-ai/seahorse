// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Jacek Laskowski

name := "deepsense-backend"

lazy val commons                = project
lazy val models                 = project dependsOn graph
lazy val deeplang               = project dependsOn commons
lazy val entitystorage          = project dependsOn (commons, commons % "test->test", deeplang, models)
lazy val `entitystorage-client` = project dependsOn models
lazy val experimentmanager      = project dependsOn (
  commons,
  commons % "test->test",
  deeplang,
  graphexecutor,
  graphjson,
  models)
lazy val graph         = project dependsOn (commons, deeplang)
lazy val graphexecutor = project dependsOn (commons, deeplang, `entitystorage-client`, graph, models)
lazy val graphjson     = project dependsOn (commons, deeplang, graph)
