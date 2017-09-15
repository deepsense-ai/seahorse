/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.exceptions

import io.deepsense.workflowmanager.rest.Version

case class WorkflowVersionNotSupportedException(
    workflowApiVersion: Version,
    supportedApiVersion: Version)
  extends WorkflowVersionException(
    s"API version ${workflowApiVersion.humanReadable} is not supported.",
    s"API version ${workflowApiVersion.humanReadable} is not supported. " +
    s"Currently supported version is ${supportedApiVersion.humanReadable}") {
  override protected def additionalDetails: Map[String, String] =
    Map("workflowApiVersion" -> workflowApiVersion.humanReadable,
        "supportedApiVersion" -> supportedApiVersion.humanReadable)
}
