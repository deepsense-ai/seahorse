import sbt._

object Version {
  val akka        = "2.3.9"
  val avro        = "1.7.7"
  val guice       = "3.0"
  val hadoop      = "2.6.0"
  val jclouds     = "1.8.1"
  val jsr305      = "3.0.0"
  val mockito     = "1.10.19"
  val nsscalaTime = "1.8.0"
  val scala       = "2.11.6"
  val scalatest   = "3.0.0-SNAP4"
  val spark       = "1.3.0"
  val spray       = "1.3.3"
  val sprayJson   = "1.3.1"
}

object Library {

  val akka   = (name: String) => "com.typesafe.akka" %% s"akka-$name"   % Version.akka
  val avro   = (name: String) => "org.apache.avro"    % s"avro$name"    % Version.avro
  val hadoop = (name: String) => "org.apache.hadoop"  % s"hadoop-$name" % Version.hadoop
  val spark  = (name: String) => "org.apache.spark"  %% s"spark-$name"  % Version.spark
  val spray  = (name: String) => "io.spray"          %% s"spray-$name"  % Version.spray

  val akkaActor          = akka("actor")
  val apacheCommons      = "org.apache.commons"           %  "commons-lang3"      % "3.3.+"
  val akkaTestkit        = akka("testkit")
  val avroCore           = avro("")
  // Please mind the dash to handle empty main module in avro and avro-ipc
  val avroRpc            = avro("-ipc")
  val guice              = "com.google.inject"            % "guice"               % Version.guice
  val guiceMultibindings = "com.google.inject.extensions" % "guice-multibindings" % Version.guice
  val hadoopClient       = hadoop("client")
  val hadoopCommon       = hadoop("common")
  val hadoopHdfs         = hadoop("hdfs")
  val hadoopYarnApi      = hadoop("yarn-api")
  val hadoopYarnClient   = hadoop("yarn-client")
  val hadoopYarnCommon   = hadoop("yarn-common")
  val jclouds            = "org.apache.jclouds"           % "jclouds-all"         % Version.jclouds
  val mockitoCore        = "org.mockito"                  % "mockito-core"        % Version.mockito
  val nscalaTime         = "com.github.nscala-time"      %% "nscala-time"         % Version.nsscalaTime
  val scalaReflect       = "org.scala-lang"               % "scala-reflect"       % Version.scala
  val scalatest          = "org.scalatest"               %% "scalatest"           % Version.scalatest
  val sparkSql           = spark("sql")
  val sparkCore          = spark("core")
  val sprayCan           = spray("can")
  val sprayRouting       = spray("routing")
  val sprayTestkit       = spray("testkit")
  val sprayJson          = "io.spray"                    %% "spray-json"          % Version.sprayJson
}

object Dependencies {

  import Library._

  val resolvers = Seq(
    "typesafe.com" at "http://repo.typesafe.com/typesafe/repo/",
    "sonatype.org" at "https://oss.sonatype.org/content/repositories/releases",
    "spray.io"     at "http://repo.spray.io"
  )

  val deeplang = Seq(
    nscalaTime,
    scalaReflect,
    sparkSql,
    sparkCore,
    sprayJson
  ) ++ Seq(scalatest, mockitoCore).map(_ % "test")

  val experimentmanager = Seq(
    jclouds,
    guice,
    guiceMultibindings,
    sprayCan,
    sprayRouting,
    sprayJson,
    akkaActor,
    apacheCommons
  ) ++ Seq(sprayTestkit, akkaTestkit, mockitoCore, scalatest).map(_ % "test")

  val graph = Seq(
    nscalaTime
  ) ++ Seq(scalatest).map(_ % "test")

  val graphexecutor = Seq(
    hadoopCommon,
    hadoopYarnClient,
    hadoopYarnApi,
    hadoopYarnCommon,
    hadoopHdfs,
    hadoopClient,
    avroCore,
    avroRpc
  ) ++ Seq(sparkCore).map(_ % "provided") ++ Seq(scalatest).map(_ % "test")

  val graphJson = Seq(
    nscalaTime,
    sprayJson
  ) ++ Seq(scalatest, mockitoCore).map(_ % "test")
}
