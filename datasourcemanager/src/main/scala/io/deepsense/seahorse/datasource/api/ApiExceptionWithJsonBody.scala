/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.api

import io.deepsense.seahorse.datasource.model.{Error, JsonBodyForError}

abstract class ApiExceptionWithJsonBody(val message: String, val errorCode: Int) extends ApiException {
  val body: String = JsonBodyForError(errorCode, message)
  val error: Error = Error(errorCode, message)
  override def getMessage: String = message
}
