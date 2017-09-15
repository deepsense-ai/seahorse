/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.exceptions

import java.util.UUID

import io.deepsense.commons.models.Id

class ExperimentRunningException(experimentId: Id)
  extends ExperimentManagerException(
    UUID.randomUUID(),
    ErrorCodes.ExperimentRunning,
    "Experiment is running and can not be updated",
    s"Experiment with id $experimentId is running and can not be updated. " +
      "Wait for the completion or abort the experiment.", None, None)
