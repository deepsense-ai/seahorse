// Copyright (c) 2015, CodiLime Inc.

resolvers ++= Seq(
  Classpaths.sbtPluginReleases,
  "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "sonatype-public" at "https://oss.sonatype.org/content/groups/public/"
)

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.4")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

// Plugin provides build info to use in code
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.4.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.4")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.4")

addSbtPlugin("ai.deepsense" %% "scalatra-swagger-codegen" % "1.7")

addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.4.1")

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "0.5.1")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")
