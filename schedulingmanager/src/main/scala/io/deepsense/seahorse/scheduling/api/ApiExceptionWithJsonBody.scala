/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.api

import io.deepsense.seahorse.scheduling.model.{Error, JsonBodyForError}

class ApiExceptionWithJsonBody(val message: String, val errorCode: Int) extends ApiException {
  val body: String = JsonBodyForError(errorCode, message)
  val error: Error = Error(errorCode, message)
  override def getMessage: String = message
}
