/**
 * Copyright 2017, deepsense.ai
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
  val apacheCommons = "3.3.2"
  val avro = "1.7.7"
  val guice = "3.0"
  val h2 = "1.4.191"
  val json4s = "3.3.0"
  val jclouds = "1.9.0"
  val metricsScala = "3.5.4_a2.3"
  val mockito = "1.10.19"
  val nsscalaTime = "1.8.0"
  val scalatest = "3.0.0"
  val scalatra = "2.4.0"
  val scoverage = "1.0.4"
  val slick = "3.1.1"

  val spark = sys.props.getOrElse("SPARK_VERSION", "2.1.1")
  val (scala, hadoop, akka, sprayRoutingLib) = spark match {
    case "2.1.0" | "2.1.1" => ("2.11.8", "2.7", "2.4.9", "routing-shapeless2")
    case "2.0.0" | "2.0.1" | "2.0.2" => ("2.11.8", "2.7", "2.4.9", "routing")
    case "1.6.1" => ("2.10.5", "2.6", "2.3.11", "routing")
  }

  val spray = "1.3.3"
  val sprayJson = "1.3.1"
  val wiremock = "1.57"
  val flyway = "4.0"
  val jetty = "9.3.8.v20160314"
}

object Library {

  implicit class RichModuleID(m: ModuleID) {
    def excludeAkkaActor: ModuleID = m excludeAll ExclusionRule("com.typesafe.akka")
    def excludeScalatest: ModuleID = m excludeAll ExclusionRule("org.scalatest")
  }

  val akka = (name: String) => "com.typesafe.akka" %% s"akka-$name" % Version.akka
  val jclouds = (name: String) => "org.apache.jclouds.api" % s"openstack-$name" % Version.jclouds

  val spark = (name: String) => "org.apache.spark" %% s"spark-$name" % Version.spark excludeScalatest

  val spray = (name: String) => "io.spray" %% s"spray-$name" % Version.spray excludeAkkaActor

  val akkaActor = akka("actor")
  val akkaAgent = akka("agent")
  val akkaTestkit = akka("testkit")
  val apacheCommons = "org.apache.commons" % "commons-lang3" % Version.apacheCommons
  val apacheCommonsExec = "org.apache.commons" % "commons-exec" % "1.3"
  val cronUtils = "com.cronutils" % "cron-utils" % "5.0.4"
  val guice = "com.google.inject" % "guice" % Version.guice
  val guiceMultibindings = "com.google.inject.extensions" % "guice-multibindings" % Version.guice
  val jcloudsKeystone = jclouds("keystone")
  val jcloudsCompute = "org.apache.jclouds" % "jclouds-compute" % Version.jclouds
  val jcloudsNova = jclouds("nova")
  val jettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % "9.3.8.v20160314"
  val metricsScala = "nl.grons" %% "metrics-scala" % Version.metricsScala excludeAkkaActor
  val mockitoCore = "org.mockito" % "mockito-core" % Version.mockito
  val nscalaTime = "com.github.nscala-time" %% "nscala-time" % Version.nsscalaTime
  val quartz = "org.quartz-scheduler" % "quartz" % "2.2.3"
  val rabbitmq = "com.thenewmotion.akka" %% "akka-rabbitmq" % "2.2" excludeAkkaActor
  val scalaReflect = "org.scala-lang" % "scala-reflect" % Version.scala
  val scalatest = "org.scalatest" %% "scalatest" % Version.scalatest
  val scalatra = "org.scalatra" %% "scalatra" % Version.scalatra
  val scalatraTest = "org.scalatra" %% "scalatra-scalatest" % Version.scalatra % "test"
  val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.8"
  val scoverage = "org.scoverage" %% "scalac-scoverage-runtime" % Version.scoverage
  val stampy = "asia.stampy" % "stampy-core" % "1.0-RELEASE"
  val slick = "com.typesafe.slick" %% "slick" % Version.slick
  val sparkCore = spark("core") excludeAll ExclusionRule("com.chuusai", "shapeless_2.11", "2.0.0")
  val sparkMLLib = spark("mllib") excludeAll ExclusionRule("com.chuusai", "shapeless_2.11", "2.0.0")
  val sparkLauncher = spark("launcher") excludeAll ExclusionRule("com.chuusai", "shapeless_2.11", "2.0.0")
  val sprayCan = spray("can")
  val sprayRouting = spray(Version.sprayRoutingLib)
  val sprayTestkit = spray("testkit")
  val sprayClient = spray("client")
  val sprayJson = "io.spray" %% "spray-json" % Version.sprayJson
  val h2 = "com.h2database" % "h2" % Version.h2
  val wiremock = "com.github.tomakehurst" % "wiremock" % Version.wiremock
  val flyway = "org.flywaydb" % "flyway-core" % Version.flyway
  // to suppress "Nullable" warning, as per
  // http://stackoverflow.com/questions/13162671/missing-dependency-class-javax-annotation-nullable.
  val findBugs = "com.google.code.findbugs" % "jsr305" % "3.0.1"

}

object Dependencies {

  import Library._

  val resolvers = Seq(
    "sonatype.org"           at "https://oss.sonatype.org/content/repositories/releases",
    "spray.io"               at "http://repo.spray.io",
    "The New Motion Public Repo" at "http://nexus.thenewmotion.com/content/groups/public/",
    Classpaths.typesafeReleases
  )

  object Spark {
    val components = Seq(
      sparkCore,
      sparkMLLib)
    // val provided = components.map(_ % Provided)
    val provided = components
    val test = components.map(_ % s"$Test,it")
    val onlyInTests = provided ++ test
  }

  val scalajs = "org.scalaj" %% "scalaj-http" % "2.3.0"

  val json4s = Seq(
    "org.json4s"              %% "json4s-jackson"                 % Version.json4s,
    "org.json4s"              %% "json4s-native"                  % Version.json4s,
    "org.json4s"              %% "json4s-ext"                     % Version.json4s
  )

  val scalatraAndJetty = Seq(
    "org.scalatra"            %% "scalatra"                       % Version.scalatra,
    "org.scalatra"            %% "scalatra-scalate"               % Version.scalatra,
    "org.scalatra"            %% "scalatra-json"                  % Version.scalatra,
    "org.scalatra"            %% "scalatra-slf4j"                 % Version.scalatra,
    "org.scalatra"            %% "scalatra-atmosphere"            % Version.scalatra,
    "org.scalatra"            %% "scalatra-scalatest"             % Version.scalatra  % "test",

    "org.eclipse.jetty"           % "jetty-server"            % Version.jetty     % "compile;test",
    "org.eclipse.jetty"           % "jetty-webapp"            % Version.jetty     % "compile;test",
    "org.eclipse.jetty.websocket" % "websocket-server"        % Version.jetty
  )

  val shapeless = "com.chuusai" %% "shapeless" % "2.3.2"
  val scalacheck = "org.scalacheck" %% "scalacheck" % "1.13.4" % Test
  val scalacheckShapeless = "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.3"

  val commons = Seq(
    akkaActor,
    apacheCommons,
    findBugs,
    guice,
    guiceMultibindings,
    jcloudsCompute,
    jcloudsKeystone,
    jcloudsNova,
    metricsScala,
    nscalaTime,
    slick,
    sprayCan,
    sprayClient,
    sprayJson,
    sprayRouting
  ) ++ scalatraAndJetty ++ Seq(akkaTestkit, mockitoCore, scalatest, sprayTestkit).map(_ % Test)

  val workflowmanager = Spark.components ++ Seq(
    akkaActor,
    apacheCommons,
    guice,
    guiceMultibindings,
    h2,
    slick,
    flyway,
    sprayCan,
    sprayClient,
    sprayJson,
    sprayRouting,
    scalaz
  ) ++ Seq(akkaTestkit, mockitoCore, scalatest, scoverage, sprayTestkit).map(_ % s"$Test,it")

  val sessionmanager = Spark.components ++ Seq(
    akkaActor,
    akkaAgent,
    apacheCommonsExec,
    h2,
    slick,
    flyway,
    scalaz,
    sprayCan,
    sprayClient,
    sprayJson,
    sprayRouting,
    sparkLauncher
  ) ++ Seq(akkaTestkit, mockitoCore, scalatest, scoverage, sprayTestkit, wiremock)
    .map(_ % s"$Test,it")

  val libraryservice = Seq(
    scalatra,
    jettyWebapp,
    scalatraTest
  ) ++ Seq(scoverage).map(_ % s"$Test,it")

  val datasourcemanager = scalatraAndJetty ++ json4s ++ Seq(
    h2,
    flyway,
    scalajs,
    scalaz,
    shapeless,
    scalacheckShapeless,
    scalacheck
  )

  val schedulingmanager = scalatraAndJetty ++ json4s ++ Seq(
    cronUtils,
    h2,
    flyway,
    scalajs,
    slick,
    quartz
  ) ++ Seq(scalatest).map(_ % Test)

  val integrationtests = Seq(
    scalaz
  ) ++ Seq(scalatest).map(_ % s"$Test,it")
}
