/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.model

import org.json4s.DefaultFormats

object JsonBodyForError {

  import org.json4s.jackson.Serialization.write
  implicit val formats = DefaultFormats

  def apply(errorCode: Int, message: String): String = {
    val error = Error(
      code = errorCode,
      message = message
    )
    write(error)
  }

}
