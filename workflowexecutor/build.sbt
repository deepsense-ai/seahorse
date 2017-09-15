// Copyright (c) 2015, CodiLime Inc.

import sbtassembly.PathList

name := "deepsense-seahorse-workflowexecutor"

libraryDependencies ++= Dependencies.workflowexecutor

// Necessary while assembling uber-jar (omitting MANIFEST.MF file from constituent jar files)
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF")               => MergeStrategy.discard
  case _ => MergeStrategy.first
}
