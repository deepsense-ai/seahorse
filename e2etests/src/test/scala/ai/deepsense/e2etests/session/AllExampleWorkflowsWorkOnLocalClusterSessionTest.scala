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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

import org.scalatest._

import ai.deepsense.commons.utils.OptionOpts._
import ai.deepsense.e2etests.{ExampleWorkflowsFetcher, SeahorseIntegrationTestDSL, TestClusters}
import ai.deepsense.models.workflows.WorkflowInfo
import ai.deepsense.workflowmanager.model.WorkflowDescription


class AllExampleWorkflowsWorkOnLocalClusterSessionTest
  extends WordSpec
  with Matchers
  with SeahorseIntegrationTestDSL
  with ExampleWorkflowsFetcher {

  info("Assuming application will be accessible under localhost:33321")

  ensureSeahorseIsRunning()

  val exampleWorkflows = Await.result(fetchExampleWorkflows(), httpTimeout)

  for {
    exampleWorkflow <- exampleWorkflows
  } {
    s"Example workflow '${exampleWorkflow.name}'" should {
      "run successfully in session mode on local cluster" in {
        val testFuture = for {
          workflow <- cloneWorkflow(exampleWorkflow)
          _ <- runAndCleanupWorkflow(workflow, TestClusters.local())
        } yield ()
        Await.result(testFuture, workflowTimeout)
      }
    }
  }

  def cloneWorkflow(workflow: WorkflowInfo): Future[WorkflowInfo] = {
    val cloneDescription = WorkflowDescription(
      name = s"CLONE - ${workflow.name}",
      description = workflow.description
    )
    val clonedWorkflowIdFut = wmclient.cloneWorkflow(workflow.id, cloneDescription)

    for {
      id <- clonedWorkflowIdFut
      workflows <- wmclient.fetchWorkflows()
      workflow <- workflows.find(_.id == id).asFuture
    } yield {
      workflow
    }
  }
}
