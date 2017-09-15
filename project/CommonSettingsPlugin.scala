/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
import sbt._
import Keys._

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
    resolvers ++= Dependencies.resolvers
  ) ++ ouritSettings ++ testSettings ++ Seq(
    test <<= test in Test
  )

  lazy val ouritSettings = inConfig(OurIT)(Defaults.testSettings) ++ inConfig(OurIT) {
    Seq(
      testOptions := Seq(
        // Show full stacktraces (F), Put results in target/test-reports
        Tests.Argument(TestFrameworks.ScalaTest, "-oF", "-u", "target/test-reports")
      ),
      javaOptions := Seq("-Denv=integtest", "-Dconfig.trace=loads", s"-DlogFile=${name.value}"),
      fork := true,
      unmanagedClasspath += baseDirectory.value / "conf"
    )
  }

  lazy val testSettings = inConfig(Test) {
    Seq(
      testOptions := Seq(
        // Put results in target/test-reports
        Tests.Argument(TestFrameworks.ScalaTest,
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
}
