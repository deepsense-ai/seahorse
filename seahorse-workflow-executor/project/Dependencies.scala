/**
 * Copyright 2015, deepsense.ai
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

  val spark = sys.props.getOrElse("SPARK_VERSION", "2.1.1")
  val (scala, java, hadoop, akka, apacheCommons) = spark match {
    case "2.1.0" | "2.1.1" => ("2.11.8", "1.8", "2.7.3", "2.4.9", "3.5")
    case "2.0.0" | "2.0.1" | "2.0.2" => ("2.11.8", "1.8", "2.7.1", "2.4.9", "3.3.+")
    case "1.6.1" => ("2.10.5", "1.7", "2.6.0", "2.3.11", "3.3.+")
  }

  val amazonS3 = "1.10.16"
  val googleApi = "1.22.0"
  val mockito = "1.10.19"
  val nsscalaTime = "1.8.0"
  val scalacheck = "1.12.2"
  val scalatest = "3.0.0"
  val scoverage = "1.0.4"
  val spray = "1.3.3"
  val sprayJson = "1.3.2"
  val wireMock = "1.57"
}

object Library {

  implicit class RichModuleID(m: ModuleID) {
    def excludeAkkaActor = m excludeAll ExclusionRule("com.typesafe.akka")
    def excludeScalatest = m excludeAll ExclusionRule("org.scalatest")
    def excludeJackson = m excludeAll ExclusionRule("com.fasterxml.jackson.core")
    def excludeGuava = m excludeAll ExclusionRule("com.google.guava", "guava")
  }

  val akka = (name: String) => "com.typesafe.akka" %% s"akka-$name" % Version.akka
  val hadoop = (name: String) => "org.apache.hadoop" % s"hadoop-$name" % Version.hadoop
  val spark = (name: String) => (version: String) => "org.apache.spark" %% s"spark-$name" % version excludeScalatest
  val spray = (name: String) => "io.spray" %% s"spray-$name" % Version.spray excludeAkkaActor

  val akkaActor = akka("actor")
  val akkaTestkit = akka("testkit")
  val amazonS3 = "com.amazonaws" % "aws-java-sdk-s3" % Version.amazonS3 excludeJackson
  val apacheCommonsLang3 = "org.apache.commons" % "commons-lang3" % Version.apacheCommons
  val apacheCommonsCsv = "org.apache.commons" % "commons-csv" % "1.1" // Also used by spark-csv
  val config = "com.typesafe" % "config" % "1.3.1"
  val hadoopAWS = hadoop("aws")
  val hadoopClient = hadoop("client")
  val hadoopCommon = hadoop("common")
  val log4JExtras = "log4j" % "apache-log4j-extras" % "1.2.17"
  val nscalaTime = "com.github.nscala-time" %% "nscala-time" % Version.nsscalaTime
  val mockitoCore = "org.mockito" % "mockito-core" % Version.mockito
  val rabbitmq = "com.thenewmotion.akka" %% "akka-rabbitmq" % "2.2" excludeAkkaActor
  val reflections = "org.reflections" % "reflections" % "0.9.10" excludeGuava
  val scalacheck = "org.scalacheck" %% "scalacheck" % Version.scalacheck
  val scalate = "org.scalatra.scalate" %% "scalate-core" % "1.7.1"
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
  val sparkHive = spark("hive")
  val wireMock = "com.github.tomakehurst" % "wiremock" % Version.wireMock exclude (
    "com.google.guava", "guava") excludeJackson
  val jsonLenses = "net.virtual-void" %%  "json-lenses" % "0.6.1"
  val javaMail = "javax.mail" % "mail" % "1.4.7"
  // Dependencies for swagger-client generated code
  val retrofit = Seq(
    "com.squareup.retrofit2" % "retrofit" % "2.0.2",
    "com.squareup.retrofit2" % "converter-scalars" % "2.0.2",
    "com.squareup.retrofit2" % "converter-gson" % "2.0.2"
  )
  val oauth2Client = "org.apache.oltu.oauth2" % "org.apache.oltu.oauth2.client" % "1.0.1"
  val swaggerAnnotations = "io.swagger" % "swagger-annotations" % "1.5.8"
  val jodaTime = "joda-time" % "joda-time" % "2.9.3"
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

  val sparkCSV: Seq[ModuleID] = Version.spark match {
    case "1.6.1" => Seq("com.databricks" %% "spark-csv" % "1.4.0")
    case "2.0.0" | "2.0.1" | "2.0.2" | "2.1.0" | "2.1.1" => Seq()
  }

  class Spark(version: String) {
    private val unversionedComponents = Seq(
      sparkMLLib,
      sparkSql,
      sparkCore,
      sparkHive
    )
    val components = unversionedComponents.map(_(version)) ++ sparkCSV
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

  object GoogleServicesApi {
    val components = Seq(
      "com.google.api-client" % "google-api-client" % Version.googleApi excludeJackson,
      "com.google.api-client" % "google-api-client-gson" % "1.22.0" excludeJackson,
      "com.google.apis" % "google-api-services-drive" % s"v3-rev51-${Version.googleApi}" excludeJackson
    )
  }

  def sparkutils(sparkVersion: String) = new Spark(sparkVersion).onlyInTests ++ Seq(akkaActor)

  val usedSpark = new Spark(Version.spark)

  val api = retrofit ++ Seq(
    oauth2Client,
    swaggerAnnotations,
    jodaTime
  )

  val commons = usedSpark.onlyInTests ++ Seq(
    akkaActor,
    sprayClient,
    apacheCommonsLang3,
    config,
    javaMail,
    log4JExtras,
    nscalaTime,
    scalate,
    slf4j,
    slf4jLog4j,
    sprayCan,
    sprayHttpx,
    sprayJson
  ) ++ Seq(mockitoCore, scalatest, scoverage).map(_ % Test)

  val deeplang = usedSpark.onlyInTests ++ Hadoop.onlyInTests ++ GoogleServicesApi.components ++ Seq(
    akkaActor,
    sprayClient,
    apacheCommonsLang3,
    amazonS3,
    nscalaTime,
    scalaReflect,
    apacheCommonsCsv,
    reflections) ++
    sparkCSV ++
    Seq(mockitoCore, scalacheck, scalatest, scoverage).map(_ % Test)

  val docgen = usedSpark.components

  val graph = Seq(nscalaTime) ++ Seq(scalatest, mockitoCore).map(_ % Test)

  val workflowJson = usedSpark.onlyInTests ++ Seq(
    nscalaTime,
    sprayJson
  ) ++ Seq(mockitoCore, scalatest).map(_ % Test)

  val models = usedSpark.onlyInTests ++ Seq(scalatest, mockitoCore).map(_ % Test)

  val reportlib = usedSpark.onlyInTests ++ Seq(
    sprayJson
  ) ++ Seq(mockitoCore, scalatest).map(_ % Test)

  val workflowexecutor = usedSpark.onlyInTests ++ Seq(
    akkaActor,
    jsonLenses,
    scopt,
    sprayClient,
    rabbitmq
  ) ++ Seq(akkaTestkit, mockitoCore, scalatest, wireMock).map(_ % s"$Test,it")

  val workflowexecutorMqProtocol = usedSpark.onlyInTests ++ Seq(
    akkaActor,
    rabbitmq,
    sprayJson,
    sprayHttpx
  )

  val sdk = Seq()

}
