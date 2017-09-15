name := "deepsense-datasourcemanager"

libraryDependencies ++= Dependencies.datasourcemanager

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

mainClass in (Compile, run) := Some("io.deepsense.seahorse.datasource.server.JettyMain")

unmanagedResourceDirectories in Runtime += CommonSettingsPlugin.globalResources
unmanagedResourceDirectories in Compile += CommonSettingsPlugin.globalResources