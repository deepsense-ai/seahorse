// Copyright (c) 2015, CodiLime Inc.
//
// Owner: Jacek Laskowski

organization := "io.deepsense"
name         := "experiment-manager"
version      := "0.1.0"
scalaVersion := "2.11.6"

unmanagedClasspath in Test += baseDirectory.value / "conf"

unmanagedClasspath in Runtime += baseDirectory.value / "conf"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "typesafe.com" at "http://repo.typesafe.com/typesafe/repo/",
  "sonatype.org" at "https://oss.sonatype.org/content/repositories/releases",
  "spray.io" at "http://repo.spray.io"
)

libraryDependencies ++= {
  val akkaV  = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "net.codingwell"    %% "scala-guice"   % "4.0.0-beta5",
    "io.spray"          %% "spray-can"     % sprayV,
    "io.spray"          %% "spray-routing" % sprayV,
    "io.spray"          %% "spray-json"    % "1.3.1",
    "io.spray"          %% "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka" %% "akka-actor"    % akkaV,
    "com.typesafe.akka" %% "akka-testkit"  % akkaV   % "test"
  )
}

Revolver.settings
