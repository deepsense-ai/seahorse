/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import scala.util.{Failure, Success, Try}

import sbt.Keys._
import sbt._
import sbtbuildinfo.BuildInfoKey.Entry
import sbtbuildinfo.{BuildInfoKey, BuildInfoPlugin}

object DeepsenseBuildInfoPlugin extends AutoPlugin {
  override def requires: Plugins = BuildInfoPlugin

  def parseVersionString(versionString: String): (Int, Int, Int, String) = {
    // <number>.<number>.<number><optional_rest>
    val splitRegex = """([0-9]+)\.([0-9]+)\.([0-9]+)([^0-9].*)?""".r

    Try {
      versionString match {
        case splitRegex(maj, min, fix, rest) => (maj.toInt, min.toInt, fix.toInt, Option(rest).getOrElse(""))
        case _ => throw new IllegalArgumentException(
          s"Version must conform to regex given by string ${splitRegex.toString()}")
      }
    } match {
      case Success(versionTuple) => versionTuple
      case Failure(nfe: NumberFormatException) =>
        throw new IllegalArgumentException("Version must start with X.Y.Z, " +
          "where X, Y and Z are non negative integers!")

      case Failure(e) => throw e
    }
  }

  val buildInfoKeysSetting: Def.Initialize[Seq[Entry[_]]] = Def.setting {

    lazy val (maj, min, fix, rest) = parseVersionString(version.value)

    Seq(
      BuildInfoKey.action("gitCommitId") {
        Process("git rev-parse HEAD").lines.head
      },

      "apiVersionMajor" -> maj,
      "apiVersionMinor" -> min,
      "apiVersionPatch" -> fix,
      "apiVersionRest" -> rest

    )
  }


  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    BuildInfoPlugin.autoImport.buildInfoKeys ++= buildInfoKeysSetting.value
  )
}
