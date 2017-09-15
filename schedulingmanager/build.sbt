name := "deepsense-schedulingmanager"

libraryDependencies ++= Dependencies.schedulingmanager

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

mainClass in (Compile, run) := Some("io.deepsense.seahorse.scheduling.server.JettyMain")
