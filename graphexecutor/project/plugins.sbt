// Copyright (c) 2015, CodiLime, Inc.
//
// Owner: Grzegorz Chilkiewicz

logLevel := Level.Warn

// sbt-avro plugin for generating the Java sources for Avro schemas and protocols
addSbtPlugin("com.cavorite" % "sbt-avro" % "0.3.2")

// Assembly plugin allows creation a fat JAR of project with all of its dependencies.
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.12.0")
