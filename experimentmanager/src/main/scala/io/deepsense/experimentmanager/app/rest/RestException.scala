/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.rest

import io.deepsense.experimentmanager.app.exceptions.ExperimentManagerException

case class RestException(code: Int, title: String, message: String, details: String)

object RestException {
  def fromException(exception: ExperimentManagerException): RestException = {
    RestException(exception.code, exception.title, exception.message, exception.details.toString)
  }
}
