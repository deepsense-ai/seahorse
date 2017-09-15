name := "Experiment Manger"

version := "0.1.0"

scalaVersion := "2.10.4"

ideaExcludeFolders += ".idea"

ideaExcludeFolders += ".idea_modules"

unmanagedClasspath in Test += baseDirectory.value / "conf"

unmanagedClasspath in Runtime += baseDirectory.value / "conf"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "typesafe.com" at "http://repo.typesafe.com/typesafe/repo/",
  "sonatype.org" at "https://oss.sonatype.org/content/repositories/releases",
  "spray.io" at "http://repo.spray.io"
)

libraryDependencies ++= {
  val akkaV = "2.3.7"
  val sprayV = "1.3.2"
  val scalaTestV = "2.2.+"
  Seq(
    "net.codingwell"      %% "scala-guice"   % "3.0.2",
    "io.spray"            %% "spray-can"     % sprayV,
    "io.spray"            %% "spray-routing" % sprayV,
    "io.spray"            %% "spray-json"    % "1.3.1",
    "io.spray"            %% "spray-testkit" % sprayV     % "test",
    "com.typesafe.akka"   %% "akka-actor"    % akkaV,
    "com.typesafe.akka"   %% "akka-testkit"  % akkaV      % "test",
    "org.specs2"          %% "specs2-core"   % "2.3.7"    % "test",
    "org.mockito"         %  "mockito-core"  % "1.10.19"  % "test",
    "org.scalatest"       %% "scalatest"     % scalaTestV % "test",
    "org.apache.jclouds"  %  "jclouds-all"   % "1.8.1"
  )
}

Revolver.settings: Seq[sbt.Def.Setting[_]]
