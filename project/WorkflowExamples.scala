/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import scala.language.postfixOps

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

    val exitCode = Seq("/bin/bash", "-c",
      s"cd '${scriptDir.getCanonicalPath}'; " +
        s"python '$scriptFile' > '${outFile.getCanonicalPath}'") !

    if (exitCode != 0) {
      throw new RuntimeException(s"Unable to generate workflow examples sql file, script exited with code: $exitCode")
    }

    Seq(outFile)

  }

  lazy val generateWorkflowExamplesSql =
    taskKey[Seq[File]]("Generates file containing sql inserts for workflow examples")

  lazy val defaultSettings = inConfig(Compile) {
    Seq(
      workflowExamplesSqlFile := resourceManaged.value / "db/migration/workflowmanager/R__insert_examples.sql",
      workflowExamplesDir := baseDirectory.value / "../deployment/generate_examples/examples",
      generateWorkflowExamplesSql := {
        generateWorkflowExamplesSqlImpl(workflowExamplesDir.value, workflowExamplesSqlFile.value)
      },
      resourceGenerators += generateWorkflowExamplesSql.taskValue
    )
  }
}
