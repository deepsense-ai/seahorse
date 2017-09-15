/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.e2etests.session

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

import org.scalatest._

import io.deepsense.commons.utils.OptionOpts._
import io.deepsense.e2etests.{ExampleWorkflowsFetcher, SeahorseIntegrationTestDSL, TestClusters}
import io.deepsense.models.workflows.WorkflowInfo
import io.deepsense.workflowmanager.model.WorkflowDescription


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
