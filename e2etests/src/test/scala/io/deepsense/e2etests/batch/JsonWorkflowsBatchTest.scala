/**
  * Copyright (c) 2016, CodiLime Inc.
  */
package io.deepsense.e2etests.batch

import org.scalatest.{Matchers, WordSpec}

import io.deepsense.e2etests.{TestClusters, TestWorkflowsIterator}

class JsonWorkflowsBatchTest
  extends WordSpec
    with Matchers
    with BatchTestInDockerSupport {

  import scala.concurrent.ExecutionContext.Implicits.global

  ensureSeahorseIsRunning()

  insertDatasourcesForTest()

  TestWorkflowsIterator.foreach { case TestWorkflowsIterator.Input(uri, fileContents) =>
    s"Workflow loaded from '$uri'" should {
      "should complete successfully in batch mode" when {
        for (cluster <- TestClusters.allAvailableClusters) {
          s"run on ${cluster.clusterType} cluster" in {
            val workflowIdFut = uploadWorkflow(fileContents).map(_.id)
            testWorkflowFromSeahorse(cluster, uri.toString, workflowIdFut)
          }
        }
      }
    }
  }
}
