// Copyright (c) 2015, CodiLime Inc.

name := "deepsense-backend"

lazy val commons                = project
lazy val workflowmanager        = project dependsOn (commons, commons % "test->test")
lazy val sessionmanager         = project dependsOn (commons, commons % "test->test")

lazy val root = (project in file(".")).
  aggregate(commons, workflowmanager, sessionmanager)

// e2e tests are not aggregated in root, so they are not run after calling sbt tasks from root project
lazy val e2etests = project dependsOn (commons, commons % "test->test")

// Sequentially perform integration tests after assembling and deploying GE with dependencies jar
addCommandAlias("ds-it",
    ";commons/it:test " +
    ";sessionmanager/it:test " +
    ";workflowmanager/it:test ")

addCommandAlias("sPublish", "aetherDeploy")
addCommandAlias("sPublishLocal", "aetherInstall")
