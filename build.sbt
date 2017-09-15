// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Jacek Laskowski

name := "deepsense-backend"

lazy val commons                = project
lazy val reportlib              = project
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
  `entitystorage-model`,
  `entitystorage-client` % "test")
lazy val `entitystorage-client` = project dependsOn `entitystorage-model`
lazy val experimentmanager      = project dependsOn (
  commons,
  commons % "test->test",
  deeplang,
  graphexecutor,
  graphexecutor % "it->it",
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

addCommandAlias("ds-it",
  ";commons/it:test " +
    ";models/it:test " +
    ";deeplang/it:test " +
    ";entitystorage-model/it:test " +
    ";entitystorage/it:test " +
    ";entitystorage-client/it:test " +
    ";experimentmanager/it:test " +
    ";graph/it:test " +
    ";graphexecutor/it:test " +
    ";reportlib/it:test " +
    ";graphjson/it:test")
