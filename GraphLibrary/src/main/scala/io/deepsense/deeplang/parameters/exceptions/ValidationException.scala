/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters.exceptions

/**
 * Base class for all Parameters Validation exceptions.
 * TODO: Exceptions hierarchy for validation exceptions.
 */
abstract class ValidationException(
    val message: String,
    val cause: Throwable = null)
  extends Exception(message, cause)
