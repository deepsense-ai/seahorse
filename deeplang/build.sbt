/**
 * Copyright (c) 2015, CodiLime Inc.
 */

name := "deepsense-deeplang"

libraryDependencies ++= Dependencies.deeplang

Seq(filterSettings: _*)
CommonSettingsPlugin.setUpFiltersPlugin
