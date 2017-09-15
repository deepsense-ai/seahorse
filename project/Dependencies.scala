/**
 * Copyright 2015, CodiLime Inc.
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

import sbt._

object Version {
  val akka          = "2.3.4-spark"
  val spark         = "1.4.0"
  val apacheCommons = "3.3.+"
  val sprayJson     = "1.3.1"
  val mockito       = "1.10.19"
  val nsscalaTime   = "1.8.0"
  val scala         = "2.11.6"
  val scalacheck    = "1.12.2"
  val scalatest     = "3.0.0-SNAP4"
  val spray         = "1.3.3"
}

object Library {
  val akka      = (name: String) => "org.spark-project.akka"  %% s"akka-$name"          % Version.akka
  val spark   = (name: String) => "org.apache.spark"          %% s"spark-$name"         % Version.spark
  val spray   = (name: String) => "io.spray"                  %% s"spray-$name"         % Version.spray

  val akkaActor          = akka("actor")
  val akkaTestkit        = akka("testkit")
  val apacheCommons      = "org.apache.commons"                %  "commons-lang3"       % Version.apacheCommons
  val log4JExtras        = "log4j"                             %  "apache-log4j-extras" % "1.2.17"
  val nscalaTime         = "com.github.nscala-time"           %%  "nscala-time"         % Version.nsscalaTime
  val mockitoCore        = "org.mockito"                       %  "mockito-core"        % Version.mockito
  val scalacheck         = "org.scalacheck"                   %%  "scalacheck"          % Version.scalacheck
  val slf4jLog4j         = "org.slf4j"                         %  "slf4j-log4j12"       % "1.7.12"
  val sprayCan           = spray("can")
  val sprayHttpx         = spray("httpx")
  val sprayJson          = "io.spray"                         %% "spray-json"           % Version.sprayJson
  val scalaLogging       = "com.typesafe.scala-logging"       %% "scala-logging"        % "3.1.0"
  val scalaReflect       = "org.scala-lang"                    % "scala-reflect"        % Version.scala
  val scalatest          = "org.scalatest"                    %% "scalatest"            % Version.scalatest
  val scopt              = "com.github.scopt"                 %% "scopt"                % "3.3.0"
  val sparkCore          = spark("core")
  val sparkMLLib         = spark("mllib")
  val sparkSql           = spark("sql")
}

object Dependencies {

  import Library._

  val resolvers = Seq(
    "typesafe.com" at "http://repo.typesafe.com/typesafe/repo/",
    "sonatype.org" at "https://oss.sonatype.org/content/repositories/releases",
    "spray.io"     at "http://repo.spray.io"
  )

  val commons = Seq(
    apacheCommons,
    log4JExtras,
    nscalaTime,
    scalaLogging,
    slf4jLog4j,
    sparkSql,
    sprayCan,
    sprayHttpx,
    sprayJson
  ) ++ Seq(mockitoCore, scalatest).map(_ % Test)

  val deeplang = Seq(
    nscalaTime,
//    sprayClient,
    scalaReflect,
    sparkSql,
    sparkMLLib,
    sparkCore
  ) ++ Seq(scalatest, mockitoCore, scalacheck).map(_ % Test)

  val entitystorageClient = Seq(
    akkaActor
  ) ++ Seq(scalatest, mockitoCore, akkaTestkit).map(_ % Test)

  val graph = Seq(nscalaTime) ++ Seq(scalatest, mockitoCore).map(_ % Test)

  val graphJson = Seq(
    nscalaTime,
    sprayJson
  ) ++ Seq(scalatest, mockitoCore).map(_ % Test)

  val models = Seq(scalatest, mockitoCore).map(_ % Test)

  val reportlib = Seq(
    sprayJson
  ) ++ Seq(scalatest, mockitoCore).map(_ % Test)

  val workflowexecutor = Seq(
    scopt
  ) ++ Seq(sparkCore, sparkSql).map(_ % Provided)
}
