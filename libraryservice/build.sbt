import com.typesafe.sbt.packager.docker.Cmd

name := "deepsense-libraryservice"

libraryDependencies ++= Dependencies.libraryservice

unmanagedClasspath in Runtime += (baseDirectory.value / "conf")

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)
