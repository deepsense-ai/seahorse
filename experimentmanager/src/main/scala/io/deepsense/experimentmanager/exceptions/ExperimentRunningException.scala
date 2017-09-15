/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.exceptions

import java.util.UUID

import io.deepsense.commons.models.Id
import io.deepsense.experimentmanager.models.Experiment

class ExperimentRunningException(experimentId: Id)
  extends ExperimentManagerException(
    UUID.randomUUID(),
    ErrorCodes.ExperimentRunning,
    s"Experiment is running and can not be updated",
    s"Experiment with id $experimentId is running and can not be updated", null, None)
