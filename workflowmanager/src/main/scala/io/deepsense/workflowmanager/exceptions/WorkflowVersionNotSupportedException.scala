/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.exceptions

import io.deepsense.commons.exception.FailureCode

class WorkflowVersionNotSupportedException(
    workflowApiVersion: String,
    supportedApiVersion: String)
  extends WorkflowManagerException(
    FailureCode.IncorrectWorkflow,
    s"API version ${workflowApiVersion} is not supported.",
    s"API version ${workflowApiVersion} is not supported. " +
    s"Currently supported version is ${supportedApiVersion}") {
  override protected def additionalDetails: Map[String, String] =
    Map("workflowApiVersion" -> workflowApiVersion,
        "supportedApiVersion" -> supportedApiVersion)
}
