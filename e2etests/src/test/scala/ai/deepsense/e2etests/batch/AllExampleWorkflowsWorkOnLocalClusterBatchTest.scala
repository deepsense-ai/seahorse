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
package ai.deepsense.e2etests.batch

import scala.concurrent.{Await, Future}

import org.scalatest.{Matchers, WordSpec}

import ai.deepsense.e2etests.{ExampleWorkflowsFetcher, TestClusters}
import ai.deepsense.models.workflows.WorkflowInfo

class AllExampleWorkflowsWorkOnLocalClusterBatchTest
  extends WordSpec
  with Matchers
  with ExampleWorkflowsFetcher
  with BatchTestInDockerSupport {

  ensureSeahorseIsRunning()

  val exampleWorkflows = Await.result(fetchExampleWorkflows(), httpTimeout)

  for {
    workflowInfo <- exampleWorkflows
  } {
    s"Example workflow '${workflowInfo.name}'" should {
      "run successfully in batch mode on local cluster" in {
        val fileName = s"${workflowInfo.name}.json"
        val testFuture = testWorkflowFromSeahorse(TestClusters.local(), fileName, workflowInfo.id)
        Await.result(testFuture, workflowTimeout)
      }
    }
  }
}
