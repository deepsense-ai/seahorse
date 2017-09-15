import com.typesafe.sbt.SbtGit
import com.typesafe.sbt.packager.docker.Cmd

dockerBaseImage := "anapsix/alpine-java:jre8"
dockerExposedPorts := Seq(9083)
dockerUpdateLatest := true
version in Docker := SbtGit.GitKeys.gitHeadCommit.value.get
dockerCommands ++= Seq(
  Cmd("USER", "root")
)
