/**
  * Copyright (c) 2016, CodiLime Inc.
  */
package io.deepsense.e2etests.session

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatest.{Matchers, WordSpec}

import io.deepsense.e2etests.{SeahorseIntegrationTestDSL, TestClusters, TestDatasourcesInserter, TestWorkflowsIterator}

class JsonWorkflowsSessionTest
  extends WordSpec
  with Matchers
  with SeahorseIntegrationTestDSL
  with TestDatasourcesInserter {

  ensureSeahorseIsRunning()

  insertDatasourcesForTest()

  TestWorkflowsIterator.foreach { case TestWorkflowsIterator.Input(uri, file, fileContents) =>
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
