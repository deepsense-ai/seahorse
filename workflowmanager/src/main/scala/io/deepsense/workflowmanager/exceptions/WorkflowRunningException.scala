/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.exceptions

import io.deepsense.commons.exception.FailureCode
import io.deepsense.commons.models.Id

class WorkflowRunningException(experimentId: Id)
  extends WorkflowManagerException(
    FailureCode.CannotUpdateRunningExperiment,
    "Experiment is running and can not be updated",
    s"Experiment with id $experimentId is running and can not be updated. " +
      "Wait for the completion or abort the experiment.") {
  override protected def additionalDetails: Map[String, String] =
    Map("experimentId" -> experimentId.toString)
}
