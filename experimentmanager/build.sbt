/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

name := "deepsense-experimentmanager"

libraryDependencies ++= Dependencies.experimentmanager

unmanagedClasspath in Runtime += (baseDirectory.value / "conf")

unmanagedClasspath in Compile += (baseDirectory.value / "conf")

Revolver.settings
