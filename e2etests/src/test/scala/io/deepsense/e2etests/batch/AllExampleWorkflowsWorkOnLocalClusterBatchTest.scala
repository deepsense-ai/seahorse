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

  import scala.concurrent.ExecutionContext.Implicits.global

  ensureSeahorseIsRunning()

  val exampleWorkflowsFut = fetchExampleWorkflows()
  Await.result(
    exampleWorkflowsFut.map(exampleWorkflows =>
      for {
        exampleWorkflowInfo <- exampleWorkflows
      } testExampleOnLocalCluster(exampleWorkflowInfo)
    ), httpTimeout)

  private def testExampleOnLocalCluster(workflowInfo: WorkflowInfo): Unit = {
    s"Example workflow '${workflowInfo.name}'" should {
      "run successfully in batch mode on local cluster" in {
        val workflowIdFut = Future.successful(workflowInfo.id)
        testWorkflowFromSeahorse(TestClusters.local(), workflowInfo.name, workflowIdFut)
      }
    }
  }
}
