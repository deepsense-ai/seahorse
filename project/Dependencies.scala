import sbt._

object Version {
  val akka = "2.3.4-spark"
  val apacheCommons = "3.3.2"
  val avro = "1.7.7"
  val cassandraConnector = "1.5.0-M2"
  val cassandraUnit = "2.1.3.1"
  val guice = "3.0"
  val jclouds = "1.9.0"
  val metricsScala = "3.5.1_a2.3"
  val mockito = "1.10.19"
  val nsscalaTime = "1.8.0"
  val scala = "2.11.6"
  val scalatest = "3.0.0-SNAP4"
  val spray = "1.3.3"
  val sprayJson = "1.3.1"
  val seahorse = "1.0.0-SNAPSHOT"
}

object Library {

  val akka = (name: String) => "org.spark-project.akka" %% s"akka-$name" % Version.akka
  val jclouds = (name: String) => "org.apache.jclouds.api" % s"openstack-$name" % Version.jclouds
  val seahorse = (name: String) => "io.deepsense" %% s"deepsense-seahorse-$name" %
    Version.seahorse exclude("com.datastax.cassandra", "cassandra-driver-core") exclude(
    "com.datastax.spark", "spark-cassandra-connector") exclude(
    "org.cassandraunit", "cassandra-unit")
  val spray = (name: String) => "io.spray" %% s"spray-$name" % Version.spray

  val akkaActor = akka("actor")
  val akkaTestkit = akka("testkit")
  val apacheCommons = "org.apache.commons" % "commons-lang3" % Version.apacheCommons
  val cassandraUnit = "org.cassandraunit" % "cassandra-unit" % Version.cassandraUnit
  val cassandraConnector = "com.datastax.spark" %% "spark-cassandra-connector" %
    Version.cassandraConnector
  val guice = "com.google.inject" % "guice" % Version.guice
  val guiceMultibindings = "com.google.inject.extensions" % "guice-multibindings"   % Version.guice
  val jcloudsKeystone = jclouds("keystone")
  val jcloudsCompute = "org.apache.jclouds" % "jclouds-compute" % Version.jclouds
  val jcloudsNova = jclouds("nova")
  val metricsScala = "nl.grons" %% "metrics-scala" % Version.metricsScala
  val mockitoCore = "org.mockito" % "mockito-core" % Version.mockito
  val nscalaTime = "com.github.nscala-time" %% "nscala-time" % Version.nsscalaTime
  val scalaReflect = "org.scala-lang" % "scala-reflect" % Version.scala
  val scalatest = "org.scalatest" %% "scalatest" % Version.scalatest
  val seahorseCommons = seahorse("commons")
  val seahorseDeeplang = seahorse("deeplang")
  val seahorseModels = seahorse("models")
  val seahorseGraph = seahorse("graph")
  val seahorseWorkflowJson = seahorse("workflow-json")
  val seahorseReportlib = seahorse("reportlib")
  val sprayCan = spray("can")
  val sprayRouting = spray("routing")
  val sprayTestkit = spray("testkit")
  val sprayClient = spray("client")
  val sprayJson = "io.spray" %% "spray-json" % Version.sprayJson
}

object Dependencies {

  import Library._

  val resolvers = Seq(
    "typesafe.com"           at "http://repo.typesafe.com/typesafe/repo/",
    "sonatype.org"           at "https://oss.sonatype.org/content/repositories/releases",
    "spray.io"               at "http://repo.spray.io",
    "seahorse.deepsense.io"  at
      "http://10.10.1.77:8081/artifactory/simple/deepsense-seahorse-release",
    "seahorse.snapshot"  at "http://10.10.1.77:8081/artifactory/simple/deepsense-seahorse-snapshot"
  )

  val commons = Seq(
    akkaActor,
    apacheCommons,
    cassandraConnector,
    guice,
    guiceMultibindings,
    jcloudsCompute,
    jcloudsKeystone,
    jcloudsNova,
    metricsScala,
    nscalaTime,
    seahorseCommons,
    sprayCan,
    sprayJson,
    sprayRouting
  ) ++ Seq(akkaTestkit, cassandraUnit, mockitoCore, scalatest, sprayTestkit).map(_ % Test)

  val workflowmanager = Seq(
    akkaActor,
    apacheCommons,
    cassandraConnector,
    guice,
    guiceMultibindings,
    seahorseDeeplang,
    seahorseGraph,
    seahorseReportlib,
    seahorseWorkflowJson,
    sprayCan,
    sprayClient,
    sprayJson,
    sprayRouting
  ) ++ Seq(akkaTestkit, mockitoCore, scalatest, sprayTestkit).map(_ % s"$Test,it")
}
