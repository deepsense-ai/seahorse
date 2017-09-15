// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Jacek Laskowski

name := "deepsense-backend"

lazy val commons           = project
lazy val deeplang          = project dependsOn commons
lazy val entitystorage     = project dependsOn deeplang
lazy val experimentmanager = project dependsOn (commons, deeplang, graphjson, graphexecutor)
lazy val graph             = project dependsOn deeplang
lazy val graphexecutor     = project dependsOn graph
lazy val graphjson         = project dependsOn (graph, deeplang)
