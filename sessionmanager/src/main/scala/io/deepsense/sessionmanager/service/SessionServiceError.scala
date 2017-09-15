/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

abstract class SessionServiceError(message: String, throwable: Throwable)
  extends Exception(message, throwable) {

  def this(message: String) = {
    this(message, null)
  }
}

case class UnauthorizedOperationException(message: String)
  extends SessionServiceError(message = s"Unauthorized operation: $message")
