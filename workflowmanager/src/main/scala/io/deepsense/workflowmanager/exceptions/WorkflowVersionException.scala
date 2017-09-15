/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.exceptions

import io.deepsense.commons.exception.FailureCode

class WorkflowVersionException(
    title: String,
    message: String,
    cause: Option[Throwable] = None,
    details: Map[String, String] = Map())
  extends WorkflowManagerException(FailureCode.IncorrectWorkflow, title, message, cause, details)
