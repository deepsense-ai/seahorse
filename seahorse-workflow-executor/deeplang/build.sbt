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

import sbt.Tests.{SubProcess, Group}
import CommonSettingsPlugin._


// scalastyle:off

name := "seahorse-executor-deeplang"

// Integration tests using Spark Clusters need jar
(test in OurIT) := ((test in OurIT).dependsOn (assembly)).value

// Only one spark context per JVM
def assignTestsToJVMs(testDefs: Seq[TestDefinition]) = {
  val (forJvm1, forJvm2) = testDefs.partition(_.name.contains("ClusterDependentSpecsSuite"))

  Seq(
    Group(
      name = "tests_for_jvm_1",
      tests = forJvm1,
      runPolicy = SubProcess(ForkOptions(runJVMOptions = Seq.empty))
    ),
    Group(
      name = "test_for_jvm_2",
      tests = forJvm2,
      runPolicy = SubProcess(ForkOptions(runJVMOptions = Seq.empty))
    )
  )
}

testGrouping in OurIT := {
  val testDefinitions = (definedTests in OurIT).value
  assignTestsToJVMs(testDefinitions)
}

libraryDependencies ++= Dependencies.deeplang

// scalastyle:on
