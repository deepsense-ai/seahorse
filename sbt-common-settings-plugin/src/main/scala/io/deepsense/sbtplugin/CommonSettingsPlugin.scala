/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.sbtplugin

import sbt._
import Keys._

object CommonSettingsPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override lazy val projectSettings = Seq(
    organization  := "io.deepsense",
    version       := "0.1.0",
    scalaVersion  := "2.11.6",
    scalacOptions := Seq(
      "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
      "-language:existentials", "-language:implicitConversions"
    )
  )
}
