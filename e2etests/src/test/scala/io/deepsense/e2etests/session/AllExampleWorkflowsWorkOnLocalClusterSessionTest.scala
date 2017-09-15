/**
  * Copyright (c) 2016, CodiLime Inc.
  */

package io.deepsense.e2etests.session

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

import org.scalatest._

import io.deepsense.commons.utils.OptionOpts._
import io.deepsense.e2etests.{ExampleWorkflowsFetcher, SeahorseIntegrationTestDSL, TestClusters}
import io.deepsense.models.workflows.WorkflowInfo
import io.deepsense.workflowmanager.model.WorkflowDescription


class AllExampleWorkflowsWorkOnLocalClusterSessionTest
  extends FreeSpec
  with Matchers
  with SeahorseIntegrationTestDSL
  with ExampleWorkflowsFetcher {

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
            Await.result(for {
              workflow <- cloneWorkflow(exampleWorkflow)
              _ <- runAndCleanupWorkflow(workflow, TestClusters.local())
            } yield (), workflowTimeout)
          }
        }), httpTimeout)
  }

  def cloneWorkflow(workflow: WorkflowInfo): Future[WorkflowInfo] = {
    val cloneDescription = WorkflowDescription(
      name = s"CLONE - ${workflow.name}",
      description = workflow.description
    )
    val clonedWorkflowIdFut = wmclient.cloneWorkflow(workflow.id, cloneDescription)

    for {
      id <- clonedWorkflowIdFut
      workflows <- wmclient.fetchWorkflows()
      workflow <- workflows.find(_.id == id).asFuture
    } yield {
      workflow
    }
  }

}
