/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import org.scalatest.FunSuite

import io.deepsense.deeplang.parameters.exceptions.ValidationException
import io.deepsense.deeplang.parameters.exceptions._

class ParametersValidationSuite extends FunSuite {
  case class MockParameterHolder(
      description: String,
      default: Option[Nothing],
      required: Boolean)
    extends ParameterHolder {
    type HeldParameter = Nothing
    val parameterType = null
    override def validate: Unit = {
      throw new ValidationException("Mock exception") {}
    }
  }

  test("Validation of valid parameters is possible") {
    val holder1 = NumericParameterHolder("example1", None, true, RangeValidator(3, 4))
    val holder2 = StringParameterHolder("example2", "default", true, RegexValidator("abc".r))

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

  test("Validation of invalid parameter using range with step should throw an exception") {
    val exception = intercept[OutOfRangeWithStepException] {
      val validator = RangeValidator(3, 5.4, step = Some(1.2))
      val holder = NumericParameterHolder("description", None, true, validator)
      holder.value = Some(NumericParameter(4.1))

      val parametersSchema = ParametersSchema("x" -> holder)
      parametersSchema.validate
    }
    assert(exception == OutOfRangeWithStepException(4.1, 3, 5.4, 1.2))
  }

  test("Creating invalid range validator should throw an exception") {
    intercept[IllegalArgumentException] {
      RangeValidator(3, 2)
    }
  }

  test("Creating invalid range with step validator should throw an exception") {
    intercept[IllegalArgumentException] {
      RangeValidator(3, 5.5, step = Some(1.2))
    }
  }

  test("Creating invalid range with step less than zero should throw an exception") {
    intercept[IllegalArgumentException] {
      RangeValidator(3, 5.4, step = Some(-1.2))
    }
  }

  test("Missing required parameter should throw an exception") {
    val exception = intercept[ParameterRequiredException] {
      val holder = StringParameterHolder("description", None, true, RegexValidator("a".r))
      val parametersSchema = ParametersSchema("x" -> holder)
      parametersSchema.validate
    }
    assert(exception == ParameterRequiredException(ParameterType.String))
  }

  test("Validation of invalid parameter using regex validator should throw an exception") {
    val regex = "a".r
    val exception = intercept[MatchException] {
      val validator = RegexValidator(regex)
      val holder = StringParameterHolder("description", None, true, validator)
      holder.value = Some(StringParameter("abc"))

      val parametersSchema = ParametersSchema("x" -> holder)
      parametersSchema.validate
    }
    assert(exception == MatchException("abc", regex))
  }

  test("Validation of choice parameter with invalid parameter should throw an exception") {
    intercept[ValidationException] {
      val holder = MockParameterHolder("example", None, true)
      val choiceSchema = ParametersSchema("x" -> holder)
      val possibleChoices = Map("onlyChoice" -> choiceSchema)

      val choice = ChoiceParameterHolder("choice", None, true, possibleChoices)
      choice.value = Some(ChoiceParameter("onlyChoice", choice.options("onlyChoice")))
      choice.validate
    }
  }

  test("Choosing nonexistent choice should throw an exception") {
    intercept[IllegalChoiceException] {
      val possibleChoices = Map.empty[String, ParametersSchema]
      val choice = ChoiceParameterHolder("choice", None, true, possibleChoices)
      choice.value = Some(ChoiceParameter("nonexistent", ParametersSchema()))
      choice.validate
    }
  }

  test("Validation of multipleChoice parameter with invalid parameter should throw an exception") {
    intercept[ValidationException] {
      val holder = MockParameterHolder("example", None, true)
      val choiceSchema = ParametersSchema("x" -> holder)
      val possibleChoices = Map("onlyChoice" -> choiceSchema)

      val multipleChoices = MultipleChoiceParameterHolder("choice", None, true, possibleChoices)
      val choices = Set(ChoiceParameter("onlyChoice", multipleChoices.options("onlyChoice")))
      multipleChoices.value = Some(MultipleChoiceParameter(choices))
      multipleChoices.validate
    }
  }
}
