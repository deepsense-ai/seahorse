/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.exceptions

import io.deepsense.commons.exception.FailureCode
import io.deepsense.models.workflows.Workflow

/**
 * Thrown when the specified experiment was not found.
 * @param experimentId Identifier of the requested experiment
 */
case class WorkflowNotFoundException(experimentId: Workflow.Id)
  extends WorkflowManagerException(
    FailureCode.ExperimentNotFound,
    "Experiment not found",
    s"Experiment with id $experimentId not found") {
  override protected def additionalDetails: Map[String, String] =
    Map("experimentId" -> experimentId.toString)
}
