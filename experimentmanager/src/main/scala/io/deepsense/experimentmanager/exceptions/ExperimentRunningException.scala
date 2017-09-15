/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.exceptions

import io.deepsense.commons.exception.DeepSenseException
import io.deepsense.commons.models.Id

class ExperimentRunningException(experimentId: Id)
  extends ExperimentManagerException(
    DeepSenseException.Id.randomId,
    ErrorCodes.ExperimentRunning,
    "Experiment is running and can not be updated",
    s"Experiment with id $experimentId is running and can not be updated. " +
      "Wait for the completion or abort the experiment.", None, None)
