/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters.exceptions

case class NoSuchParameterException(parameterName: String)
  extends ValidationException(s"Parameter not found: $parameterName.")
