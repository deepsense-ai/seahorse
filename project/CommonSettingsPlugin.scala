/**
 * Copyright (c) 2015, CodiLime Inc.
 */

import com.typesafe.sbt.SbtGit.git
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.Universal

object CommonSettingsPlugin extends AutoPlugin {
  override def trigger = allRequirements

  lazy val OurIT = config("it") extend Test

  override def globalSettings = Seq(
  )

  override def projectSettings = Seq(
    organization := "io.deepsense",
    scalaVersion := "2.11.8",
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
