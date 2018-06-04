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

package ai.deepsense.e2etests

import org.scalatest.WordSpec

import ai.deepsense.deeplang.CatalogRecorder
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.workflowexecutor.executor.WorkflowExecutor


class AllInputWorkflowsCorrectTest extends WordSpec {

  val graphReader = {
    val operationsCatalog = CatalogRecorder.resourcesCatalogRecorder.catalogs.operations
    new GraphReader(operationsCatalog)
  }

  TestWorkflowsIterator.foreach { case TestWorkflowsIterator.Input(uri, fileContents) =>
    s"Workflow from '$uri'" should {
      "be correctly formatted" in {
        val workflow = new WorkflowJsonConverter(graphReader).parseWorkflow(fileContents)
        val datasources = WorkflowExecutor.datasourcesFrom(workflow)
      }
    }
  }
}
