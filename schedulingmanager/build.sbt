name := "deepsense-schedulingmanager"

libraryDependencies ++= Dependencies.schedulingmanager

unmanagedResourceDirectories in Runtime += CommonSettingsPlugin.globalResources
unmanagedResourceDirectories in Compile += CommonSettingsPlugin.globalResources