/**
 * Copyright (c) 2015, CodiLime Inc.
 */

import sbt.Keys._
import sbt._
import sbtfilter.Plugin._

object CommonSettingsPlugin extends AutoPlugin {
  override def trigger = allRequirements

  lazy val OurIT = config("it") extend Test

  override lazy val projectSettings = Seq(
    organization  := "io.deepsense",
    version       := "0.1.0",
    scalaVersion  := "2.11.6",
    scalacOptions := Seq(
      "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
      "-language:existentials", "-language:implicitConversions"
    ),
    javacOptions ++= Seq(
      "-source", "1.7",
      "-target", "1.7"
    ),
    resolvers ++= Dependencies.resolvers
  ) ++ ouritSettings ++ testSettings ++ Seq(
    test <<= test in Test
  )

  lazy val ouritSettings = inConfig(OurIT)(Defaults.testSettings) ++ inConfig(OurIT) {
    Seq(
      testOptions ++= Seq(
        // Show full stacktraces (F), Put results in target/test-reports
        Tests.Argument(TestFrameworks.ScalaTest, "-oF", "-u", "target/test-reports")
      ),
      javaOptions ++= Seq("-Denv=integtest", s"-DlogFile=${name.value}"),
      fork := true,
      unmanagedClasspath += baseDirectory.value / "conf"
    )
  }

  lazy val testSettings = inConfig(Test) {
    Seq(
      testOptions := Seq(
        // Put results in target/test-reports
        Tests.Argument(
          TestFrameworks.ScalaTest,
          "-o",
          "-u", "target/test-reports",
          "-y", "org.scalatest.FlatSpec",
          "-y", "org.scalatest.WordSpec",
          "-y", "org.scalatest.FunSuite"
        )
      ),
      fork := true,
      javaOptions := Seq("-Denv=test", s"-DlogFile=${name.value}"),
      unmanagedClasspath += baseDirectory.value / "conf"
    )
  }

  override def projectConfigurations = OurIT +: super.projectConfigurations

  lazy val filtersDirectory =
    if (System.getProperty("env") != null) System.getProperty("env") else "local"
  lazy val entityStorageIp =
    if (System.getProperty("es") != null) System.getProperty("es") else "172.28.128.1"
  lazy val runningExperimentsIp =
    if (System.getProperty("re") != null) System.getProperty("re") else "172.28.128.1"
  import FilterKeys._
  lazy val setUpFiltersPlugin = Seq(
    filterDirectoryName := s"filters/$filtersDirectory",
    includeFilter in (Compile, filters) ~= { f => f || ("*.props" | "*.conf") },
    includeFilter in (Compile, filterResources) ~= { f => f || ("*.props" | "*.conf") },
    extraProps += "entityStorage.hostname" -> entityStorageIp,
    extraProps += "runningExperiments.hostname" -> runningExperimentsIp
  )
}
