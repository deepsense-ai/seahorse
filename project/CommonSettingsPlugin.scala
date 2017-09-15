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

  lazy val artifactoryUrl = settingKey[String]("Artifactory URL to deploy packages to")

  override def globalSettings = Seq(
    // Set custom URL using -Dartifactory.url
    // sbt -Dartifactory.url=http://192.168.59.104/artifactory/
    artifactoryUrl := sys.props.getOrElse("artifactory.url", "http://10.10.1.77:8081/artifactory/")
  )

  override def projectSettings = Seq(
    organization := "io.deepsense",
    scalaVersion := "2.11.6",
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
  ) ++ Seq(
    publishTo := {
      val url = artifactoryUrl.value
      if (isSnapshot.value)
        Some("snapshots" at url + "deepsense-backend-snapshot")
      else
        Some("releases" at url + "deepsense-backend-release")
    },
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runTest,
      setReleaseVersion,
      publishArtifacts,
      setNextVersion
    ),
    credentials += Credentials(Path.userHome / ".artifactory_credentials"),
    git.formattedShaVersion := git.gitHeadCommit.value.map { sha =>
      val postfix = if (isSnapshot.value) "-SNAPSHOT" else ""
      git.formattedDateVersion.value + "-" + sha.substring(0, 7) + postfix
    },
    git.baseVersion <<= (version in ThisBuild),
    git.uncommittedSignifier := None,
    (version in Universal) := {
      git.formattedShaVersion.value.getOrElse((version in Universal).value)
    },
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
