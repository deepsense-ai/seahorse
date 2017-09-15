/**
 * Copyright (c) 2016, CodiLime Inc.
 */
package io.deepsense.e2etests

import org.scalatest.{Matchers, WordSpec}

class JsonWorkflowsTest extends WordSpec with Matchers with SeahorseIntegrationTestDSL {

  ensureSeahorseIsRunning()
  TestWorkflowsIterator.foreach { case TestWorkflowsIterator.Input(path, fileContents) =>
    s"Workflow loaded from '$path'" should {
      "be correct - all nodes run and completed successfully" in {
        val id = uploadWorkflow(fileContents)
        withExecutor(id) { implicit ctx =>
          launch(id)
          assertAllNodesCompletedSuccessfully(id)
        }
        deleteWorkflow(id)
      }
    }
  }
}
