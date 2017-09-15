/**
 * Copyright 2016, deepsense.io
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
import sbtbuildinfo.BuildInfoKey.Entry
import sbtbuildinfo.{BuildInfoKey, BuildInfoPlugin}

object DeepsenseBuildInfoPlugin extends AutoPlugin {
  override def requires: Plugins = BuildInfoPlugin

  val buildInfoKeysSetting: Def.Initialize[Seq[Entry[_]]] = Def.setting {
    val slices = 3
    val versionSeparator = '.'
    lazy val versionSplit: Seq[Int] = {
      val split = version.value.replaceAll("[^\\d.]", "").split(versionSeparator).toSeq
        .filter(_.nonEmpty).map(_.toInt)
      assert(split.size == slices, assertionMessage)
      val apiVersion = split.take(slices).mkString(versionSeparator.toString)
      assert(version.value.startsWith(apiVersion), assertionMessage)
      split
    }

    lazy val assertionMessage = s"Version is set to '${version.value}' but should be in a format" +
      " X.Y.Z, where X, Y and Z are non negative integers!"

    Seq(
      BuildInfoKey.action("gitCommitId") {
        Process("git rev-parse HEAD").lines.head
      },
      BuildInfoKey.action("apiVersionMajor") {
        versionSplit.head
      },
      BuildInfoKey.action("apiVersionMinor") {
        versionSplit(1)
      },
      BuildInfoKey.action("apiVersionPatch") {
        versionSplit(2)
      }
    )
  }


  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    BuildInfoPlugin.autoImport.buildInfoKeys ++= buildInfoKeysSetting.value
  )
}
