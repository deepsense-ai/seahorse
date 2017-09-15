import sbt._

object Version {
  val spark         = "1.4.0"
}

object Library {
  val spark   = (name: String) => "org.apache.spark"       %% s"spark-$name"      % Version.spark

  val sparkCore          = spark("core")
  // exclude("org.slf4j", "slf4j-log4j12")
}

object Dependencies {

  import Library._

  val resolvers = Seq(
    "typesafe.com" at "http://repo.typesafe.com/typesafe/repo/",
    "sonatype.org" at "https://oss.sonatype.org/content/repositories/releases",
    "spray.io"     at "http://repo.spray.io"
  )

  val workflowexecutor = Seq(
  ) ++ Seq(sparkCore).map(_ % Provided)
}
