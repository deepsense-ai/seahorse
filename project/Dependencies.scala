import sbt._

object Version {
  val akka          = "2.3.4-spark"
  val apacheCommons = "3.3.+"
  val avro          = "1.7.7"
  val cassandra     = "2.1.5"
  val cassandraUnit = "2.1.3.1"
  val guava         = "17.0"
  val guice         = "3.0"
  val hadoop        = "2.6.0"
  val jclouds       = "1.9.0"
  val jsr305        = "3.0.0"
  val logback       = "1.1.2"
  val metricsScala  = "3.5.1_a2.3"
  val mockito       = "1.10.19"
  val nsscalaTime   = "1.8.0"
  val scala         = "2.11.6"
  val scalacheck    = "1.12.2"
  val scalatest     = "3.0.0-SNAP4"
  val spark         = "1.4.0"
  val spray         = "1.3.3"
  val sprayJson     = "1.3.1"
  val seahorse      = "0.1.4"
}

object Library {

  val akka      = (name: String) => "org.spark-project.akka"  %% s"akka-$name"              % Version.akka
  val avro      = (name: String) => "org.apache.avro"         % s"avro$name"                % Version.avro
  val hadoop    = (name: String) => "org.apache.hadoop"       % s"hadoop-$name"             % Version.hadoop exclude("org.slf4j", "slf4j-log4j12")
  val jclouds   = (name: String) => "org.apache.jclouds.api"  % s"openstack-$name"          % Version.jclouds
  val logback   = (name: String) => "ch.qos.logback"          % s"logback-$name"            % Version.logback
  val seahorse  = (name: String) => "io.deepsense"            % s"deepsense-seahorse-$name" % Version.seahorse
  val spark     = (name: String) => "org.apache.spark"       %% s"spark-$name"              % Version.spark exclude("org.slf4j", "slf4j-log4j12")
  val spray     = (name: String) => "io.spray"               %% s"spray-$name"              % Version.spray

  val akkaActor          = akka("actor")
  val akkaTestkit        = akka("testkit")
  val apacheCommons      = "org.apache.commons"           %  "commons-lang3"      % Version.apacheCommons
  val avroCore           = avro("")
  // Please mind the dash to handle empty main module in avro and avro-ipc
  val avroRpc            = avro("-ipc")
  val cassandraUnit      = "org.cassandraunit"        %  "cassandra-unit"         % Version.cassandraUnit
  val datastaxCassandra  = "com.datastax.cassandra"   %  "cassandra-driver-core"  % Version.cassandra
  val guice              = "com.google.inject"            % "guice"               % Version.guice
  val guiceMultibindings = "com.google.inject.extensions" % "guice-multibindings" % Version.guice
  val hadoopClient       = hadoop("client")
  val hadoopCommon       = hadoop("common")
  val hadoopHdfs         = hadoop("hdfs")
  val hadoopYarnApi      = hadoop("yarn-api")
  val hadoopYarnClient   = hadoop("yarn-client")
  val hadoopYarnCommon   = hadoop("yarn-common")
  val jcloudsKeystone    = jclouds("keystone")
  val jcloudsCompute     = "org.apache.jclouds"           % "jclouds-compute"     % Version.jclouds
  val jcloudsNova        = jclouds("nova")
  val logbackClassic     = logback("classic")
  val logbackCore        = logback("core")
  val metricsScala       = "nl.grons"                    %% "metrics-scala"       % Version.metricsScala
  val mockitoCore        = "org.mockito"                  % "mockito-core"        % Version.mockito
  val nscalaTime         = "com.github.nscala-time"      %% "nscala-time"         % Version.nsscalaTime
  val scalacheck         = "org.scalacheck"              %% "scalacheck"          % Version.scalacheck
  val scalaLogging       = "com.typesafe.scala-logging"  %% "scala-logging"       % "3.1.0"
  val scalaReflect       = "org.scala-lang"               % "scala-reflect"       % Version.scala
  val scalatest          = "org.scalatest"               %% "scalatest"           % Version.scalatest
  val seahorseCommons    = seahorse("commons")
  val seahorseDeeplang   = seahorse("deeplang")
  val seahorseESClient   = seahorse("entitystorage-client")
  val seahorseESModel    = seahorse("entitystorage-model")
  val seahorseModels     = seahorse("models")
  val seahorseGraph      = seahorse("graph")
  val seahorseGraphJson  = seahorse("graph-json")
  val seahorseReportlib  = seahorse("reportlib")
  val sparkCore          = spark("core")
  val sparkMLLib         = spark("mllib")
  val sprayCan           = spray("can")
  val sprayRouting       = spray("routing")
  val sprayTestkit       = spray("testkit")
  val sprayClient        = spray("client")
  val sprayJson          = "io.spray"                    %% "spray-json"          % Version.sprayJson
}

object Dependencies {

  import Library._

  val resolvers = Seq(
    "typesafe.com"           at "http://repo.typesafe.com/typesafe/repo/",
    "sonatype.org"           at "https://oss.sonatype.org/content/repositories/releases",
    "spray.io"               at "http://repo.spray.io",
    "seahorse.deepsense.io"  at "http://10.10.1.77:8081/artifactory/simple/deepsense-seahorse-release"
  )

  val deploymodelservice = Seq(
    akkaActor,
    sprayCan,
    seahorseDeeplang,
    sprayJson,
    sprayRouting
  ) ++ Seq(scalatest, sprayTestkit).map(_ % s"$Test,it")

  val entitystorage = Seq(
    akkaActor,
    seahorseDeeplang
  ) ++ Seq(akkaTestkit, cassandraUnit, mockitoCore, scalatest, seahorseESClient, sprayTestkit).map(_ % s"$Test,it")

  val commons = Seq(
    akkaActor,
    apacheCommons,
    datastaxCassandra,
    jcloudsCompute,
    jcloudsKeystone,
    jcloudsNova,
    logbackClassic,
    logbackCore,
    scalaLogging,
    seahorseCommons,
    sprayCan,
    sprayJson,
    sprayRouting,
    guice,
    guiceMultibindings,
    metricsScala,
    nscalaTime
  ) ++ Seq(sprayTestkit, akkaTestkit, mockitoCore, scalatest, cassandraUnit).map(_ % Test)

  val workflowmanager = Seq(
    akkaActor,
    apacheCommons,
    guice,
    guiceMultibindings,
    seahorseDeeplang,
    seahorseESModel,
    seahorseGraphJson,
    seahorseESModel,
    seahorseReportlib,
    sprayCan,
    sprayClient,
    sprayJson,
    sprayRouting
  ) ++ Seq(sprayTestkit, akkaTestkit, mockitoCore, scalatest).map(_ % s"$Test,it")

  val graphexecutor = Seq(
    hadoopClient,
    hadoopCommon,
    hadoopHdfs,
    hadoopYarnApi,
    hadoopYarnClient,
    hadoopYarnCommon,
    seahorseDeeplang,
    seahorseESClient,
    seahorseESModel,
    seahorseGraph,
    seahorseModels
  ) ++ Seq(sparkCore).map(_ % Provided) ++ Seq(akkaTestkit, mockitoCore, scalatest).map(_ % s"$Test,it")
}
