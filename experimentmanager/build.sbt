/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */
name := "Experiment Manager"
version := "0.1.0"
scalaVersion := "2.10.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

Revolver.settings

inConfig(Test) { 
  Seq(
    testOptions := Seq(
      Tests.Filter(unitFilter),
      // Put results in target/test-reports
      Tests.Argument(TestFrameworks.ScalaTest, "-o", "-u", "target/test-reports")
    ),
    fork := true,
    javaOptions := Seq("-Denv=test"),
    unmanagedClasspath += baseDirectory.value / "conf"
  )
}

unmanagedClasspath in Runtime += baseDirectory.value / "conf"

resolvers ++= Seq(
  "typesafe.com" at "http://repo.typesafe.com/typesafe/repo/",
  "sonatype.org" at "https://oss.sonatype.org/content/repositories/releases",
  "spray.io" at "http://repo.spray.io"
)

libraryDependencies ++= {
  val akkaV      = "2.3.9"
  val sprayV     = "1.3.2"
  val scalaTestV = "3.0.0-SNAP4"
  val jsr305V    = "3.0.0"
  Seq(
    "org.apache.jclouds" %  "jclouds-all"   % "1.8.1",
    "net.codingwell"     %% "scala-guice"   % "3.0.2",
    "io.spray"           %% "spray-can"     % sprayV,
    "io.spray"           %% "spray-routing" % sprayV,
    "io.spray"           %% "spray-json"    % "1.3.1",
    "com.typesafe.akka"  %% "akka-actor"    % akkaV,
    "io.spray"           %% "spray-testkit" % sprayV     % "test",
    "com.typesafe.akka"  %% "akka-testkit"  % akkaV      % "test",
    "org.mockito"        %  "mockito-core"  % "1.10.19"  % "test",
    "org.scalatest"      %% "scalatest"     % scalaTestV % "test"
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
    javaOptions := Seq("-Denv=integtest"),
    fork := true
  )
}

def integFilter(name: String) = name.endsWith("IntegSpec")
def unitFilter(name: String) = name.endsWith("Spec") && !integFilter(name)
