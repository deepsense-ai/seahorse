// Copyright (c) 2015, CodiLime Inc.

// scalastyle:off

name := "deepsense-backend"

lazy val seahorseCommons = ProjectRef(file("./seahorse-workflow-executor"), "commons")
lazy val seahorseMqProtocol = ProjectRef(file("./seahorse-workflow-executor"), "workflowexecutormqprotocol")
lazy val seahorseDeeplang = ProjectRef(file("./seahorse-workflow-executor"), "deeplang")
lazy val seahorseGraph = ProjectRef(file("./seahorse-workflow-executor"), "graph")
lazy val seahorseReportlib = ProjectRef(file("./seahorse-workflow-executor"), "reportlib")
lazy val seahorseWorkflowJson = ProjectRef(file("./seahorse-workflow-executor"), "workflowjson")

lazy val commons                = project dependsOn seahorseCommons
lazy val workflowmanager        = project dependsOn (seahorseDeeplang, seahorseGraph, seahorseReportlib, seahorseWorkflowJson, commons, commons % "test->test")
lazy val sessionmanager         = project dependsOn (seahorseMqProtocol, commons, commons % "test->test")
lazy val libraryservice         = project dependsOn (commons, commons % "test->test")

lazy val root = (project in file(".")).
  aggregate(commons, workflowmanager, sessionmanager, libraryservice)

// e2e tests are not aggregated in root, so they are not run after calling sbt tasks from root project
lazy val e2etests = project dependsOn (commons, commons % "test->test", sessionmanager, workflowmanager)

// Sequentially perform integration tests after assembling and deploying GE with dependencies jar.
// Additionally, check if all input workflows for e2e tests can be parsed - we want to have quick feedback
// if any of those workflows ceased to be correct.
addCommandAlias("ds-it",
    ";e2etests/test:testOnly io.deepsense.e2etests.AllInputWorkflowsCorrectTest " +
    ";commons/it:test " +
    ";sessionmanager/it:test " +
    ";workflowmanager/it:test " +
    ";libraryservice/it:test")

// scalastyle:on
