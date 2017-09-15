/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.e2etests

import org.scalatest.{Matchers, WordSpec}

class AllExampleWorkflowsCompletedTest extends WordSpec with Matchers with SeahorseIntegrationTestDSL {

  info("Assuming application will be accessible under localhost:33321")

  "All examples workflows" should {
    "be correct - all nodes run and completed successfully" when {

      ensureSeahorseIsRunning()
      val exampleWorkflowIds = getExampleWorkflowsIds()
      exampleWorkflowIds should not be empty
      exampleWorkflowIds.foreach { exampleWorkflowId =>
        val clonedWorkflowId = cloneWorkflow(exampleWorkflowId)

        withExecutor(clonedWorkflowId) { implicit ctx =>
          launch(clonedWorkflowId)
          assertAllNodesCompletedSuccessfully(clonedWorkflowId)
        }

        deleteWorkflow(clonedWorkflowId)
      }
    }
  }



}
