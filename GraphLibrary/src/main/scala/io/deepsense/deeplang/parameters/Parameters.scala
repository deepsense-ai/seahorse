/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

case class BooleanParameter(value: Boolean) extends Parameter

case class NumericParameter(value: Double) extends Parameter

case class StringParameter(value: String) extends Parameter

case class ChoiceParameter(label: String, value: ParametersSchema) extends Parameter

case class MultipleChoiceParameter(values: Set[ChoiceParameter]) extends Parameter

case class MultiplicatorParameter(value: List[ParametersSchema]) extends Parameter
