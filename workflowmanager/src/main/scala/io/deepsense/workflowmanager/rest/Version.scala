/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest

import buildinfo.BuildInfo

case class Version(major: Int, minor: Int, fix: Int) {
  def humanReadable: String = {
    Seq(major, minor, fix).mkString(Version.separator)
  }

  /**
   * Tells whether the version is compatible with the current version.
   */
  def isCompatible: Boolean =
    major == Version.currentVersion.major && minor == Version.currentVersion.minor
}

object Version {
  val separator = "."
  lazy val currentVersion = Version(BuildInfo.version)

  def apply(version: String): Version = {
    val Seq(major, minor, fix) =
      version.split(separator(0)).map(_.replaceAll("[^\\d.]", "").toInt).toSeq

    Version(major, minor, fix)
  }
}

