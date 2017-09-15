/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.e2etests

import org.scalatest.WordSpec

import io.deepsense.deeplang.CatalogRecorder
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.workflowexecutor.executor.WorkflowExecutor


class AllInputWorkflowsCorrectTest extends WordSpec {

  val graphReader = {
    val operationsCatalog = CatalogRecorder.resourcesCatalogRecorder.catalogs.dOperationsCatalog
    new GraphReader(operationsCatalog)
  }

  TestWorkflowsIterator.foreach { case TestWorkflowsIterator.Input(uri, file, fileContents) =>
    s"Workflow from '$uri'" should {
      "be correctly formatted" in {
        val workflow = new WorkflowJsonConverter(graphReader).parseWorkflow(fileContents)
        val datasources = WorkflowExecutor.datasourcesFrom(workflow)
      }
    }
  }
}
