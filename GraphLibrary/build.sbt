// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Radoslaw Kotowski

name := "GraphLibrary"

version := "1.0"

scalaVersion := "2.10.4"

ideaExcludeFolders += ".idea"

ideaExcludeFolders += ".idea_modules"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.github.nscala-time" %% "nscala-time" % "1.8.0"
)
