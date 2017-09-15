// Copyright (c) 2015, CodiLime Inc.

import org.scalastyle.sbt._

// scalastyle:off

name := "deepsense-backend"

lazy val seahorseCommons = ProjectRef(file("./seahorse-workflow-executor"), "commons")
lazy val seahorseMqProtocol = ProjectRef(file("./seahorse-workflow-executor"), "workflowexecutormqprotocol")
lazy val seahorseDeeplang = ProjectRef(file("./seahorse-workflow-executor"), "deeplang")
lazy val seahorseGraph = ProjectRef(file("./seahorse-workflow-executor"), "graph")
lazy val seahorseReportlib = ProjectRef(file("./seahorse-workflow-executor"), "reportlib")
lazy val seahorseWorkflowJson = ProjectRef(file("./seahorse-workflow-executor"), "workflowjson")
lazy val seahorseWorkflowExecutor = ProjectRef(file("./seahorse-workflow-executor"), "workflowexecutor")

lazy val backendcommons         = project dependsOn seahorseCommons
lazy val workflowmanager        = project dependsOn (seahorseDeeplang, seahorseGraph, seahorseReportlib,
  seahorseWorkflowJson, backendcommons, backendcommons % "test->test")
lazy val sessionmanager         = project dependsOn (seahorseMqProtocol, backendcommons, backendcommons % "test->test")
lazy val libraryservice         = project dependsOn (backendcommons, backendcommons % "test->test")

lazy val seahorseWorkflowExecutorProjects = Seq(
    seahorseCommons,
    seahorseMqProtocol,
    seahorseDeeplang,
    seahorseGraph,
    seahorseReportlib,
    seahorseWorkflowJson,
    seahorseWorkflowExecutor
)

lazy val seahorseBackendProjects = Seq(
    backendcommons,
    workflowmanager,
    sessionmanager,
    libraryservice
)

lazy val rootProjects: Seq[sbt.ProjectReference] =
    seahorseBackendProjects.map(Project.projectToRef)
lazy val root = (project in file(".")).aggregate(rootProjects:_*)


// e2e tests are not aggregated in root, so they are not run after calling sbt tasks from root project
lazy val e2etests = project dependsOn (backendcommons, backendcommons % "test->test",
  sessionmanager, workflowmanager, seahorseWorkflowExecutor, seahorseMqProtocol, seahorseWorkflowJson
)


// Sequentially perform integration tests after assembling and deploying GE with dependencies jar.
// Additionally, check if all input workflows for e2e tests can be parsed - we want to have quick feedback
// if any of those workflows ceased to be correct.
addCommandAlias("ds-it",
    ";e2etests/test:testOnly io.deepsense.e2etests.AllInputWorkflowsCorrectTest " +
    ";backendcommons/it:test " +
    ";sessionmanager/it:test " +
    ";workflowmanager/it:test " +
    ";libraryservice/it:test")

// Scalastyle config for seahorse workflow executor is different and should not be run against backend scalastyle config
// * Ex. Seahorse workflow executor have different file header (with APACHE license)
lazy val projectsForScalastyle = seahorseBackendProjects // :+ e2etests // TODO Make e2etest compliant with scalastyle
lazy val scalastyleCmd = projectsForScalastyle.flatMap(p => Seq(
    s"${p.id}/scalastyle",
    s"${p.id}/it:scalastyle"
//    s"${p.id}/test:scalastyle" // TODO Make test code compliant with scalastyle
)).mkString(";", " ;", "")
addCommandAlias("scalastylebackend", scalastyleCmd) // override default scalastyle task

// scalastyle:on
