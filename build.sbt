// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Jacek Laskowski

name := "deepsense-backend"

lazy val commons                = project
lazy val models                 = project dependsOn graph
lazy val deeplang               = project dependsOn (
  commons,
  `entitystorage-model`,
  `entitystorage-client`)
lazy val `entitystorage-model`  = project dependsOn commons
lazy val entitystorage          = project dependsOn (
  commons,
  commons % "test->test",
  deeplang,
  `entitystorage-model`)
lazy val `entitystorage-client` = project dependsOn `entitystorage-model`
lazy val experimentmanager      = project dependsOn (
  commons,
  commons % "test->test",
  deeplang,
  graphexecutor,
  graphjson,
  models)
lazy val graph         = project dependsOn (commons, deeplang)
lazy val graphexecutor = project dependsOn (
  commons,
  deeplang,
  `entitystorage-client`,
  graph,
  models)
lazy val graphjson     = project dependsOn (commons, deeplang, graph)
