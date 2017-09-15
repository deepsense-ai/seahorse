/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */
import sbt._
import Keys._
import spray.revolver.RevolverPlugin._

lazy val commonSettings = Seq(
  name := "Experiment Manager",
  version := "0.1.0",
  scalaVersion := "2.10.4"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(Revolver.settings: _*).
  configs(IntegTest).
  settings(inConfig(IntegTest)(Defaults.testTasks) : _*).
  settings(
    testOptions in Test := Seq(
      Tests.Filter(unitFilter),
      // Put results in target/test-reports
      Tests.Argument(TestFrameworks.ScalaTest, "-o", "-u", "target/test-reports")
    ),
    testOptions in IntegTest := Seq(
      Tests.Filter(integFilter),
      // Show full stacktraces (F), Put results in target/test-reports
      Tests.Argument(TestFrameworks.ScalaTest, "-oF", "-u", "target/test-reports")
    ),
    javaOptions in Test := Seq("-Denv=test"),
    fork in Test := true,
    javaOptions in IntegTest := Seq("-Denv=integtest"),
    fork in IntegTest := true,
    unmanagedClasspath in Test += baseDirectory.value / "conf",
    unmanagedClasspath in Runtime += baseDirectory.value / "conf",
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    resolvers ++= Seq(
      "typesafe.com" at "http://repo.typesafe.com/typesafe/repo/",
      "sonatype.org" at "https://oss.sonatype.org/content/repositories/releases",
      "spray.io" at "http://repo.spray.io"
    ),
    libraryDependencies ++= {
      val akkaV = "2.3.7"
      val sprayV = "1.3.2"
      val scalaTestV = "2.2.+"
      Seq(
        "com.google.code.findbugs" % "jsr305" % "1.3.9" % "provided",
        "net.codingwell" %% "scala-guice" % "3.0.2",
        "io.spray" %% "spray-can" % sprayV,
        "io.spray" %% "spray-routing" % sprayV,
        "io.spray" %% "spray-json" % "1.3.1",
        "io.spray" %% "spray-testkit" % sprayV % "test",
        "com.typesafe.akka" %% "akka-actor" % akkaV,
        "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
        "org.specs2" %% "specs2-core" % "2.3.7" % "test",
        "org.mockito" % "mockito-core" % "1.10.19" % "test",
        "org.scalatest" %% "scalatest" % scalaTestV % "test",
        "org.apache.jclouds" % "jclouds-all" % "1.8.1")
    }
  )

lazy val IntegTest = config("integ") extend(Test)

def integFilter(name: String): Boolean = name endsWith "IntegSpec"

def unitFilter(name: String): Boolean = (name endsWith "Spec") && !integFilter(name)
