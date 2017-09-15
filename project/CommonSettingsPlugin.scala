/**
 * Copyright (c) 2015, CodiLime Inc.
 */

import sbt.Keys._
import sbt._

object CommonSettingsPlugin extends AutoPlugin {
  override def trigger = allRequirements

  override def globalSettings = Seq(
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
    resolvers ++= Dependencies.resolvers,
    crossPaths := false
  )
}
