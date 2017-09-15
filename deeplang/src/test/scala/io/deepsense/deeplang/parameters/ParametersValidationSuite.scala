/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang.parameters

import scala.collection.immutable.ListMap

import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang.parameters.exceptions._

class ParametersValidationSuite extends FunSuite with MockitoSugar {

  test("Validation of valid parameters is successful") {
    val param1 = NumericParameter("example1", None, RangeValidator(3, 4))
    val param2 = StringParameter("example2", Some("default"), RegexValidator("abc".r))

    param1.value = 3.5
    param2.value = "abc"

    val parametersSchema = ParametersSchema("x" -> param1, "y" -> param2)
    assert(parametersSchema.validateParams.isEmpty)
  }

  test("Validation of invalid parameter using range validator should return an exception") {
    val param = NumericParameter("description", None, RangeValidator(3, 4))
    param.value = 4.1
    val parametersSchema = ParametersSchema("x" -> param)
    assertValidationReturnedException(parametersSchema, OutOfRangeException(4.1, 3, 4))
  }

  test("Validation of invalid parameters should return exception for each of them") {
    val paramOne = mock[Parameter]
    val paramOneValidationOneException = OutOfRangeException(4.1, 3, 4)
    when(paramOne.validate("paramOne")).thenReturn(Vector(paramOneValidationOneException))

    val paramTwo = mock[Parameter]
    val paramTwoValidationOneException = MatchException("abc", "a".r)
    when(paramTwo.validate("paramTwo")).thenReturn(Vector(paramTwoValidationOneException))

    val parametersSchema = ParametersSchema("paramOne" -> paramOne, "paramTwo" -> paramTwo)
    assertValidationReturnedException(
      parametersSchema,
      paramOneValidationOneException, paramTwoValidationOneException)
  }

  test("Validation of invalid parameter using range with step should return an exception") {
    val validator = RangeValidator(3, 5.4, step = Some(1.2))
    val param = NumericParameter("description", None, validator)
    param.value = Some(4.1)

    val parametersSchema = ParametersSchema("x" -> param)
    assertValidationReturnedException(parametersSchema,
      OutOfRangeWithStepException(4.1, 3, 5.4, 1.2))
  }

  test("Creating invalid range validator should return an exception") {
    intercept[IllegalArgumentException] {
      RangeValidator(3, 2)
    }
  }

  test("Creating invalid range with step validator should return an exception") {
    intercept[IllegalArgumentException] {
      RangeValidator(3, 5.5, step = Some(1.2))
    }
  }

  test("Creating invalid range with step less than zero return throw an exception") {
    intercept[IllegalArgumentException] {
      RangeValidator(3, 5.4, step = Some(-1.2))
    }
  }

  test("Validating empty parameter without default value should return an exception") {
    val param = StringParameter("description", None, RegexValidator("a".r))
    val parametersSchema = ParametersSchema("x" -> param)
    parametersSchema.validateParams
    assertValidationReturnedException(parametersSchema, ParameterRequiredException("x"))
  }

  test("Validation empty parameter with default value should not throw an exception") {
    val default = "someDefault"
    val param = StringParameter("description", Some(default), new AcceptAllRegexValidator)
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.validateParams.isEmpty)
    assert(param.value == default)
  }

  test("Validation of invalid parameter using regex validator should return an exception") {
    val regex = "a".r
    val validator = RegexValidator(regex)
    val param = StringParameter("description", None, validator)
    param.value = "abc"

    val parametersSchema = ParametersSchema("x" -> param)
    assertValidationReturnedException(parametersSchema, MatchException("abc", regex))
  }

  test("Validation of choice parameter should validate chosen schema") {
    val mockSchema = mock[ParametersSchema]
    val possibleChoices = ListMap("onlyChoice" -> mockSchema)
    val choice = ChoiceParameter("choice", None, possibleChoices)
    choice.value = "onlyChoice"
    choice.validate("choice")
    verify(mockSchema).validateParams
  }

  test("Validation of multipleChoice parameter should validate chosen schemas") {
    val mockSchema1 = mock[ParametersSchema]
    when(mockSchema1.validateParams).thenReturn(Vector())
    val mockSchema2 = mock[ParametersSchema]
    when(mockSchema2.validateParams).thenReturn(Vector())
    val possibleChoices = ListMap("firstChoice" -> mockSchema1, "secondChoice" -> mockSchema2)
    val multipleChoices = MultipleChoiceParameter("choice", None, possibleChoices)
    multipleChoices.value = Some(Traversable("firstChoice", "secondChoice"))
    multipleChoices.validate("choice")
    verify(mockSchema1).validateParams
    verify(mockSchema2).validateParams
  }

  test("Validation of parameters sequence should validate inner schemas") {
    val mockSchema = mock[ParametersSchema]
    when(mockSchema.validateParams).thenReturn(Vector())
    val multiplicator = ParametersSequence("description", mockSchema)
    multiplicator.value = Vector(mockSchema, mockSchema)
    multiplicator.validate("someParamName")
    verify(mockSchema, times(2)).validateParams
  }

  test("Validation of MultipleColumnSelection should validate inner selectors") {
    val selections = Vector.fill(3)({mock[ColumnSelection]})
    selections.foreach(selection => when(selection.validate).thenReturn(Vector()))
    val selectorParameter = ColumnSelectorParameter("description", 0)
    val multipleColumnSelection = MultipleColumnSelection(selections, false)
    selectorParameter.value = multipleColumnSelection
    selectorParameter.validate("someParamName")
    selections.foreach {
      case selection => verify(selection).validate
    }
  }


  test("Validation of IndexRangeColumnSelector should be valid only if lower <= upper bound") {
    assert(IndexRangeColumnSelection(Some(5), Some(5)).validate.isEmpty)
    assert(IndexRangeColumnSelection(Some(5), Some(6)).validate.isEmpty)

    def assertColumnSelectionReturnsException(
        lowerBound: Option[Int], upperBound: Option[Int]): Unit = {
      val selection = IndexRangeColumnSelection(None, Some(5))
      assertValidationReturnedException(
        selection, IllegalIndexRangeColumnSelectionException(selection))
    }

    assertColumnSelectionReturnsException(None, Some(5))
    assertColumnSelectionReturnsException(Some(4), None)
    assertColumnSelectionReturnsException(None, None)
    assertColumnSelectionReturnsException(Some(5), Some(4))
  }

  def assertValidationReturnedException(
      validable: ParametersSchema,
      exceptions: ValidationException*): Unit = {
    val validationExceptions = validable.validateParams
    assert(validationExceptions.size == exceptions.size)
    exceptions.foreach(
      e => assert( validationExceptions.contains(e))
    )
  }

  def assertValidationReturnedException(
      validable: IndexRangeColumnSelection,
      exception: ValidationException): Unit = {
    val validationExceptions = validable.validate
    assert(validationExceptions.size == 1)
    assert(validationExceptions.head == exception)
  }

}
