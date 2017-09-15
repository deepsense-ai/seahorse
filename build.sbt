// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Jacek Laskowski

organization := "io.deepsense"
name         := "backend"
version      := "0.1.0"
scalaVersion := "2.11.6"

lazy val deeplang          = project in file("deepsense-deeplang")
lazy val experimentmanager = project in file("deepsense-experimentmanager")
lazy val graph             = project in file("deepsense-graph") dependsOn deeplang