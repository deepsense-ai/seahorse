// Copyright (c) 2015, CodiLime Inc.

name := "deepsense-backend"

lazy val commons                = project
lazy val `deploy-model-service` = project dependsOn (
  commons,
  commons % "test->test")
lazy val entitystorage          = project dependsOn (commons, commons % "test->test")
lazy val workflowmanager        = project dependsOn (
  commons,
  commons % "test->test",
  `deploy-model-service`)

// Sequentially perform integration tests after assembling and deploying GE with dependencies jar
addCommandAlias("ds-it",
    ";commons/it:test " +
    ";entitystorage/it:test " +
    ";workflowmanager/it:test " +
    ";deploy-model-service/it:test")

addCommandAlias("sPublish", "aetherDeploy")
addCommandAlias("sPublishLocal", "aetherInstall")
