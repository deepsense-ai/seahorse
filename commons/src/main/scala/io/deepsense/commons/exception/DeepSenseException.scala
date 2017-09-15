/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.exception

import io.deepsense.commons.exception.FailureCode.FailureCode

/**
 * Base exception for all DeepSense exceptions
 */
abstract class DeepSenseException(
  val code: FailureCode,
  val title: String,
  val message: String,
  val cause: Option[Throwable] = None,
  val details: Map[String, String] = Map()) extends Exception(message, cause.orNull) {

  val id = DeepSenseFailure.Id.randomId

  def failureDescription: FailureDescription = FailureDescription(
    id,
    code,
    title,
    Some(message),
    details ++ additionalDetails)

  protected def additionalDetails: Map[String, String] = Map()
}
