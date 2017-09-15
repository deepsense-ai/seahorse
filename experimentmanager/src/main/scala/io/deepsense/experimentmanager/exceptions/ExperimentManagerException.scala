/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.exceptions

import java.util.UUID

import io.deepsense.commons.exception.{DeepSenseException, ExceptionDetails}

/**
 * Base exception for all exceptions Experiment Manager
 */
abstract class ExperimentManagerException(
    id: UUID,
    code: Int,
    title: String,
    message: String,
    cause: Option[Throwable],
    details: Option[ExceptionDetails])
   extends DeepSenseException(id, code, title, message, cause, details)
