// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Jacek Laskowski

name := "deepsense-backend"

lazy val deeplang          = project
lazy val experimentmanager = project dependsOn (deeplang, graphjson, graphexecutor)
lazy val graph             = project dependsOn deeplang
lazy val graphexecutor     = project dependsOn graph
lazy val graphjson         = project dependsOn (graph, deeplang)
