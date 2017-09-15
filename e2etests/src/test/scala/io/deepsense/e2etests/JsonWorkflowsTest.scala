/**
  * Copyright (c) 2016, CodiLime Inc.
  */
package io.deepsense.e2etests

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

import org.scalatest.{Matchers, WordSpec}

import io.deepsense.models.workflows.WorkflowInfo


class JsonWorkflowsTest extends WordSpec with Matchers with SeahorseIntegrationTestDSL {

  ensureSeahorseIsRunning()
  TestWorkflowsIterator.foreach { case TestWorkflowsIterator.Input(path, fileContents) =>
    s"Workflow loaded from '$path'" should {
      "be correct - all nodes run and completed successfully" when {
        for (cluster <- TestClusters.allAvailableClusters) {
          s"run on ${cluster.clusterType} cluster" in {
            Await.result({
              val workflowFut = uploadWorkflow(fileContents)
              workflowFut.flatMap { workflow =>
                val id = workflow.id

                createSessionSynchronously(id, cluster)
                smclient.launchSession(id)

                assertAllNodesCompletedSuccessfully(workflow)

                smclient.deleteSession(id)
                wmclient.deleteWorkflow(id)

              }
            }, workflowTimeout)
          }
        }
      }
    }
  }

  def uploadWorkflow(fileContents: String): Future[WorkflowInfo] = {
    for {
      id <- wmclient.uploadWorkflow(fileContents)
      wflows <- wmclient.fetchWorkflows()
      wflow = wflows.find(_.id == id)
    } yield {
      wflow.get
    }
  }
}
