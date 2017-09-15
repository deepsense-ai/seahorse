/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import org.scalatest.FunSuite

import io.deepsense.deeplang.parameters.exceptions._

class ParametersValidationSuite extends FunSuite {
  test("Validation of valid parameters is possible") {
    val holder1 = NumericParameterHolder("example1", None, true, RangeValidator(3, 4))
    val holder2 = StringParameterHolder("example2", "default", true)

    holder1.value = Some(NumericParameter(3.5))
    holder2.value = Some(StringParameter("abc"))

    val parametersSchema = ParametersSchema("x" -> holder1, "y" -> holder2)
    parametersSchema.validate
  }

  test("Validation of invalid parameter using range validator should throw an exception") {
    val exception = intercept[OutOfRangeException] {
      val holder = NumericParameterHolder("description", None, true, RangeValidator(3, 4))
      holder.value = Some(NumericParameter(4.1))

      val parametersSchema = ParametersSchema("x" -> holder)
      parametersSchema.validate
    }
    assert(exception == OutOfRangeException(4.1, 3, 4))
  }

  test("Missing required parameter should throw an exception") {
    val exception = intercept[ParameterRequiredException] {
      val holder = StringParameterHolder("description", None, true)
      val parametersSchema = ParametersSchema("x" -> holder)
      parametersSchema.validate
    }
    assert(exception == ParameterRequiredException(ParameterType.String))
  }
}
