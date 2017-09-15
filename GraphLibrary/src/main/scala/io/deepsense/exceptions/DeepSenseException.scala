/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.exceptions

abstract class DeepSenseException (
    val message: String,
    val cause: Throwable = null)
  extends Exception(message, cause)
