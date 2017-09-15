name := "seahorse-schedulingmanager"

libraryDependencies ++= Dependencies.schedulingmanager

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

mainClass in (Compile, run) := Some("ai.deepsense.seahorse.scheduling.server.JettyMain")
