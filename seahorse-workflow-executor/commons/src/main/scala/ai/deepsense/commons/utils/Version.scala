/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.commons.utils

import scala.util.{Failure, Success, Try}

case class Version(major: Int, minor: Int, fix: Int, rest: String) {
  def humanReadable: String = {
    Seq(major, minor, fix).mkString(Version.separator.toString) + rest
  }

  /**
   * Tells whether the version are compatible.
   */
  def compatibleWith(other: Version): Boolean =
    major == other.major && minor == other.minor
}

object Version {
  val separator = '.'

  def apply(major: Int, minor: Int, fix: Int): Version = {
    Version(major, minor, fix, "")
  }

  def apply(versionString: String): Version = {
    // <number>.<number>.<number><optional_rest>
    val splitRegex = """([0-9]+)\.([0-9]+)\.([0-9]+)([^0-9].*)?""".r

    Try {
      versionString match {
        case splitRegex(maj, min, fix, rest) => Version(maj.toInt, min.toInt, fix.toInt, Option(rest).getOrElse(""))
        case _ => throw new IllegalArgumentException(
          s"Version must conform to regex given by string ${splitRegex.toString()}")
      }
    } match {
      case Success(version) => version
      case Failure(nfe: NumberFormatException) =>
        throw VersionException(versionString, Some(
            new IllegalArgumentException("Version must start with X.Y.Z, " +
              "where X, Y and Z are non negative integers!",
              nfe)))
      case Failure(e) => throw VersionException(versionString, Some(e))
    }
  }
}

case class VersionException(versionString: String, cause: Option[Throwable] = None)
  extends Exception(s"Could not parse version '$versionString'", cause.orNull)
