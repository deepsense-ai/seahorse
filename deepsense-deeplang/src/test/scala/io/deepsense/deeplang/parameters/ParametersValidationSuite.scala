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

  /** Parameter that always throws ValidationException, regardless of held value. */
  case class MockParameter(
      description: String,
      default: Option[Nothing],
      required: Boolean)
    extends Parameter {
    type HeldValue = Null
    val parameterType = null

    def value = Some(null)

    def replicate = copy()

    override def validateDefined(any: Null): Unit = {
      throw new ValidationException("Mock exception") {}
    }
  }

  test("Validation of valid parameters is possible") {
    val param1 = NumericParameter("example1", None, true, RangeValidator(3, 4))
    val param2 = StringParameter("example2", Some("default"), true, RegexValidator("abc".r))

    param1.value = Some(3.5)
    param2.value = Some("abc")

    val parametersSchema = ParametersSchema("x" -> param1, "y" -> param2)
    parametersSchema.validate
  }

  test("Validation of invalid parameter using range validator should throw an exception") {
    val exception = intercept[OutOfRangeException] {
      val param = NumericParameter("description", None, true, RangeValidator(3, 4))
      param.value = Some(4.1)

      val parametersSchema = ParametersSchema("x" -> param)
      parametersSchema.validate
    }
    assert(exception == OutOfRangeException(4.1, 3, 4))
  }

  test("Validation of invalid parameter using range with step should throw an exception") {
    val exception = intercept[OutOfRangeWithStepException] {
      val validator = RangeValidator(3, 5.4, step = Some(1.2))
      val param = NumericParameter("description", None, true, validator)
      param.value = Some(4.1)

      val parametersSchema = ParametersSchema("x" -> param)
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
      val param = StringParameter("description", None, true, RegexValidator("a".r))
      val parametersSchema = ParametersSchema("x" -> param)
      parametersSchema.validate
    }
    assert(exception == ParameterRequiredException(ParameterType.String))
  }

  test("Validation of invalid parameter using regex validator should throw an exception") {
    val regex = "a".r
    val exception = intercept[MatchException] {
      val validator = RegexValidator(regex)
      val param = StringParameter("description", None, true, validator)
      param.value = Some("abc")

      val parametersSchema = ParametersSchema("x" -> param)
      parametersSchema.validate
    }
    assert(exception == MatchException("abc", regex))
  }

  test("Choosing nonexistent choice in single choice parameter should throw an exception") {
    intercept[IllegalChoiceException] {
      val possibleChoices = Map.empty[String, ParametersSchema]
      val choice = ChoiceParameter("choice", None, true, possibleChoices)
      choice.fill("nonexistent", x => { })
    }
  }

  test("Choosing nonexistent choice in multiple choice parameter should throw an exception") {
    intercept[IllegalChoiceException] {
      val possibleChoices = Map.empty[String, ParametersSchema]
      val choice = MultipleChoiceParameter("choice", None, true, possibleChoices)
      choice.fill(Map("nonexistent" -> (x => { })))
    }
  }

  test("Validation of choice parameter with invalid value should throw an exception") {
    intercept[ValidationException] {
      val param = MockParameter("example", None, true)
      val choiceSchema = ParametersSchema("x" -> param)
      val possibleChoices = Map("onlyChoice" -> choiceSchema)

      val choice = ChoiceParameter("choice", None, true, possibleChoices)
      choice.fill("onlyChoice", _ => { })
      choice.validate
    }
  }

  test("Validation of multipleChoice parameter with invalid parameter should throw an exception") {
    intercept[ValidationException] {
      val param = MockParameter("example", None, true)
      val choiceSchema = ParametersSchema("x" -> param)
      val possibleChoices = Map("onlyChoice" -> choiceSchema)

      val multipleChoices = MultipleChoiceParameter("choice", None, true, possibleChoices)
      multipleChoices.fill(Map("onlyChoice" -> (x => { })))
      multipleChoices.validate
    }
  }

  test("Validation of multiplier parameter with invalid parameter should throw an exception") {
    intercept[ValidationException] {
      val param = MockParameter("example", None, true)
      val schema = ParametersSchema("x" -> param)
      val multiplicator = MultiplierParameter("description", None, true, schema)

      multiplicator.fill(List(x => { }, x => { }))
      multiplicator.validate
    }
  }
}
