import sbt._

object Version {
  val spark         = "1.4.0"
  val apacheCommons = "3.3.+"
  val sprayJson     = "1.3.1"
  val mockito       = "1.10.19"
  val nsscalaTime   = "1.8.0"
  val scala         = "2.11.6"
  val scalatest     = "3.0.0-SNAP4"
  val spray         = "1.3.3"
}

object Library {
  val spark   = (name: String) => "org.apache.spark"       %% s"spark-$name"         % Version.spark
  val spray   = (name: String) => "io.spray"               %% s"spray-$name"         % Version.spray

  val apacheCommons      = "org.apache.commons"             %  "commons-lang3"       % Version.apacheCommons
  val log4JExtras        = "log4j"                          %  "apache-log4j-extras" % "1.2.17"
  val nscalaTime         = "com.github.nscala-time"        %%  "nscala-time"         % Version.nsscalaTime
  val mockitoCore        = "org.mockito"                    %  "mockito-core"        % Version.mockito
  val slf4jLog4j         = "org.slf4j"                      %  "slf4j-log4j12"       % "1.7.12"
  val sprayCan           = spray("can")
  val sprayHttpx         = spray("httpx")
  val sprayJson          = "io.spray"                      %% "spray-json"           % Version.sprayJson
  val scalaLogging       = "com.typesafe.scala-logging"    %% "scala-logging"        % "3.1.0"
  val scalatest          = "org.scalatest"                 %% "scalatest"            % Version.scalatest
  val sparkCore          = spark("core")
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
    sprayCan,
    sprayHttpx,
    sprayJson
  ) ++ Seq(mockitoCore, scalatest).map(_ % Test)

  val workflowexecutor = Seq(
  ) ++ Seq(sparkCore).map(_ % Provided)
}
