/**
 * Copyright 2015, CodiLime Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt.Keys._
import sbt._

object CommonSettingsPlugin extends AutoPlugin {
  override def trigger = allRequirements

  lazy val OurIT = config("it") extend Test

  lazy val artifactoryUrl = settingKey[String]("Artifactory URL to deploy packages to")

  override def globalSettings = Seq(
    // Set custom URL using -Dartifactory.url
    // sbt -Dartifactory.url=http://192.168.59.104/artifactory/
    artifactoryUrl := sys.props.getOrElse("artifactory.url", "http://10.10.1.77:8081/artifactory/")
  )

  override def projectSettings = Seq(
    organization := "io.deepsense",
    // Default scala version
    // TODO: Fix GraphSuite.scala and use here: scalaVersion := "2.10.5",
    scalaVersion := "2.11.6",
    // Scala versions used for cross-builds
    // (use `sbt clean "+ publish"` to publish using scala 2.11.6)
    crossScalaVersions := Seq("2.11.6"),
    scalacOptions := Seq(
      "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
      "-language:existentials", "-language:implicitConversions"
    ),
    javacOptions ++= Seq(
      "-source", "1.7",
      "-target", "1.7"
    ),
    resolvers ++= Dependencies.resolvers,
    // Disable using the Scala version in output paths and artifacts
    crossPaths := false
  ) ++ ouritSettings ++ testSettings ++ Seq(
    test <<= test in Test
  ) ++ Seq(
    publishTo := {
      val url = artifactoryUrl.value
      if (isSnapshot.value)
        Some("snapshots" at url + "deepsense-seahorse-snapshot")
      else
        Some("releases" at url + "deepsense-seahorse-release")
    },
    credentials += Credentials(Path.userHome / ".artifactory_credentials")
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
          "-u", "target/test-reports",
          "-y", "org.scalatest.FlatSpec",
          "-y", "org.scalatest.WordSpec",
          "-y", "org.scalatest.FunSuite"
        )
      ),
      fork := true,
      javaOptions := Seq(s"-DlogFile=${name.value}"),
      unmanagedClasspath += baseDirectory.value / "conf"
    )
  }

  override def projectConfigurations = OurIT +: super.projectConfigurations
}
