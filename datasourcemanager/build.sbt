name := "seahorse-datasourcemanager"

libraryDependencies ++= Dependencies.datasourcemanager

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

mainClass in (Compile, run) := Some("ai.deepsense.seahorse.datasource.server.JettyMain")

unmanagedSourceDirectories in Test += (sourceDirectory in Test).value / ("scala_" + scalaBinaryVersion.value)
