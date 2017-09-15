/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.exceptions

import io.deepsense.commons.exception.DeepSenseException
import io.deepsense.commons.models.Id

/**
 * Thrown when the specified experiment was not found.
 * @param experimentId Identifier of the requested experiment
 */
case class ExperimentNotFoundException(experimentId: Id)
  extends ExperimentManagerException(
    DeepSenseException.Id.randomId,
    ErrorCodes.ExperimentNotFound,
    "Experiment not found",
    s"Experiment with id $experimentId not found", None, None)
