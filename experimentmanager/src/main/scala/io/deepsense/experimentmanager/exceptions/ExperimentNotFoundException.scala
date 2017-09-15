/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.exceptions

import io.deepsense.commons.exception.FailureCode
import io.deepsense.models.experiments.Experiment

/**
 * Thrown when the specified experiment was not found.
 * @param experimentId Identifier of the requested experiment
 */
case class ExperimentNotFoundException(experimentId: Experiment.Id)
  extends ExperimentManagerException(
    FailureCode.ExperimentNotFound,
    "Experiment not found",
    s"Experiment with id $experimentId not found") {
  override protected def additionalDetails: Map[String, String] =
    Map("experimentId" -> experimentId.toString)
}
