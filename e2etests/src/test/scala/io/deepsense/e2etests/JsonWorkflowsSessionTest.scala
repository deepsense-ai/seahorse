/**
  * Copyright (c) 2016, CodiLime Inc.
  */
package io.deepsense.e2etests

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatest.{Matchers, WordSpec}

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
