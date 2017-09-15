import com.typesafe.sbt.SbtGit

enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)

imageNames in docker := Seq(ImageName(s"seahorse-schedulingmanager:${SbtGit.GitKeys.gitHeadCommit.value.get}"))

// TODO SMTP_PORT should be in docker compose instead
// 601xx ports are container-level and docker-compose level details concepts
dockerfile in docker := NativePackagerJavaAppDockerfile(stage.value, executableScriptName.value)
    .env("SMTP_PORT", "60111")
