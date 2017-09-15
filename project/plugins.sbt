// Copyright (c) 2015, CodiLime, Inc.
//
// Owner: Robert Pohnke

resolvers += Classpaths.sbtPluginReleases

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.4")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

// sbt-avro plugin for generating the Java sources for Avro schemas and protocols
addSbtPlugin("com.cavorite" % "sbt-avro" % "0.3.2")

// Assembly plugin allows creation a fat JAR of project with all of its dependencies.
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.12.0")

// Plugin provides build info to use in code
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.4.0")

addSbtPlugin("com.github.sdb" % "xsbt-filter" % "0.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.0")
