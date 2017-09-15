/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.exceptions

import io.deepsense.commons.exception.FailureCode
import io.deepsense.models.workflows.Workflow

/**
 * Thrown when the specified workflow was not found.
 * @param workflowId Identifier of the requested workflow
 */
case class WorkflowNotFoundException(workflowId: Workflow.Id)
  extends WorkflowManagerException(
    FailureCode.WorkflowNotFound,
    "Workflow not found",
    s"Workflow with id $workflowId not found") {
  override protected def additionalDetails: Map[String, String] =
    Map("workflowId" -> workflowId.toString)
}
