/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.exceptions

import io.deepsense.commons.exception.FailureCode
import io.deepsense.commons.models.Id

class WorkflowNotRunningException(workflowId: Id)
  extends WorkflowManagerException(
    FailureCode.CannotUpdateRunningWorkflow,
    "Workflow is not running and cannot be aborted",
    s"Workflow with id $workflowId is not running and can not be aborted.") {
  override protected def additionalDetails: Map[String, String] =
    Map("workflowId" -> workflowId.toString)
}

