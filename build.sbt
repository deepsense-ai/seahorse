/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// scalastyle:off

lazy val settingsForPublished = CommonSettingsPlugin.assemblySettings ++
  LicenceReportSettings.settings ++ PublishSettings.enablePublishing
lazy val settingsForNotPublished = CommonSettingsPlugin.assemblySettings ++
  LicenceReportSettings.settings ++ PublishSettings.disablePublishing

lazy val sparkVersion = CommonSettingsPlugin.Versions.spark
lazy val sparkUtils = project in file (s"sparkutils$sparkVersion") settings settingsForPublished

lazy val rootProject = project.in(file("."))
  .settings(name := "seahorse")
  .settings(PublishSettings.disablePublishing)
  .aggregate(api, sparkUtils, commons, deeplang, docgen, graph, workflowjson, models,reportlib, workflowexecutormqprotocol,
    workflowexecutor)

lazy val api                    = project

lazy val commons                = project dependsOn (api, sparkUtils) settings settingsForPublished

lazy val deeplang               = project dependsOn (
  commons,
  commons % "test->test",
  graph,
  graph % "test->test",
  reportlib,
  reportlib % "test->test") settings settingsForPublished
lazy val docgen                 = project dependsOn (
  deeplang) settings settingsForNotPublished
lazy val graph                  = project dependsOn (
  commons,
  commons % "test->test") settings settingsForPublished
lazy val workflowjson           = project dependsOn (commons, deeplang, graph, models) settings settingsForNotPublished
lazy val models                 = project dependsOn (commons, deeplang, graph) settings settingsForNotPublished
lazy val reportlib              = project dependsOn commons settings settingsForPublished
lazy val workflowexecutormqprotocol = project dependsOn (
  commons,
  commons % "test->test",
  models,
  reportlib % "test->test",
  workflowjson) settings settingsForNotPublished

lazy val sdk                    = project dependsOn (
  deeplang
) settings settingsForPublished

lazy val workflowexecutor       = project dependsOn (
  commons % "test->test",
  deeplang,
  deeplang % "test->test",
  deeplang % "test->it",
  models,
  workflowjson,
  workflowjson % "test -> test",
  sdk,
  workflowexecutormqprotocol,
  workflowexecutormqprotocol % "test -> test") settings settingsForNotPublished

// Sequentially perform integration tests
addCommandAlias("ds-it",
    ";commons/it:test " +
    ";deeplang/it:test " +
    ";graph/it:test " +
    ";workflowjson/it:test " +
    ";models/it:test " +
    ";reportlib/it:test " +
    ";workflowexecutor/it:test" +
    ";workflowexecutormqprotocol/it:test")

addCommandAlias("generateExamples", "deeplang/it:testOnly io.deepsense.deeplang.doperations.examples.*")

// scalastyle:on
