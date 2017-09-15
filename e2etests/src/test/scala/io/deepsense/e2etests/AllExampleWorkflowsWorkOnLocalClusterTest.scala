/**
  * Copyright (c) 2016, CodiLime Inc.
  */

package io.deepsense.e2etests

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

import org.scalatest._

import io.deepsense.commons.utils.OptionOpts._
import io.deepsense.models.workflows.WorkflowInfo
import io.deepsense.workflowmanager.model.WorkflowDescription


class AllExampleWorkflowsWorkOnLocalClusterTest extends FreeSpec with Matchers with SeahorseIntegrationTestDSL {

  info("Assuming application will be accessible under localhost:33321")

  "All example workflows should be correct - all nodes run and completed successfully on a local cluster" - {
    ensureSeahorseIsRunning()
    val exampleWorkflowsFut = fetchExampleWorkflows()
    Await.result(
      exampleWorkflowsFut.map(exampleWorkflows =>
        for {
          exampleWorkflow <- exampleWorkflows
        } {
          s"Workflow '${exampleWorkflow.name}'" in {
            Await.result({
              val workflowFut = cloneWorkflow(exampleWorkflow)
              workflowFut.flatMap { workflow =>
                runAndCleanupWorkflow(workflow, TestClusters.local())
              }
            }, workflowTimeout)
          }
        }), httpTimeout)
  }

  def cloneWorkflow(wflow: WorkflowInfo): Future[WorkflowInfo] = {
    val cloneDescription = WorkflowDescription(
      name = s"CLONE - ${wflow.name}",
      description = wflow.description
    )
    val clonedWorkflowIdFut = wmclient.cloneWorkflow(wflow.id, cloneDescription)

    for {
      id <- clonedWorkflowIdFut
      workflows <- wmclient.fetchWorkflows()
      workflow <- workflows.find(_.id == id).asFuture
    } yield {
      workflow
    }
  }

  def fetchExampleWorkflows(): Future[Seq[WorkflowInfo]] = {
    val workflowsFut = wmclient.fetchWorkflows()
    val exampleWorkflows = workflowsFut.map(_.filter(_.ownerName == "seahorse"))
    // Order matters because of
    // - `Write Transformer` in Example 1
    // - `Read Transformer` in Example 4
    exampleWorkflows.map(_.sortBy(_.name))
  }

}
