/**
  * Copyright (c) 2016, CodiLime Inc.
  */
package io.deepsense.e2etests

import org.scalatest.{FreeSpec, Matchers, WordSpec}

class JsonWorkflowsTest extends WordSpec with Matchers with SeahorseIntegrationTestDSL {

  ensureSeahorseIsRunning()
  TestWorkflowsIterator.foreach { case TestWorkflowsIterator.Input(path, fileContents) =>
    s"Workflow loaded from '$path'" should {
      "be correct - all nodes run and completed successfully" when {
        for (cluster <- TestClusters.allAvailableClusters) {
          s"run on ${cluster.clusterType} cluster" in {
            val id = uploadWorkflow(fileContents)
            withExecutor(id, cluster) { implicit ctx =>
              launch(id)
              assertAllNodesCompletedSuccessfully(id)
            }
            deleteWorkflow(id)
          }
        }
      }
    }
  }
}
