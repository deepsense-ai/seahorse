// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Jacek Laskowski

organization := "io.deepsense"
name         := "deepsense-deeplang"
version      := "0.1.0"
scalaVersion := "2.11.6"

scalacOptions := Seq(
  "-unchecked", "-deprecation", "-encoding", "utf8", "-feature", "-language:existentials",
  "-language:implicitConversions"
)

libraryDependencies ++= Seq(
  "org.scalatest"          %% "scalatest"     % "2.2.4" % "test",
  "com.github.nscala-time" %% "nscala-time"   % "1.8.0",
  "org.scala-lang"         %  "scala-reflect" % scalaVersion.value,
  "org.apache.spark"       %% "spark-sql"     % "1.3.0",
  "org.apache.spark"       %% "spark-core"    % "1.3.0"
)

//TODO: Extract common logic for tests separation

inConfig(Test) {
  Seq(
    testOptions := Seq(
      Tests.Filter(unitFilter),
      // Put results in target/test-reports
      Tests.Argument(TestFrameworks.ScalaTest, "-o", "-u", "target/test-reports")
    ),
    fork := true,
    unmanagedClasspath += baseDirectory.value / "conf",
    scalacOptions := Seq(
      "-unchecked", "-deprecation", "-encoding", "utf8",
      "-feature", "-language:existentials"
    )
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
