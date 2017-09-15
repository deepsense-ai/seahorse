// Copyright (c) 2015, CodiLime, Inc.
//
// Owner: Grzegorz Chilkiewicz

import sbtassembly.PathList

name         := "deepsense-graphexecutor"

val hadoopV = "2.6.0"
val avroV = "1.7.7"
libraryDependencies ++= Seq(
  "org.apache.hadoop" %  "hadoop-common"      % hadoopV,
  "org.apache.hadoop" %  "hadoop-yarn-client" % hadoopV,
  "org.apache.hadoop" %  "hadoop-yarn-api"    % hadoopV,
  "org.apache.hadoop" %  "hadoop-yarn-common" % hadoopV,
  "org.apache.hadoop" %  "hadoop-hdfs"        % hadoopV,
  "org.apache.hadoop" %  "hadoop-client"      % hadoopV,
  "org.apache.spark"  %% "spark-core"         % "1.3.0"  % "provided",
  // Avro is used to provide RPC communication with Graph Executor
  "org.apache.avro"   %  "avro"               % avroV,
  "org.apache.avro"   %  "avro-ipc"           % avroV
)

// Import the sbt-avro plugin settings to activate sbt-avro plugin
sbtavro.SbtAvro.avroSettings
// Set Avro version for Avro compiler
version in avroConfig := avroV

// Necessary while assembling uber-jar (omitting MANIFEST.MF file from constituent jar files)
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", "INDEX.LIST") => MergeStrategy.discard
  case PathList("META-INF", "ECLIPSEF.SF") => MergeStrategy.discard
  case PathList("META-INF", "ECLIPSEF.RSA") => MergeStrategy.discard
  case _ => MergeStrategy.first
}
