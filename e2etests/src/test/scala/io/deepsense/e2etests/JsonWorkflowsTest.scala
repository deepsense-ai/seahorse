/**
  * Copyright (c) 2016, CodiLime Inc.
  */
package io.deepsense.e2etests

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

import org.scalatest.{Matchers, WordSpec}

import io.deepsense.commons.utils.OptionOpts._
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
                runAndCleanupWorkflow(workflow, cluster)
              }
            }, workflowTimeout)
          }
        }
      }
    }
  }

  private def uploadWorkflow(fileContents: String): Future[WorkflowInfo] = {
    for {
      id <- wmclient.uploadWorkflow(fileContents)
      workflows <- wmclient.fetchWorkflows()
      workflow <- workflows.find(_.id == id).asFuture
    } yield {
      workflow
    }
  }
}
