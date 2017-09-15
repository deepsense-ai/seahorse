// Copyright (c) 2015, CodiLime Inc.

name := "deepsense-backend"

lazy val commons                = project
lazy val reportlib              = project
lazy val `deploy-model-service` = project dependsOn (
  commons % "test->test")
lazy val models                 = project dependsOn graph
lazy val deeplang               = project dependsOn (
  commons,
  `deploy-model-service`,
  `entitystorage-model`,
  `entitystorage-client`,
  reportlib)
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
  models,
  `deploy-model-service`)
lazy val graph         = project dependsOn (commons, deeplang)
lazy val graphexecutor = project dependsOn (
  commons,
  deeplang,
  deeplang % "test->test",
  `entitystorage-client`,
  graph,
  models)
lazy val graphjson     = project dependsOn (commons, deeplang, graph)

addCommandAlias("deployOnly",
  ";graphexecutor/runMain io.deepsense.graphexecutor.deployment.DeployOnHdfs")
addCommandAlias("deploy", ";graphexecutor/assembly ;deployOnly")

addCommandAlias("ds-it",
  ";deploy " +
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
    ";graphjson/it:test " +
    ";deploy-model-service/it:test")
