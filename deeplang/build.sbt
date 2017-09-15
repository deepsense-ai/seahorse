// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Jacek Laskowski

name         := "deepsense-deeplang"

val sparkVersion = "1.3.0"
libraryDependencies ++= Seq(
  "org.scalatest"          %% "scalatest"     % "2.2.4" % "test",
  "com.github.nscala-time" %% "nscala-time"   % "1.8.0",
  "org.scala-lang"         %  "scala-reflect" % scalaVersion.value,
  "org.apache.spark"       %% "spark-sql"     % sparkVersion,
  "org.apache.spark"       %% "spark-core"    % sparkVersion
)

// Fork to run all test and run tasks in JVM separated from sbt JVM
fork := true
