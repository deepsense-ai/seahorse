/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

case class BooleanParameter(value: Boolean) extends Parameter

case class NumericParameter(value: Double) extends Parameter

case class StringParameter(value: String) extends Parameter
