/**
  * Copyright (c) 2016, CodiLime Inc.
  */

package io.deepsense.e2etests

import org.scalatest.{FreeSpec, Matchers}

import io.deepsense.e2etests.client.WorkflowManagerClient
import io.deepsense.models.workflows.WorkflowInfo
import io.deepsense.workflowmanager.model.WorkflowDescription

class AllExampleWorkflowsWorkOnLocalClusterTest extends FreeSpec with Matchers with SeahorseIntegrationTestDSL {

  info("Assuming application will be accessible under localhost:33321")

  "All example workflows should be correct - all nodes run and completed successfully on a local cluster" - {
    ensureSeahorseIsRunning()
    val exampleWorkflows = getExampleWorkflows()
    for (exampleWorkflow <- exampleWorkflows) s"Workflow '${exampleWorkflow.name}'" in {
      val cloneDescription = WorkflowDescription(
        name = s"CLONE - ${exampleWorkflow.name}",
        description = exampleWorkflow.description
      )
      val clonedWorkflowId = WorkflowManagerClient.cloneWorkflow(exampleWorkflow.id, cloneDescription)
      val clonedWorkflow = WorkflowManagerClient.getWorkflow(clonedWorkflowId)

      withExecutor(clonedWorkflowId, TestClusters.local()) { implicit ctx =>
        launch(clonedWorkflowId)
        assertAllNodesCompletedSuccessfully(clonedWorkflow)
      }

      WorkflowManagerClient.deleteWorkflow(clonedWorkflowId)
    }
  }

  def getExampleWorkflows(): Seq[WorkflowInfo] = {
    val workflows = WorkflowManagerClient.getWorkflows()
    val exampleWorkflows = workflows.filter(_.ownerName == "seahorse")
    // Order matters because of
    // - `Write Transformer` in Example 1
    // - `Read Transformer` in Example 4
    exampleWorkflows.sortBy(_.name)
  }

}
