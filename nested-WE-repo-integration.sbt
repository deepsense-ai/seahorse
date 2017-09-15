// scalastyle:off println

val cleanWe = TaskKey[Unit]("cleanWe", "Execute clean in WE repo")
val publishWeClasses = TaskKey[Unit]("publishWeClasses", "Publish WE code to embedded ivy repo")

val shell = Seq("bash", "-c")

cleanWe := {
  shell :+ "cd seahorse-workflow-executor; sbt clean" !
}

clean <<= clean dependsOn cleanWe

publishWeClasses := {
  val workflowExecutorEmbeddedIvyRepo = new File("seahorse-workflow-executor/target/ds-workflow-executor-ivy-repo")

  val nestedRepoCmd = "sbt publish"
  if (workflowExecutorEmbeddedIvyRepo.exists()) {
    println(
      s"""
       |WE classes in embedded ivy repo already exists. Assuming it's up to date.
       |If you need to refresh WE classes run `$nestedRepoCmd` task in embedded WE repo
       |or run ${cleanWe.key.label} task.
       """.stripMargin
    )
  } else {
    shell :+ s"cd seahorse-workflow-executor; $nestedRepoCmd" !
  }
}

// TODO Make compile task depending on publishWeClasses
//compile in Compile <<= (compile in Compile) dependsOn publishWeClasses

// scalastyle:on