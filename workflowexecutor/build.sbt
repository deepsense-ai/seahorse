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

import sbtassembly.PathList

name := "deepsense-seahorse-workflowexecutor"

libraryDependencies ++= Dependencies.workflowexecutor

unmanagedClasspath in Runtime += (baseDirectory.value / "conf")

// Necessary while assembling uber-jar (omitting MANIFEST.MF file from constituent jar files)
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF")               => MergeStrategy.discard
  case PathList("META-INF", "INDEX.LIST")                => MergeStrategy.discard
  case PathList("META-INF", "ECLIPSEF.SF")               => MergeStrategy.discard
  case PathList("META-INF", "ECLIPSEF.RSA")              => MergeStrategy.discard
  case PathList("META-INF", "DUMMY.SF")                  => MergeStrategy.discard
  case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") => MergeStrategy.concat
  case "reference.conf"                                  => MergeStrategy.concat
  case _ => MergeStrategy.first
}
// Skip test while assembling uber-jar
test in assembly := {}

// Include PyExecutor code in assembled uber-jar (under path inside jar: /pyexecutor)
unmanagedResourceDirectories in Compile += { baseDirectory.value / "../python" }

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "io.deepsense.workflowexecutor.buildinfo"

assemblyJarName in assembly := "workflowexecutor.jar"
