// Copyright (c) 2015, CodiLime Inc.

name := "deepsense-backend"

lazy val commons                = project
lazy val `deploy-model-service` = project dependsOn (
  commons,
  commons % "test->test")
lazy val entitystorage          = project dependsOn (commons, commons % "test->test")
lazy val workflowmanager      = project dependsOn (
  commons,
  commons % "test->test",
  graphexecutor,
  graphexecutor % "it->it",
  `deploy-model-service`)
lazy val graphexecutor = project dependsOn (
  commons,
  commons % "test->test")


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
    ";entitystorage/it:test " +
    ";workflowmanager/it:test " +
    ";graphexecutor/it:test " +
    ";deploy-model-service/it:test")
