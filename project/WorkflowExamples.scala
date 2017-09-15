/**
 * Copyright (c) 2016, CodiLime Inc.
 */

import scala.languageFeature.postfixOps

import sbt.Keys._
import sbt._

object WorkflowExamples {

  lazy val workflowExamplesDir = settingKey[File]("Directory with example workflows to be inserted into db")
  lazy val workflowExamplesSqlFile = settingKey[File]("Output sql file")

  def generateWorkflowExamplesSqlImpl(examplesDir: File, outFile: File): Seq[File] = {

    val scriptDir = examplesDir / ".."
    val scriptFile = "generate_workflow_examples_sql.py"
    val outFileDir = outFile.getParentFile
    outFileDir.mkdirs()

    Seq("/bin/bash", "-c",
      s"cd ${scriptDir.getCanonicalPath}; python $scriptFile > ${outFile.getCanonicalPath}") !

    Seq(outFile)

  }

  lazy val generateWorkflowExamplesSql =
    taskKey[Seq[File]]("Generates file containing sql inserts for workflow examples")

  lazy val defaultSettings = inConfig(Compile) {
    Seq(
      workflowExamplesSqlFile := resourceManaged.value / "db/migration/V2__insert_examples.sql",
      workflowExamplesDir := baseDirectory.value / "../deployment/generate_examples/examples",
      generateWorkflowExamplesSql := {
        generateWorkflowExamplesSqlImpl(workflowExamplesDir.value, workflowExamplesSqlFile.value)
      },
      resourceGenerators += generateWorkflowExamplesSql.taskValue
    )
  }
}
