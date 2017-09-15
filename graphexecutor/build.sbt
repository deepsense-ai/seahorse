// Copyright (c) 2015, CodiLime, Inc.

import sbtassembly.PathList

name := "deepsense-graphexecutor"

libraryDependencies ++= Dependencies.graphexecutor

// Activate sbt-avro plugin
sbtavro.SbtAvro.avroSettings
// Set Avro version for Avro compiler
version in avroConfig := Version.avro

// Necessary while assembling uber-jar (omitting MANIFEST.MF file from constituent jar files)
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF")  => MergeStrategy.discard
  case PathList("META-INF", "INDEX.LIST")   => MergeStrategy.discard
  case PathList("META-INF", "ECLIPSEF.SF")  => MergeStrategy.discard
  case PathList("META-INF", "ECLIPSEF.RSA") => MergeStrategy.discard
  case _ => MergeStrategy.first
}
// Skip test while assembling uber-jar
test in assembly := {}

// Make assembly task create jar with only DeepSense.io code inside
assemblyOption in assembly :=
  (assemblyOption in assembly).value.copy(includeScala = false, includeDependency = false)


Seq(filterSettings: _*)
CommonSettingsPlugin.setUpFiltersPlugin

enablePlugins(BuildInfoPlugin)
