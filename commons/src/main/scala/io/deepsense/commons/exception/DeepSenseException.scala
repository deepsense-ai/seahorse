/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.commons.exception

import java.util.UUID

/**
 * Base exception for all DeepSense exceptions
 */
abstract class DeepSenseException(
    val id: UUID,
    val code: Int,
    val title: String,
    val message: String,
    val cause: Option[Throwable],
    val details: Option[ExceptionDetails]) extends Exception(message, cause.orNull)

trait ExceptionDetails

case class StringExceptionDetails(details: String) extends ExceptionDetails
