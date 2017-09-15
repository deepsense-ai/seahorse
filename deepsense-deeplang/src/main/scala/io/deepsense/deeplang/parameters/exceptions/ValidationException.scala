/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters.exceptions

import io.deepsense.deeplang.exceptions.DeepLangException

/**
 * Base class for all Parameters Validation exceptions.
 */
abstract class ValidationException(message: String) extends DeepLangException(message)
