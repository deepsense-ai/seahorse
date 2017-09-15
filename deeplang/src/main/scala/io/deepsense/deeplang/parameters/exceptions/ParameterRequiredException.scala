/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters.exceptions

import io.deepsense.deeplang.parameters.ParameterType.ParameterType

case class ParameterRequiredException(parameterType: ParameterType)
  extends ValidationException(s"Parameter value of type $parameterType was required " +
    "but was not provided.")
