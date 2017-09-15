/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.exceptions

import java.util.UUID

import io.deepsense.commons.models.Id
import io.deepsense.models.experiments.Experiment

/**
 * Thrown when the specified experiment was not found.
 * @param experimentId Identifier of the requested experiment
 */
case class ExperimentNotFoundException(experimentId: Id)
  extends ExperimentManagerException(
    UUID.randomUUID(),
    ErrorCodes.ExperimentNotFound,
    "Experiment not found",
    s"Experiment with id $experimentId not found", None, None)
