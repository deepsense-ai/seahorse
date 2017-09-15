// Copyright (c) 2015, CodiLime, Inc.
//
// Owner: Grzegorz Chilkiewicz

import sbtassembly.PathList

name := "deepsense-graphexecutor"

libraryDependencies ++= Dependencies.graphexecutor

// Activate sbt-avro plugin
sbtavro.SbtAvro.avroSettings
// Set Avro version for Avro compiler
version in avroConfig := Version.avro

// Necessary while assembling uber-jar (omitting MANIFEST.MF file from constituent jar files)
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", "INDEX.LIST") => MergeStrategy.discard
  case PathList("META-INF", "ECLIPSEF.SF") => MergeStrategy.discard
  case PathList("META-INF", "ECLIPSEF.RSA") => MergeStrategy.discard
  case _ => MergeStrategy.first
}
// Skip test while assembling uber-jar
test in assembly := {}


// Configuration for test and it:test tasks
inConfig(Test) {
  Seq(
    testOptions := Seq(
      Tests.Filter(unitFilter),
      // Put results in target/test-reports
      Tests.Argument(TestFrameworks.ScalaTest, "-o", "-u", "target/test-reports")
    ),
    fork := true,
    javaOptions := Seq("-Denv=test"),
    unmanagedClasspath += baseDirectory.value / "conf",
    scalacOptions := Seq(
      "-unchecked", "-deprecation", "-encoding", "utf8",
      "-feature", "-language:existentials"
    )
  )
}

lazy val IntegTest = config("it") extend(Test)
configs(IntegTest)

inConfig(IntegTest) {
  Defaults.testTasks ++ Seq(
    testOptions := Seq(
      Tests.Filter(integFilter),
      // Show full stacktraces (F), Put results in target/test-reports
      Tests.Argument(TestFrameworks.ScalaTest, "-oF", "-u", "target/test-reports")
    ),
    javaOptions := Seq("-Denv=integtest"),
    fork := true
  )
}

def integFilter(name: String) = name.endsWith("IntegSuite")
def unitFilter(name: String) = name.endsWith("Suite") && !integFilter(name)

// Always perform assembly task before it:test
test in IntegTest <<= (test in IntegTest) dependsOn assembly
