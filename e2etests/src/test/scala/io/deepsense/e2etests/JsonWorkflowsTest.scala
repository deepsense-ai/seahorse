/**
  * Copyright (c) 2016, CodiLime Inc.
  */
package io.deepsense.e2etests

import scala.concurrent.{Await, Future}

import org.scalatest.{FreeSpec, Matchers, WordSpec}
import scala.concurrent.ExecutionContext.Implicits.global

import io.deepsense.models.workflows.{Workflow, WorkflowInfo}


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
                runWorkflowSynchronously(workflow, id)
                client.deleteWorkflow(id)
              }
            }, workflowTimeout)
          }
        }
      }
    }
  }

  def uploadWorkflow(fileContents: String): Future[WorkflowInfo] = {
    for {
      id <- client.uploadWorkflow(fileContents)
      wflows <- client.fetchWorkflows()
      wflow = wflows.find(_.id == id)
    } yield {
      wflow.get
    }
  }
}
