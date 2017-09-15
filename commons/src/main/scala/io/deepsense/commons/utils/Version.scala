/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.commons.utils

import scala.util.{Failure, Success, Try}

case class Version(major: Int, minor: Int, fix: Int) {
  def humanReadable: String = {
    Seq(major, minor, fix).mkString(Version.separator.toString)
  }

  /**
   * Tells whether the version are compatible.
   */
  def compatibleWith(other: Version): Boolean =
    major == other.major && minor == other.minor
}

object Version {
  val separator = '.'

  def apply(versionString: String): Version = {
    val expectedParts = 3
    val split =
      versionString.replaceAll("[^\\d.]", "").split(separator).toSeq
      .filter(_.nonEmpty).map(_.toInt)

    Try {
      assert(split.size >= expectedParts,
        s"Version must have at least $expectedParts dot-separated parts (had ${split.size}).")
      val version = Version(split.head, split(1), split(2))
      assert(versionString.startsWith(version.humanReadable),
        s"Version must start with X.Y.Z, where X, Y and Z are non negative integers!")
      version
    } match {
      case Failure(exception) => throw new VersionException(versionString, Some(exception))
      case Success(value) => value
    }
  }
}

case class VersionException(versionString: String, cause: Option[Throwable] = None)
  extends Exception(s"Could not parse version '$versionString'", cause.orNull)
