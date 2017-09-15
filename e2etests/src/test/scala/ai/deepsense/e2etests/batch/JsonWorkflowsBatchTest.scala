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

import scala.concurrent.Await
import org.scalatest.{Matchers, WordSpec}
import ai.deepsense.e2etests.{TestClusters, TestWorkflowsIterator}
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.ClusterType

class JsonWorkflowsBatchTest
  extends WordSpec
  with Matchers
  with BatchTestInDockerSupport {

  import scala.concurrent.ExecutionContext.Implicits.global

  ensureSeahorseIsRunning()

  insertDatasourcesForTest()

  TestWorkflowsIterator.foreach { case TestWorkflowsIterator.Input(uri, fileContents) =>
    s"Workflow loaded from '$uri'" should {
      "complete successfully in batch mode" when {
        for (cluster <- TestClusters.allAvailableClusters) {
          s"run on ${cluster.clusterType} cluster" in {
            if (cluster.clusterType == ClusterType.yarn && uri.toString.contains("SDK_Identity")) {
              cancel("TODO: Currently SDK with YARN in BATCH is not supported. ADD IT LATER")
            }
            val testFuture = for {
              workflowInfo <- uploadWorkflow(fileContents)
              _ <- testWorkflowFromSeahorse(cluster, uri.getPath, workflowInfo.id)
            } yield ()
            Await.result(testFuture, workflowTimeout)
          }
        }
      }
    }
  }
}
