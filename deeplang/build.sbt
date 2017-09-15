/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Jacek Laskowski
 */

name := "deepsense-deeplang"

libraryDependencies ++= Dependencies.deeplang

inConfig(Test) {
  Seq(
    testOptions := Seq(
      Tests.Filter(unitFilter),
      // Put results in target/test-reports
      Tests.Argument(TestFrameworks.ScalaTest, "-o", "-u", "target/test-reports")
    ),
    fork := true,
    unmanagedClasspath += baseDirectory.value / "conf"
  )
}

lazy val IntegTest = config("it") extend(Test)
configs(IntegTest)

inConfig(IntegTest) {
  Defaults.testTasks ++ Seq(
    testOptions := Seq(
      Tests.Filter(integFilter),
      // Show full stacktraces (F), Put results in target/test-reports
      Tests.Argument(TestFrameworks.ScalaTest, "-oF", "-u", "target/test-reports")
    ),
    fork := true
  )
}

def integFilter(name: String) = name.endsWith("IntegSpec")
def unitFilter(name: String) = name.endsWith("Spec") && !integFilter(name)
