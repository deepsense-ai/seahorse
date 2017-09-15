// Copyright (c) 2015, CodiLime Inc.

name := "deepsense-backend"

lazy val commons                = project
lazy val models                 = project dependsOn (commons, graph)
lazy val deeplang               = project dependsOn (commons)
lazy val `deploy-model-service` = project dependsOn (
  commons,
  commons % "test->test",
  deeplang)
lazy val `entitystorage-model`  = project dependsOn commons
lazy val entitystorage          = project dependsOn (
  commons,
  commons % "test->test",
  deeplang)
lazy val workflowmanager      = project dependsOn (
  commons,
  commons % "test->test",
  deeplang,
  graphexecutor,
  graphexecutor % "it->it",
  graphjson,
  graphjson % "it->it",
  models,
  `deploy-model-service`)
lazy val graph         = project dependsOn (commons, deeplang)
lazy val graphexecutor = project dependsOn (
  commons,
  commons % "test->test",
  deeplang,
  deeplang % "test->test",
  graph,
  models)
lazy val graphjson     = project dependsOn (commons, deeplang, graph, models)


// Assembly and deploy GE without dependencies jar
addCommandAlias("deployGeWithoutDeps",
  ";graphexecutor/assembly " +
    ";graphexecutor/runMain io.deepsense.graphexecutor.deployment.DeployOnHdfs deployGeWithoutDeps")
addCommandAlias("deployGeNoDeps", ";deployGeWithoutDeps")
addCommandAlias("deployQuick", ";deployGeWithoutDeps")

// Deploy only (without assembling) GE with dependencies jar
addCommandAlias("deployOnly",
  ";graphexecutor/runMain io.deepsense.graphexecutor.deployment.DeployOnHdfs deployGeWithDeps")

// Assembly and deploy GE with dependencies jar
addCommandAlias("deployGeWithDeps",
  ";graphexecutor/assembly ;graphexecutor/assemblyPackageDependency ;deployOnly")
addCommandAlias("deploy", ";deployGeWithDeps")

// Sequentially perform integration tests after assembling and deploying GE with dependencies jar
addCommandAlias("ds-it",
  ";deploy " +
    ";commons/it:test " +
    ";models/it:test " +
    ";deeplang/it:test " +
    ";entitystorage/it:test " +
    ";workflowmanager/it:test " +
    ";graph/it:test " +
    ";graphexecutor/it:test " +
    ";graphjson/it:test " +
    ";deploy-model-service/it:test")
