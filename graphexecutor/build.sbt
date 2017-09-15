// Copyright (c) 2015, CodiLime Inc.

import sbtassembly.PathList

name := "deepsense-graphexecutor"

libraryDependencies ++= Dependencies.graphexecutor

// Necessary while assembling uber-jar (omitting MANIFEST.MF file from constituent jar files)
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF")               => MergeStrategy.discard
  case PathList("META-INF", "INDEX.LIST")                => MergeStrategy.discard
  case PathList("META-INF", "ECLIPSEF.SF")               => MergeStrategy.discard
  case PathList("META-INF", "ECLIPSEF.RSA")              => MergeStrategy.discard
  case PathList("application.conf")                      => MergeStrategy.discard
  case PathList("conf", "hadoop", "core-site.xml")       => MergeStrategy.discard
  case PathList("conf", "hadoop", "hdfs-site.xml")       => MergeStrategy.discard
  case PathList("conf", "hadoop", "yarn-site.xml")       => MergeStrategy.discard
  case _ => MergeStrategy.first
}
// Skip test while assembling uber-jar
test in assembly := {}

// Make assembly task create jar with only DeepSense.io code inside
assemblyOption in assembly :=
  (assemblyOption in assembly).value.copy(includeScala = false, includeDependency = false)

unmanagedClasspath in Runtime += (baseDirectory.value / "conf")

mainClass in Compile := Some("io.deepsense.graphexecutor.GraphExecutor")

enablePlugins(BuildInfoPlugin, JavaAppPackaging, GitVersioning, UniversalDeployPlugin)

mappings in Universal := {
  val universalMappings = (mappings in Universal).value
  val assemblyJar = (assembly in Compile).value
  val assemblyDepsJar = (assemblyPackageDependency in Compile).value

  val filtered = universalMappings filter {
      case (file, name) => ! name.endsWith(".jar")
  }

  filtered :+
    (assemblyJar -> ("lib/" + assemblyJar.getName)) :+
    (assemblyDepsJar -> ("lib/" + assemblyDepsJar.getName))
}
