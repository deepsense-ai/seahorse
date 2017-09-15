/**
  * Copyright (c) 2016, CodiLime Inc.
  */

package io.deepsense.e2etests

import scala.concurrent.Future

import io.deepsense.models.workflows.WorkflowInfo

trait ExampleWorkflowsFetcher {
  self: SeahorseIntegrationTestDSL =>

  import scala.concurrent.ExecutionContext.Implicits.global

  def fetchExampleWorkflows(): Future[Seq[WorkflowInfo]] = {
    val workflowsFut = wmclient.fetchWorkflows()
    val exampleWorkflows = workflowsFut.map(_.filter(_.ownerName == "seahorse"))
    // Order matters because of
    // - `Write Transformer` in Example 1
    // - `Read Transformer` in Example 4
    exampleWorkflows.map(_.sortBy(_.name))
  }
}
