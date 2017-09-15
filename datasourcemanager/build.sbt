name := "deepsense-seahorse-datasourcemanager"

libraryDependencies ++= Dependencies.datasourcemanager

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

mainClass in (Compile, run) := Some("io.deepsense.seahorse.datasource.server.JettyMain")