/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

lazy val sparkVersion = Version.spark

// helperSparkUtils is introduced because sparkUtils needs to depend on other project, which also
// depends on sparkVersion. It is top-level, since sbt only registers top-level values;
// if we did it inside sparkUtils initialization, it would result in error.
lazy val ignoredProject = project in file("empty")

lazy val helperSparkUtils = sparkVersion match {
  case "1.6.1" =>
    ignoredProject
  case "2.0.0" | "2.0.1" | "2.0.2" =>
    val sparkUtils2_0_x = project in file("sparkutils2.0.x") settings settingsForPublished
    sparkUtils2_0_x
  case "2.1.0" | "2.1.1" =>
    val sparkUtils2_1_0 = project in file("sparkutils2.1.x") settings settingsForPublished
    sparkUtils2_1_0
}

lazy val sparkUtils = sparkVersion match {
  case "1.6.1" =>
    val sparkUtils = project in file (s"sparkutils1.6.1") settings settingsForPublished
    sparkUtils
  case "2.0.0" | "2.0.1" | "2.0.2" | "2.1.0" | "2.1.1" =>
    val sparkUtils = project in file(s"sparkutils2.x") dependsOn helperSparkUtils settings settingsForPublished
    sparkUtils
}
lazy val rootProject = project.in(file("."))
  .settings(name := "seahorse")
  .settings(PublishSettings.disablePublishing)
  .aggregate(api, helperSparkUtils, sparkUtils, commons, deeplang, docgen, graph, workflowjson, reportlib, workflowexecutormqprotocol,
    workflowexecutor)

lazy val api                    = project settings settingsForPublished

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
lazy val workflowjson           = project dependsOn (commons, deeplang, graph) settings settingsForNotPublished
lazy val reportlib              = project dependsOn commons settings settingsForPublished
lazy val workflowexecutormqprotocol = project dependsOn (
  commons,
  commons % "test->test",
  deeplang,
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
    ";reportlib/it:test " +
    ";workflowexecutor/it:test" +
    ";workflowexecutormqprotocol/it:test")

addCommandAlias("generateExamples", "deeplang/it:testOnly ai.deepsense.deeplang.doperations.examples.*")

// scalastyle:on
