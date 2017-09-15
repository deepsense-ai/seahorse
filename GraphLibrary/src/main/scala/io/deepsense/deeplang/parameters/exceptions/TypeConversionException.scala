/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters.exceptions

import io.deepsense.deeplang.parameters.Parameter

case class TypeConversionException(parameter: Parameter, targetTypeName: String)
  extends ValidationException(s"Cannot convert $parameter to $targetTypeName.")
