// Copyright (c) 2015, CodiLime Inc.

import org.scalastyle.sbt._

// scalastyle:off

name := "seahorse-backend"

lazy val shWeRepoDir = file("./seahorse-workflow-executor")

lazy val seahorseApi = ProjectRef(shWeRepoDir, "api")
lazy val seahorseCommons = ProjectRef(shWeRepoDir, "commons")
lazy val seahorseMqProtocol = ProjectRef(shWeRepoDir, "workflowexecutormqprotocol")
lazy val seahorseDeeplang = ProjectRef(shWeRepoDir, "deeplang")
lazy val seahorseGraph = ProjectRef(shWeRepoDir, "graph")
lazy val seahorseReportlib = ProjectRef(shWeRepoDir, "reportlib")
lazy val seahorseWorkflowJson = ProjectRef(shWeRepoDir, "workflowjson")
lazy val seahorseWorkflowExecutor = ProjectRef(shWeRepoDir, "workflowexecutor")

lazy val backendcommons         = project dependsOn seahorseCommons
lazy val workflowmanager        = project dependsOn (seahorseDeeplang, seahorseGraph, seahorseReportlib,
  seahorseWorkflowJson, backendcommons, backendcommons % "test->test", seahorseApi)
lazy val sessionmanager         = project dependsOn (seahorseMqProtocol, backendcommons, backendcommons % "test->test",
  seahorseWorkflowJson, seahorseApi)
lazy val libraryservice         = project dependsOn (backendcommons, backendcommons % "test->test")
lazy val datasourcemanager      = project dependsOn (backendcommons, seahorseApi)
lazy val schedulingmanager      = project dependsOn (backendcommons, workflowmanager, sessionmanager) settings (
  // javax.servlet from org.eclipse.jetty.orbit needs to be excluded from schedulingmanager's
  // dependencies because it is in conflict (same package) with javax.servlet-api, which is used
  // by Jetty used by schedulingmanager.
  projectDependencies := {
    Seq(
      (projectID in backendcommons).value.exclude("org.eclipse.jetty.orbit", "javax.servlet"),
      (projectID in workflowmanager).value.exclude("org.eclipse.jetty.orbit", "javax.servlet"),
      (projectID in sessionmanager).value.exclude("org.eclipse.jetty.orbit", "javax.servlet")
    )
  })

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
    libraryservice,
    datasourcemanager,
    schedulingmanager
)

lazy val rootProjects: Seq[sbt.ProjectReference] =
    seahorseBackendProjects.map(Project.projectToRef)
lazy val root = (project in file(".")).aggregate(rootProjects:_*)


// e2e tests are not aggregated in root, so they are not run after calling sbt tasks from root project
lazy val e2etests = project dependsOn (backendcommons, backendcommons % "test->test",
  sessionmanager, workflowmanager, seahorseWorkflowExecutor, seahorseMqProtocol, seahorseWorkflowJson
)

// Server smoke tests of for services opens up port 8080
parallelExecution in ThisBuild := false


// Sequentially perform integration tests after assembling and deploying GE with dependencies jar.
// Additionally, check if all input workflows for e2e tests can be parsed - we want to have quick feedback
// if any of those workflows ceased to be correct.
addCommandAlias("ds-it",
    ";e2etests/test:testOnly ai.deepsense.e2etests.AllInputWorkflowsCorrectTest " +
    ";backendcommons/it:test " +
    ";sessionmanager/it:test " +
    ";workflowmanager/it:test " +
    ";libraryservice/it:test")

// Scalastyle config for seahorse workflow executor is different and should not be run against backend scalastyle config
// * Ex. Seahorse workflow executor have different file header (with APACHE license)
lazy val projectsForScalastyle = seahorseBackendProjects :+ e2etests
lazy val scalastyleCmd = projectsForScalastyle.flatMap(p => Seq(
    s"${p.id}/scalastyle",
    s"${p.id}/it:scalastyle",
    s"${p.id}/test:scalastyle"
)).mkString(";", " ;", "")
addCommandAlias("scalastylebackend", scalastyleCmd) // override default scalastyle task

// scalastyle:on
