/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.exceptions

import io.deepsense.commons.exception.{DeepSenseException, ExceptionDetails}

/**
 * Base exception for all exceptions Experiment Manager
 */
abstract class ExperimentManagerException(
    id: DeepSenseException.Id,
    code: Int,
    title: String,
    message: String,
    cause: Option[Throwable],
    details: Option[ExceptionDetails])
   extends DeepSenseException(id, code, title, message, cause, details)
