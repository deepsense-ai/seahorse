/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Jacek Laskowski
 */

name := "deepsense-deeplang"

libraryDependencies ++= Dependencies.deeplang

Seq(filterSettings: _*)
CommonSettingsPlugin.setUpFiltersPlugin
