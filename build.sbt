// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Jacek Laskowski

name := "deepsense-backend"

lazy val commons           = project
lazy val deeplang          = project
lazy val entitystorage     = project dependsOn (commons, deeplang)
lazy val experimentmanager = project dependsOn (commons, deeplang, graphjson, graphexecutor)
lazy val graph             = project dependsOn (commons, deeplang)
lazy val graphexecutor     = project dependsOn graph
lazy val graphjson         = project dependsOn (commons, graph, deeplang)
