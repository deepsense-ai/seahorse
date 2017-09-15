/**
 * Copyright (c) 2016, CodiLime Inc.
 */
package io.deepsense.e2etests.batch

import scala.concurrent.{Await, Future}

import org.scalatest.{Matchers, WordSpec}

import io.deepsense.e2etests.{ExampleWorkflowsFetcher, TestClusters}
import io.deepsense.models.workflows.WorkflowInfo

class AllExampleWorkflowsWorkOnLocalClusterBatchTest
  extends WordSpec
  with Matchers
  with ExampleWorkflowsFetcher
  with BatchTestInDockerSupport {

  ensureSeahorseIsRunning()

  val exampleWorkflows = Await.result(fetchExampleWorkflows(), httpTimeout)

  for {
    workflowInfo <- exampleWorkflows
  } {
    s"Example workflow '${workflowInfo.name}'" should {
      "run successfully in batch mode on local cluster" in {
        val fileName = s"${workflowInfo.name}.json"
        val testFuture = testWorkflowFromSeahorse(TestClusters.local(), fileName, workflowInfo.id)
        Await.result(testFuture, workflowTimeout)
      }
    }
  }
}
