/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.exceptions

import java.util.UUID

import io.deepsense.experimentmanager.app.models.Experiment

/**
 * Thrown when the specified experiment was not found.
 * @param experimentId Identifier of the requested experiment
 */
case class ExperimentNotFoundException(experimentId: Experiment.Id)
  extends ExperimentManagerException(
    UUID.randomUUID(),
    ErrorCodes.ExperimentNotFound,
    s"Experiment not found",
    s"Experiment with id $experimentId not found", null, None)
