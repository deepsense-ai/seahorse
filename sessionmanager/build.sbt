// Copyright (c) 2016, CodiLime Inc.

// scalastyle:off println

import com.typesafe.sbt.SbtGit
import com.typesafe.sbt.packager.docker._

name := "deepsense-sessionmanager"

libraryDependencies ++= Dependencies.sessionmanager
resolvers ++= Dependencies.resolvers

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

// If there are many `App` objects in project, docker image will crash with cryptic message
mainClass in Compile := Some("io.deepsense.sessionmanager.SessionManagerApp")

val weJar = taskKey[File]("Workflow executor runnable jar")

weJar := {
  val jar =
    new File("seahorse-workflow-executor/workflowexecutor/target/scala-2.11/workflowexecutor.jar")

  val assemblyCmd = "sbt workflowexecutor/assembly"

  if(jar.exists()) {
    println(
      s"""
         |Workflow executor jar in nested repo already exist. Assuming it's up to date.
         |If you need to rebuild we.jar run `$assemblyCmd` in embedded WE repo.
          """.stripMargin
    )
  } else {
    val shell = Seq("bash", "-c")
    shell :+ s"cd seahorse-workflow-executor; $assemblyCmd" !
  }

  jar
}

mappings in Universal += weJar.value -> "we.jar"

val preparePythonDeps = taskKey[File]("Generates we_deps.zip file with python dependencies")

preparePythonDeps := {
  "sessionmanager/prepare-deps.sh" !

  target.value / "we-deps.zip"
}

preparePythonDeps <<= preparePythonDeps dependsOn weJar

mappings in Universal += preparePythonDeps.value -> "we-deps.zip"

dockerBaseImage := {
  // Require environment variable SEAHORSE_BUILD_TAG to be set
  // This variable indicates tag of base image for sessionmanager image
  val seahorseBuildTag = {
    scala.util.Properties.envOrNone("SEAHORSE_BUILD_TAG").getOrElse {
      println("SEAHORSE_BUILD_TAG is not defined. Trying to use $GITBRANCH-latest")
      s"${SbtGit.GitKeys.gitCurrentBranch.value}-latest"
    }
  }
  s"docker-repo.deepsense.codilime.com/deepsense_io/deepsense-mesos-spark:$seahorseBuildTag"
}

val tiniVersion = "v0.10.0"

dockerCommands ++= Seq(
// Add Tini - so the python zombies can be collected
  Cmd("ENV", "TINI_VERSION", tiniVersion),
  Cmd("ADD", s"https://github.com/krallin/tini/releases/download/$tiniVersion/tini", "/bin/tini"),
  Cmd("USER", "root"),
  Cmd("RUN", "chmod", "+x", "/bin/tini"),
  Cmd("RUN", "/opt/conda/bin/pip install pika==0.10.0"),
  ExecCmd("ENTRYPOINT", "/bin/tini", "--"),
  ExecCmd("CMD", "bin/deepsense-sessionmanager")
)
dockerUpdateLatest := true

// scalastyle:on
