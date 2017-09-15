import sbt.File

object NativePackagerJavaAppDockerfile {

  import sbtdocker._

  def apply(appDir: File, executableScriptName: String): Dockerfile = new Dockerfile()
    .from("anapsix/alpine-java:jre8")
    .user("root")
    .workDir("/opt/docker")
    .copy(appDir, targetDir)
    .entryPoint(s"$targetDir/bin/$executableScriptName")

  private val targetDir = "app"

}
