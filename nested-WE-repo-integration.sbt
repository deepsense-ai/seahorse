// scalastyle:off println

val cleanWe = TaskKey[Unit]("cleanWe", "Execute clean in WE repo")

val shell = Seq("bash", "-c")

cleanWe := {
  shell :+ "cd seahorse-workflow-executor; sbt clean" !
}

clean := (clean dependsOn cleanWe.toTask).value

// scalastyle:on
