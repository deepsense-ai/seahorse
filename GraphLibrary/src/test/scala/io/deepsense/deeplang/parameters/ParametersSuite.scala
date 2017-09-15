/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import org.scalatest.FunSuite

import io.deepsense.deeplang.parameters.exceptions.TypeConversionException

class ParametersSuite extends FunSuite {
  test("Getting Boolean Parameter") {
    val holder = BooleanParameterHolder("example", true, true)
    holder.value = Some(BooleanParameter(true))
    val parametersSchema = ParametersSchema("x" -> holder)
    assert(parametersSchema.getBooleanParameter("x").get.value == true)
  }

  test("Getting Numeric Parameter") {
    val holder = NumericParameterHolder("example", 3.1, true, RangeValidator(3, 4))
    holder.value = Some(NumericParameter(3.2))
    val parametersSchema = ParametersSchema("x" -> holder)
    assert(parametersSchema.getNumericParameter("x").get.value == 3.2)
  }

  test("Getting String Parameter") {
    val holder = StringParameterHolder("example", "default", true)
    holder.value = Some(StringParameter("abc"))
    val parametersSchema = ParametersSchema("x" -> holder)
    assert(parametersSchema.getStringParameter("x").get.value == "abc")
  }

  test("Getting wrong type of parameter should throw an exception") {
    val parameter = Some(StringParameter("abc"))
    val expectedTargetTypeName = "io.deepsense.deeplang.parameters.NumericParameter"
    val exception = intercept[TypeConversionException] {
      val holder = StringParameterHolder("description", None, true)
      holder.value = parameter
      val parametersSchema = ParametersSchema("x" -> holder)
      parametersSchema.getNumericParameter("x")
    }
    assert(exception == TypeConversionException(parameter.get, expectedTargetTypeName))
  }
}
