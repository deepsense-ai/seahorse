/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.exception

case class FailureDescription(code: Int, title: String, message: String, details: String)

object FailureDescription {
  def fromException(exception: DeepSenseException): FailureDescription = FailureDescription(
    exception.code,
    exception.title,
    exception.message,
    exception.details.toString)
}
