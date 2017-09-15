/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.e2etests

import org.scalatest.WordSpec

import io.deepsense.workflowexecutor.executor.WorkflowExecutor


class AllInputWorkflowsCorrectTest extends WordSpec {

  TestWorkflowsIterator.foreach { case TestWorkflowsIterator.Input(uri, file, fileContents) =>
    s"Workflow from '$uri'" should {
      "be correctly formatted" in {
        val workflow = WorkflowParser.parseWorkflow(fileContents)
        val datasources = WorkflowExecutor.datasourcesFrom(workflow)
      }
    }
  }
}
