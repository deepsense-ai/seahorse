/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy.exceptions

/**
 * Base class for all DHierarchy exceptions.
 * TODO: Decide if this is right place for this exception.
 */
abstract class DHierarchyException(
    val message: String,
    val cause: Throwable = null)
  extends Exception(message, cause)
