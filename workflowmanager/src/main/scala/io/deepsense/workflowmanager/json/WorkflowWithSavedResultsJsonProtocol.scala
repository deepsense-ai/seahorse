/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.json

import io.deepsense.models.json.workflow.WorkflowWithResultsJsonProtocol
import io.deepsense.workflowmanager.model.{ExecutionReportWithId, WorkflowWithSavedResults}

trait WorkflowWithSavedResultsJsonProtocol extends WorkflowWithResultsJsonProtocol {

  implicit val executionReportWithIdFormat = jsonFormat5(ExecutionReportWithId.apply)

  implicit val workflowWithSavedResultsFormat =
    jsonFormat(WorkflowWithSavedResults.apply,
      "id", "metadata", "workflow", "thirdPartyData", "executionReport")

}
