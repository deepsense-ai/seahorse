/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.exceptions

import io.deepsense.commons.exception.FailureCode
import io.deepsense.commons.models.Id

class ExperimentNotRunningException(experimentId: Id)
  extends ExperimentManagerException(
    FailureCode.CannotUpdateRunningExperiment,
    "Experiment is not running and cannot be aborted",
    s"Experiment with id $experimentId is not running and can not be aborted.") {
  override protected def additionalDetails: Map[String, String] =
    Map("experimentId" -> experimentId.toString)
}

