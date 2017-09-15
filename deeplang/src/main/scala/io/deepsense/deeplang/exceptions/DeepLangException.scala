/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.deeplang.exceptions

abstract class DeepLangException(
    val message: String,
    val cause: Throwable = null)
  extends Exception(message, cause)
