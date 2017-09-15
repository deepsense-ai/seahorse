import com.typesafe.sbt.SbtGit

enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)

imageNames in docker := Seq(ImageName(s"seahorse-workflowmanager:${SbtGit.GitKeys.gitHeadCommit.value.get}"))

dockerfile in docker := NativePackagerJavaAppDockerfile(stage.value, executableScriptName.value)
