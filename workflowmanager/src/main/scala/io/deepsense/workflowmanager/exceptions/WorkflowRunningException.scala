/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.exceptions

import io.deepsense.commons.exception.FailureCode
import io.deepsense.commons.models.Id

class WorkflowRunningException(workflowId: Id)
  extends WorkflowManagerException(
    FailureCode.CannotUpdateRunningWorkflow,
    "Workflow is running and can not be updated",
    s"Workflow with id $workflowId is running and can not be updated. " +
      "Wait for the completion or abort the workflow.") {
  override protected def additionalDetails: Map[String, String] =
    Map("workflowId" -> workflowId.toString)
}
