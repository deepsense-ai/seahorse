// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Wojciech Jurczyk

organization := "io.deepsense"
name         := "deepsense-graph-json"
version      := "0.1.0"
scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.github.nscala-time" %% "nscala-time"   % "1.8.0",
  "io.spray"               %% "spray-json"    % "1.3.1",
  "org.scalatest"          %% "scalatest"     % "2.2.4"   % "test",
  "org.mockito"            %  "mockito-core"  % "1.10.19" % "test"
)

// Fork to run all test and run tasks in JVM separated from sbt JVM
fork := true
