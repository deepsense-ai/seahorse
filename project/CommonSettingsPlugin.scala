/**
 * Copyright (c) 2015, CodiLime Inc.
 */

import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.Universal
import sbt.Keys._
import sbt._

object CommonSettingsPlugin extends AutoPlugin {
  override def trigger = allRequirements

  lazy val OurIT = config("it") extend Test

  override def globalSettings = Seq(
    scalaVersion := "2.11.8"
  )

  override def projectSettings = Seq(
    organization := "io.deepsense",
    scalacOptions := Seq(
      "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
      "-language:existentials", "-language:implicitConversions"
    ),
    javacOptions ++= Seq(
      "-source", "1.7",
      "-target", "1.7"
    ),
    resolvers ++= Dependencies.resolvers,
    crossPaths := false
  ) ++ ouritSettings ++ testSettings ++ Seq(
    test <<= test in Test
  ) ++ Seq(
    publish <<= publish dependsOn (packageBin in Universal)
  )

  lazy val ouritSettings = inConfig(OurIT)(Defaults.testSettings) ++ inConfig(OurIT) {
    Seq(
      testOptions ++= Seq(
        // Show full stacktraces (F), Put results in target/test-reports
        Tests.Argument(TestFrameworks.ScalaTest, "-oF", "-u", "target/test-reports")
      ),
      javaOptions := Seq(s"-DlogFile=${name.value}"),
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
          "-u", "target/test-reports"
        )
      ),
      fork := true,
      javaOptions := Seq(s"-DlogFile=${name.value}"),
      unmanagedClasspath += baseDirectory.value / "conf"
    )
  }

  override def projectConfigurations = OurIT +: super.projectConfigurations
}
