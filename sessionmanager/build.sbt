// Copyright (c) 2016, CodiLime Inc.

// scalastyle:off println

import com.typesafe.sbt.SbtGit
import com.typesafe.sbt.packager.docker._
import sbt.ProjectRef

name := "deepsense-sessionmanager"

libraryDependencies ++= Dependencies.sessionmanager
resolvers ++= Dependencies.resolvers

Revolver.settings

enablePlugins(JavaAppPackaging, GitVersioning, DeepsenseUniversalSettingsPlugin)

// If there are many `App` objects in project, docker image will crash with cryptic message
mainClass in Compile := Some("io.deepsense.sessionmanager.SessionManagerApp")

lazy val weSparkVersion = DeepsenseUniversalSettingsPlugin.weSparkVersion

lazy val workflowExecutorProject = ProjectRef(file("./seahorse-workflow-executor"), "workflowexecutor")
lazy val assembly = taskKey[File]("Copied from sbt-assembly's keys.")
lazy val weJar = taskKey[File]("Workflow executor runnable jar")
weJar := (assembly in workflowExecutorProject).value

mappings in Universal += weJar.value -> "we.jar"

lazy val preparePythonDeps = taskKey[File]("Generates we_deps.zip file with python dependencies")

preparePythonDeps := {
  Seq("sessionmanager/prepare-deps.sh", weSparkVersion).!!

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
  // TODO set image with proper spark version
  s"docker-repo.deepsense.codilime.com/deepsense_io/deepsense-mesos-spark:$seahorseBuildTag"
}

lazy val tiniVersion = "v0.10.0"

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
version in Docker := SbtGit.GitKeys.gitHeadCommit.value.get

// scalastyle:on
