/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.exceptions

import io.deepsense.workflowmanager.rest.Version

case class WorkflowVersionNotFoundException(
    supportedApiVersion: Version)
  extends WorkflowVersionException(
  "API version was not included in the request.",
  "API version was not included in the request." +
    s"Currently supported version is ${supportedApiVersion.humanReadable}") {
  override protected def additionalDetails: Map[String, String] =
    Map("supportedApiVersion" -> supportedApiVersion.humanReadable)
}
