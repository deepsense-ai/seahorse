// Copyright (c) 2015, CodiLime Inc.

resolvers += Classpaths.sbtPluginReleases

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

// Assembly plugin allows creation a fat JAR of project with all of its dependencies.
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.12.0")

// Plugin provides build info to use in code
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.4.0")

logLevel := Level.Warn

