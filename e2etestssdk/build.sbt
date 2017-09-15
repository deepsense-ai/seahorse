name := "e2etests-sdk"
version := "1.0"
scalaVersion := "2.11.8"
// TODO Use stable version - this is snapshot from branch dev_sdk
resolvers += Resolver.sonatypeRepo("public")
libraryDependencies += "io.deepsense" % "deepsense-seahorse-deeplang_2.11" % "1.3.0-LOCAL-SNAPSHOT" % "provided"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
