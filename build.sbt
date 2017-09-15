// Copyright (c) 2015, CodiLime Inc.

// scalastyle:off

name := "deepsense-backend"

lazy val commons                = project settings LicenceReportSettings.settings
lazy val workflowmanager        = project dependsOn (commons, commons % "test->test") settings LicenceReportSettings.settings
lazy val sessionmanager         = project dependsOn (commons, commons % "test->test") settings LicenceReportSettings.settings

// Sequentially perform integration tests after assembling and deploying GE with dependencies jar
addCommandAlias("ds-it",
    ";commons/it:test " +
    ";sessionmanager/it:test " +
    ";workflowmanager/it:test ")

addCommandAlias("sPublish", "aetherDeploy")
addCommandAlias("sPublishLocal", "aetherInstall")

// scalastyle:on
