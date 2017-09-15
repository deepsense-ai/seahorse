/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters

import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang.parameters.exceptions._

class ParametersValidationSuite extends FunSuite with MockitoSugar {

  test("Validation of valid parameters is successful") {
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

  test("Validation of choice parameter should validate chosen schema") {
    val mockSchema = mock[ParametersSchema]
    val possibleChoices = Map("onlyChoice" -> mockSchema)
    val choice = ChoiceParameter("choice", None, true, possibleChoices)
    choice.value = Some("onlyChoice")
    choice.validate
    verify(mockSchema).validate
  }

  test("Validation of multipleChoice parameter should validate chosen schemas") {
    val mockSchema1 = mock[ParametersSchema]
    val mockSchema2 = mock[ParametersSchema]
    val possibleChoices = Map("firstChoice" -> mockSchema1, "secondChoice" -> mockSchema2)
    val multipleChoices = MultipleChoiceParameter("choice", None, true, possibleChoices)
    multipleChoices.value = Some(Traversable("firstChoice", "secondChoice"))
    multipleChoices.validate
    verify(mockSchema1).validate
    verify(mockSchema2).validate
  }

  test("Validation of parameters sequence should validate inner schemas") {
    val mockSchema = mock[ParametersSchema]
    val multiplicator = ParametersSequence("description", true, mockSchema)
    multiplicator.value = Some(Vector(mockSchema, mockSchema))
    multiplicator.validate
    verify(mockSchema, times(2)).validate
  }

  test("Validation of MultipleColumnSelection should validate inner selectors") {
    val selections = Vector.fill(3)({mock[ColumnSelection]})
    val selectorParameter = ColumnSelectorParameter("description", true)
    val multipleColumnSelection = MultipleColumnSelection(selections)
    selectorParameter.value = Some(multipleColumnSelection)
    selectorParameter.validate
    selections.foreach {
      case selection => verify(selection).validate
    }
  }

  test("Validation of IndexRangeColumnSelector should be valid only if lower <= upper bound") {
    IndexRangeColumnSelection(Some(5), Some(5)).validate
    IndexRangeColumnSelection(Some(5), Some(6)).validate

    intercept[IllegalIndexRangeColumnSelectionException]{
      IndexRangeColumnSelection(None, Some(5)).validate
    }
    intercept[IllegalIndexRangeColumnSelectionException]{
      IndexRangeColumnSelection(Some(4), None).validate
    }
    intercept[IllegalIndexRangeColumnSelectionException]{
      IndexRangeColumnSelection(None, None).validate
    }
    intercept[IllegalIndexRangeColumnSelectionException]{
      IndexRangeColumnSelection(Some(5), Some(4)).validate
    }
  }
}
