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

import sbt._

object Version {
  val akka = "2.4.9"
  val amazonS3 = "1.10.16"
  val apacheCommons = "3.3.+"
  val guava = "16.0"
  val hadoop = "2.7.0"
  val mockito = "1.10.19"
  val nsscalaTime = "1.8.0"
  val scala = "2.11.8"
  val scalacheck = "1.12.2"
  val scalatest = "3.0.0"
  val scoverage = "1.0.4"
  val spark = "2.0.0"
  val spray = "1.3.3"
  val sprayJson = "1.3.2"
  val wireMock = "1.57"
}

object Library {

  implicit class RichModuleID(m: ModuleID) {
    def excludeAkkaActor: ModuleID = m excludeAll ExclusionRule("com.typesafe.akka")
    def excludeScalatest: ModuleID = m excludeAll ExclusionRule("org.scalatest")
  }

  val akka = (name: String) => "com.typesafe.akka" %% s"akka-$name" % Version.akka
  val hadoop = (name: String) => "org.apache.hadoop" % s"hadoop-$name" % Version.hadoop
  val spark = (name: String) => "org.apache.spark" %% s"spark-$name" % Version.spark excludeScalatest
  val spray = (name: String) => "io.spray" %% s"spray-$name" % Version.spray excludeAkkaActor

  val akkaActor = akka("actor")
  val akkaTestkit = akka("testkit")
  val amazonS3 = "com.amazonaws" % "aws-java-sdk-s3" %
    Version.amazonS3 exclude("com.fasterxml.jackson.core", "jackson-databind")
  val apacheCommonsLang3 = "org.apache.commons" % "commons-lang3" % Version.apacheCommons
  val apacheCommonsCsv = "org.apache.commons" % "commons-csv" % "1.1" // Also used by spark-csv
  val guava = "com.google.guava" % "guava" % Version.guava
  val hadoopAWS = hadoop("aws")
  val hadoopClient = hadoop("client")
  val hadoopCommon = hadoop("common")
  val log4JExtras = "log4j" % "apache-log4j-extras" % "1.2.17"
  val nscalaTime = "com.github.nscala-time" %% "nscala-time" % Version.nsscalaTime
  val mockitoCore = "org.mockito" % "mockito-core" % Version.mockito
  val rabbitmq = "com.thenewmotion.akka" %% "akka-rabbitmq" % "2.2" excludeAkkaActor
  val reflections = "org.reflections" % "reflections" % "0.9.10"
  val scalacheck = "org.scalacheck" %% "scalacheck" % Version.scalacheck
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.12"
  val slf4jLog4j = "org.slf4j" % "slf4j-log4j12" % "1.7.12"
  val sprayCan = spray("can")
  val sprayClient = spray("client")
  val sprayHttpx = spray("httpx")
  val sprayJson = "io.spray" %% "spray-json" % Version.sprayJson
  val scalaReflect = "org.scala-lang" % "scala-reflect" % Version.scala
  val scalatest = "org.scalatest" %% "scalatest" % Version.scalatest
  val scopt = "com.github.scopt" %% "scopt" % "3.3.0"
  val scoverage = "org.scoverage" %% "scalac-scoverage-runtime" % "1.0.4"
  val sparkCore = spark("core")
  val sparkMLLib = spark("mllib")
  val sparkSql = spark("sql")
  val wireMock = "com.github.tomakehurst" % "wiremock" % Version.wireMock exclude(
    "com.fasterxml.jackson.core", "jackson-databind") exclude (
    "com.google.guava", "guava")
  val jsonLenses = "net.virtual-void" %%  "json-lenses" % "0.6.1"
}

object Dependencies {

  import Library._

  val resolvers = Seq(
    "typesafe.com" at "http://repo.typesafe.com/typesafe/repo/",
    "sonatype.org" at "https://oss.sonatype.org/content/repositories/releases",
    "spray.io"     at "http://repo.spray.io",
    "The New Motion Public Repo" at "http://nexus.thenewmotion.com/content/groups/public/",
    "central.maven.org" at "http://central.maven.org/maven2/"
  )

  object Spark {
    val components = Seq(
      sparkMLLib,
      sparkSql,
      sparkCore
    )
    val provided = components.map(_ % Provided)
    val test = components.map(_ % s"$Test,it")
    val onlyInTests = provided ++ test
  }

  object Hadoop {
    private val hadoopComponents = Seq(
      hadoopAWS,
      hadoopClient,
      hadoopCommon
    )
    val provided = hadoopComponents.map(_ % Provided)
    val test = hadoopComponents.map(_ % s"$Test,it")
    val onlyInTests = provided ++ test
  }

  val commons = Spark.onlyInTests ++ Seq(
    apacheCommonsLang3,
    log4JExtras,
    nscalaTime,
    slf4j,
    slf4jLog4j,
    sprayCan,
    sprayHttpx,
    sprayJson
  ) ++ Seq(mockitoCore, scalatest, scoverage).map(_ % Test)

  val deeplang = Spark.onlyInTests ++ Hadoop.onlyInTests ++ Seq(
    apacheCommonsLang3,
    amazonS3,
    nscalaTime,
    scalaReflect,
    apacheCommonsCsv,
    reflections
  ) ++ Seq(mockitoCore, scalacheck, scalatest, scoverage).map(_ % Test)

  val docgen = Spark.components

  val graph = Seq(nscalaTime) ++ Seq(scalatest, mockitoCore).map(_ % Test)

  val workflowJson = Spark.onlyInTests ++ Seq(
    nscalaTime,
    sprayJson
  ) ++ Seq(mockitoCore, scalatest).map(_ % Test)

  val models = Spark.onlyInTests ++ Seq(scalatest, mockitoCore).map(_ % Test)

  val reportlib = Spark.onlyInTests ++ Seq(
    sprayJson
  ) ++ Seq(mockitoCore, scalatest).map(_ % Test)

  val workflowexecutor = Spark.onlyInTests ++ Seq(
    akkaActor,
    guava,
    jsonLenses,
    scopt,
    sprayClient,
    rabbitmq
  ) ++ Seq(akkaTestkit, mockitoCore, scalatest, wireMock).map(_ % s"$Test,it")

  val workflowexecutorMqProtocol = Seq(
    akkaActor,
    rabbitmq,
    sprayJson,
    sprayHttpx
  )
}
