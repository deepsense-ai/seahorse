// Copyright (c) 2015, CodiLime, Inc.
//
// Owner: Robert Pohnke
//
// SBT plugin declarations

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.4")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")
