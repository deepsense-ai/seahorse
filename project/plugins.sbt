// Copyright (c) 2015, CodiLime Inc.

resolvers ++= Seq(
  Classpaths.sbtPluginReleases,
  "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
   Resolver.url("Deepsense Ivy Releases", url(
     "http://artifactory.deepsense.codilime.com:8081/artifactory/deepsense-io-ivy"
   ))(Resolver.defaultIvyPatterns)
)

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.4")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

// Plugin provides build info to use in code
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.4.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.4")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.4")

addSbtPlugin("io.deepsense" %% "scalatra-swagger-codegen" % "1.6")
