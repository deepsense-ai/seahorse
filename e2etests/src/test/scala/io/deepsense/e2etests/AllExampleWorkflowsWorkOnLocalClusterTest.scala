/**
  * Copyright (c) 2016, CodiLime Inc.
  */

package io.deepsense.e2etests

import org.scalatest.{FreeSpec, Matchers, WordSpec}

class AllExampleWorkflowsWorkOnLocalClusterTest extends FreeSpec with Matchers with SeahorseIntegrationTestDSL {

  info("Assuming application will be accessible under localhost:33321")

  "All example workflows should be correct - all nodes run and completed successfully on a local cluster" in {
    ensureSeahorseIsRunning()
    val exampleWorkflowIds = getExampleWorkflowsIds()
    exampleWorkflowIds should not be empty
    exampleWorkflowIds.foreach { exampleWorkflowId =>
      val clonedWorkflowId = cloneWorkflow(exampleWorkflowId)

      withExecutor(clonedWorkflowId, TestClusters.local()) { implicit ctx =>
        launch(clonedWorkflowId)
        assertAllNodesCompletedSuccessfully(clonedWorkflowId)
      }

      deleteWorkflow(clonedWorkflowId)
    }
  }
}