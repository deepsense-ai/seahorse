/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.exception

import io.deepsense.commons.exception.FailureCode.FailureCode

case class FailureDescription(
  id: DeepSenseFailure.Id,
  code: FailureCode,
  title: String,
  message: Option[String] = None,
  details: Map[String, String] = Map())

object FailureDescription {
  def stacktraceDetails(stackTrace: Array[StackTraceElement]): Map[String, String] = {
    import scala.compat.Platform.EOL
    Map("stacktrace" -> stackTrace.mkString("", EOL, EOL))
  }
}
