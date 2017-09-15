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
  val mockito       = "1.10.19"
  val nsscalaTime   = "1.8.0"
  val scala         = "2.11.6"
  val scalacheck    = "1.12.2"
  val scalatest     = "3.0.0-SNAP4"
  val spark         = "1.3.0"
  val spray         = "1.3.3"
  val sprayJson     = "1.3.1"
}

object Library {

  val akka    = (name: String) => "org.spark-project.akka"  %% s"akka-$name"      % Version.akka
  val avro    = (name: String) => "org.apache.avro"         % s"avro$name"        % Version.avro
  val hadoop  = (name: String) => "org.apache.hadoop"       % s"hadoop-$name"     % Version.hadoop exclude("org.slf4j", "slf4j-log4j12")
  val jclouds = (name: String) => "org.apache.jclouds.api"  % s"openstack-$name"  % Version.jclouds
  val logback = (name: String) => "ch.qos.logback"          % s"logback-$name"    % Version.logback
  val spark   = (name: String) => "org.apache.spark"       %% s"spark-$name"      % Version.spark exclude("org.slf4j", "slf4j-log4j12")
  val spray   = (name: String) => "io.spray"               %% s"spray-$name"      % Version.spray

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
  val mockitoCore        = "org.mockito"                  % "mockito-core"        % Version.mockito
  val nscalaTime         = "com.github.nscala-time"      %% "nscala-time"         % Version.nsscalaTime
  val scalacheck         = "org.scalacheck"              %% "scalacheck"          % Version.scalacheck
  val scalaLogging       = "com.typesafe.scala-logging"  %% "scala-logging"       % "3.1.0"
  val scalaReflect       = "org.scala-lang"               % "scala-reflect"       % Version.scala
  val scalatest          = "org.scalatest"               %% "scalatest"           % Version.scalatest
  val sparkSql           = spark("sql")
  val sparkCore          = spark("core")
  val sparkMLLib         = spark("mllib")
  val sprayCan           = spray("can")
  val sprayRouting       = spray("routing")
  val sprayTestkit       = spray("testkit")
  val sprayClient       = spray("client")
  val sprayJson          = "io.spray"                    %% "spray-json"          % Version.sprayJson
}

object Dependencies {

  import Library._

  val resolvers = Seq(
    "typesafe.com" at "http://repo.typesafe.com/typesafe/repo/",
    "sonatype.org" at "https://oss.sonatype.org/content/repositories/releases",
    "spray.io"     at "http://repo.spray.io"
  )

  val deploymodelservice = Seq(
    akkaActor,
    sprayCan,
    sprayJson,
    sprayRouting
  ) ++ Seq(scalatest, sprayTestkit).map(_ % s"$Test,it")

  val entitystorage = Seq(
    akkaActor
  ) ++ Seq(scalatest, mockitoCore, sprayTestkit, cassandraUnit, akkaTestkit).map(_ % s"$Test,it")

  val entitystorageClient = Seq(
    akkaActor
  ) ++ Seq(scalatest, mockitoCore, akkaTestkit).map(_ % Test)

  val reportlib = Seq(
    sprayJson
  ) ++ Seq(scalatest, mockitoCore).map(_ % Test)

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
    sparkSql,
    sprayCan,
    sprayJson,
    sprayRouting,
    guice,
    guiceMultibindings,
    nscalaTime
  ) ++ Seq(sprayTestkit, akkaTestkit, mockitoCore, scalatest, cassandraUnit).map(_ % Test)

  val deeplang = Seq(
    nscalaTime,
    sprayClient,
    scalaReflect,
    sparkSql,
    sparkMLLib,
    sparkCore
  ) ++ Seq(scalatest, mockitoCore, scalacheck).map(_ % Test)

  val experimentmanager = Seq(
    guice,
    guiceMultibindings,
    sprayCan,
    sprayClient,
    sprayRouting,
    sprayJson,
    akkaActor,
    apacheCommons
  ) ++ Seq(sprayTestkit, akkaTestkit, mockitoCore, scalatest).map(_ % s"$Test,it")

  val graph = Seq(
    nscalaTime
  ) ++ Seq(scalatest).map(_ % Test)

  val graphexecutor = Seq(
    hadoopCommon,
    hadoopYarnClient,
    hadoopYarnApi,
    hadoopYarnCommon,
    hadoopHdfs,
    hadoopClient,
    avroCore,
    avroRpc
  ) ++ Seq(sparkCore).map(_ % Provided) ++ Seq(scalatest).map(_ % s"$Test,it")

  val graphJson = Seq(
    nscalaTime,
    sprayJson
  ) ++ Seq(scalatest, mockitoCore).map(_ % Test)
}
