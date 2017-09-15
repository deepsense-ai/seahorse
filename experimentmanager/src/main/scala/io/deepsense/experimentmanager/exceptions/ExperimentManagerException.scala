/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.exceptions

import java.util.UUID

/**
 * Base exception for all exceptions Experiment Manager
 */
abstract class ExperimentManagerException(
    val id: UUID,
    val code: Int,
    val title: String,
    val message: String,
    val cause: Throwable,
    val details: Option[ExceptionDetails]) extends Exception(message, cause)

trait ExceptionDetails
