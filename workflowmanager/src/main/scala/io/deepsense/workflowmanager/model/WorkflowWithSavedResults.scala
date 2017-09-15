/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.model

import io.deepsense.graph.Graph
import io.deepsense.models.workflows.{ThirdPartyData, Workflow, WorkflowMetadata, WorkflowWithResults}

case class WorkflowWithSavedResults(
  id: Workflow.Id,
  metadata: WorkflowMetadata,
  graph: Graph,
  thirdPartyData: ThirdPartyData,
  executionReport: ExecutionReportWithId)

object WorkflowWithSavedResults {
  def apply(
      resultsId: ExecutionReportWithId.Id,
      workflowWithResults: WorkflowWithResults): WorkflowWithSavedResults = {
    new WorkflowWithSavedResults(
      workflowWithResults.id,
      workflowWithResults.metadata,
      workflowWithResults.graph,
      workflowWithResults.thirdPartyData,
      ExecutionReportWithId(resultsId, workflowWithResults.executionReport))
  }
}
