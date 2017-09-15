/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.exceptions

import io.deepsense.commons.exception.FailureCode
import io.deepsense.models.workflows.Workflow

case class WorkflowOwnerMismatchException(workflowId: Workflow.Id)
  extends WorkflowManagerException(
    FailureCode.IllegalArgumentException,
    "Workflow owner mismatch",
    s"Cannot modify workflow with id $workflowId, as it belongs to another user.")
