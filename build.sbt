// Copyright (c) 2015, CodiLime Inc.

// scalastyle:off

name := "deepsense-backend"

lazy val commons                = project
lazy val workflowmanager        = project dependsOn (commons, commons % "test->test")
lazy val sessionmanager         = project dependsOn (commons, commons % "test->test")
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

val printWarning = (s: State) => {
  // TODO Make compile depending on publishWeClasses instead
  println(
    """
      |****************************
      |******** ATTENTION *********
      |****************************
      |**
      |** Run `sbt publishWeClasses` first to solve resolver problems with workflow executor packages
      |**
      |****************************
    """.stripMargin)
  s
}
onLoad in Global ~= (printWarning compose _)

// scalastyle:on