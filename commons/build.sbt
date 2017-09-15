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

name := "deepsense-seahorse-commons"

libraryDependencies ++= Dependencies.commons
resolvers ++= Dependencies.resolvers

Revolver.settings

inConfig(Test) {
  Seq(
    testOptions := Seq(
      Tests.Filter(unitFilter),
      // Put results in target/test-reports
      Tests.Argument(TestFrameworks.ScalaTest, "-o", "-u", "target/test-reports")
    ),
    fork := true,
    javaOptions := Seq("-Denv=test", s"-DlogFile=${name.value}"),
    unmanagedClasspath += baseDirectory.value / "conf",
    scalacOptions := Seq(
      "-unchecked", "-deprecation", "-encoding", "utf8",
      "-feature", "-language:existentials"
    )
  )
}

unmanagedClasspath in Runtime += baseDirectory.value / "conf"

lazy val IntegTest = config("it") extend Test
configs(IntegTest)

inConfig(IntegTest) {
  Defaults.testTasks ++ Seq(
    testOptions := Seq(
      Tests.Filter(integFilter),
      // Show full stacktraces (F), Put results in target/test-reports
      Tests.Argument(TestFrameworks.ScalaTest, "-oF", "-u", "target/test-reports")
    ),
    javaOptions := Seq("-Denv=integtest", s"-DlogFile=${name.value}"),
    fork := true
  )
}

def integFilter(name: String) = name.endsWith("IntegSpec")
def unitFilter(name: String) = name.endsWith("Spec") && !integFilter(name)

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "io.deepsense.commons"

buildInfoKeys ++= {
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
    " X.Y.Z, where X and Y are non negative integers!"

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
