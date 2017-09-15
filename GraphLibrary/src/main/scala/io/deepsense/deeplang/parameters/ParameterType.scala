/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

object ParameterType extends Enumeration {
  type ParameterType = Value
  val Numeric = Value("numeric")
  val String = Value("string")
}
