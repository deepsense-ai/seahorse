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
package ai.deepsense.e2etests.session

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatest.{Matchers, WordSpec}

import ai.deepsense.e2etests.{SeahorseIntegrationTestDSL, TestClusters, TestDatasourcesInserter, TestWorkflowsIterator}

class JsonWorkflowsSessionTest
  extends WordSpec
  with Matchers
  with SeahorseIntegrationTestDSL
  with TestDatasourcesInserter {

  ensureSeahorseIsRunning()

  insertDatasourcesForTest()

  TestWorkflowsIterator.foreach { case TestWorkflowsIterator.Input(uri, fileContents) =>
    s"Workflow loaded from '$uri'" should {
      "be correct - all nodes run and completed successfully" when {
        for (cluster <- TestClusters.allAvailableClusters) {
          s"run on ${cluster.clusterType} cluster" in {
            Await.result({
              for {
                workflow <- uploadWorkflow(fileContents)
                _ <- runAndCleanupWorkflow(workflow, cluster)
              } yield ()
            }, workflowTimeout)
          }
        }
      }
    }
  }
}
