/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.exception

import io.deepsense.commons.models

/**
 * Base exception for all DeepSense exceptions
 */
abstract class DeepSenseException(
  val id: DeepSenseException.Id,
  val code: Int,
  val title: String,
  val message: String,
  val cause: Option[Throwable],
  val details: Option[ExceptionDetails]) extends Exception(message, cause.orNull)

object DeepSenseException {
  type Id = models.Id
  val Id = models.Id
}

trait ExceptionDetails

case class StringExceptionDetails(details: String) extends ExceptionDetails
