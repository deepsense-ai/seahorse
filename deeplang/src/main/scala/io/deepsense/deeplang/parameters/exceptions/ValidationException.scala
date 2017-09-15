/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters.exceptions

import io.deepsense.deeplang.exceptions.DeepLangException

/**
 * Base class for all Parameters Validation exceptions.
 */
abstract class ValidationException(message: String) extends DeepLangException(message)
