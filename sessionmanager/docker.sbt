import com.typesafe.sbt.SbtGit

enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)

lazy val workflowExecutorProject = ProjectRef(file("./seahorse-workflow-executor"), "workflowexecutor")
lazy val assembly = taskKey[File]("Copied from sbt-assembly's keys.")
lazy val weJar = taskKey[File]("Workflow executor runnable jar")
weJar := (assembly in workflowExecutorProject).value

lazy val weSparkVersion = DeepsenseUniversalSettingsPlugin.weSparkVersion
lazy val pythonAndRDeps = taskKey[File]("Generates we_deps.zip file with python and R dependencies")
pythonAndRDeps := {
  Seq("sessionmanager/prepare-deps.sh", weSparkVersion).!!
  target.value / "we-deps.zip"
}
pythonAndRDeps <<= pythonAndRDeps dependsOn weJar

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

imageNames in docker := Seq(ImageName(s"deepsense-sessionmanager:${SbtGit.GitKeys.gitHeadCommit.value.get}"))

dockerfile in docker := {
  val sessionManagerAppDir = stage.value

  new Dockerfile {
    from(dockerBaseImage.value)

    user("root")
    workDir("/opt/docker")

    runRaw("/opt/conda/bin/pip install pika==0.10.0")

    // Add Tini - so the python zombies can be collected
    env("TINI_VERSION", tiniVersion)
    addRaw(s"https://github.com/krallin/tini/releases/download/$tiniVersion/tini", "/bin/tini")
    runRaw("chmod +x /bin/tini")

    copy(pythonAndRDeps.value, "we-deps.zip")
    copy(weJar.value, "we.jar")
    copy(sessionManagerAppDir, "app")

    entryPoint("/bin/tini", "--")
    cmd("app/bin/deepsense-sessionmanager")
  }
}
