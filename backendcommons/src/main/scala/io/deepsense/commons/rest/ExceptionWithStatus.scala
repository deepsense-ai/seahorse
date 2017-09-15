/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.rest

/**
 * Exception that will translate to an http error response with a specific
 * status code.
 */
case class ExceptionWithStatus(
    statusCode: Int,
    msg: String,
    cause: Throwable = null)
  extends Exception(msg, cause)
