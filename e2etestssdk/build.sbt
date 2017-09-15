name := "e2etests-sdk"
version := "1.0"
scalaVersion := "2.11.8"
resolvers += Resolver.sonatypeRepo("public")
libraryDependencies += "io.deepsense" % "deepsense-seahorse-deeplang_2.11" % "1.4.0-RC1" % "provided"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
