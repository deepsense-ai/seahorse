// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Jacek Laskowski

name := "deepsense-deeplang"

libraryDependencies ++= Dependencies.deeplang

// Fork to run all test and run tasks in JVM separated from sbt JVM
fork := true
