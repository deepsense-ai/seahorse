// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Radoslaw Kotowski

name         := "deepsense-graph"

libraryDependencies ++= Seq(
  "org.scalatest"          %% "scalatest"     % "2.2.4"  % "test",
  "com.github.nscala-time" %% "nscala-time"   % "1.8.0"
)

// Fork to run all test and run tasks in JVM separated from sbt JVM
fork := true
