/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters

import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSuite, Matchers}

import io.deepsense.deeplang.parameters.exceptions.{NoSuchParameterException, TypeConversionException}

class ParametersSuite extends FunSuite with Matchers with MockitoSugar {

  test("Getting BooleanParameter from schema") {
    val param = mock[BooleanParameter]
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getBooleanParameter("x") eq param)
  }

  test("Getting NumericParameter from schema") {
    val param = mock[NumericParameter]
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getNumericParameter("x") eq param)
  }

  test("Getting StringParameter from schema") {
    val param = mock[StringParameter]
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getStringParameter("x") eq param)
  }

  test("Getting ChoiceParameter from schema") {
    val param = mock[ChoiceParameter]
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getChoiceParameter("x") eq param)
  }

  test("Getting MultipleChoiceParameter from schema") {
    val param = mock[MultipleChoiceParameter]
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getMultipleChoiceParameter("x") eq param)
  }

  test("Getting ParametersSequence from schema") {
    val param = mock[ParametersSequence]
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getParametersSequence("x") eq param)
  }

  test("Getting SingleColumnSelector from schema") {
    val param = mock[SingleColumnSelectorParameter]
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getSingleColumnSelectorParameter("x") eq param)
  }

  test("Getting ColumnSelector from schema") {
    val param = mock[ColumnSelectorParameter]
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getColumnSelectorParameter("x") eq param)
  }

  test("Getting wrong type of parameter should throw an exception") {
    val expectedTargetTypeName = "io.deepsense.deeplang.parameters.NumericParameter"
    val param = mock[StringParameter]
    val exception = intercept[TypeConversionException] {
      val parametersSchema = ParametersSchema("x" -> param)
      parametersSchema.getNumericParameter("x")
    }
    assert(exception == TypeConversionException(param, expectedTargetTypeName))
  }

  test("Getting BooleanParameter value from schema") {
    val param = BooleanParameter("example", Some(true), true)
    param.value = Some(true)
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getBoolean("x") == param.value)
  }

  test("Getting NumericParameter value from schema") {
    val param = NumericParameter("example", Some(3.1), true, RangeValidator(3, 4))
    param.value = Some(3.2)
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getDouble("x") == param.value)
  }

  test("Getting StringParameter value from schema") {
    val param = StringParameter("example", Some("default"), true, RegexValidator("a".r))
    param.value = Some("abc")
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getString("x") == param.value)
  }

  test("Getting ChoiceParameter value from schema") {
    val choiceSchema = mock[ParametersSchema]
    val possibleChoices = Map("onlyChoice" -> choiceSchema)

    val choice = ChoiceParameter("description", None, true, possibleChoices)
    choice.value = Some("onlyChoice")

    val parametersSchema = ParametersSchema("choice" -> choice)
    assert(parametersSchema.getChoice("choice").get == Selection("onlyChoice", choiceSchema))
  }

  test("Getting MultipleChoiceParameter value from schema") {
    val choiceSchema = mock[ParametersSchema]
    val possibleChoices = Map("onlyChoice" -> choiceSchema)
    val multipleChoice = MultipleChoiceParameter("", None, true, possibleChoices)
    multipleChoice.value = Some(Traversable("onlyChoice"))
    val parametersSchema = ParametersSchema("multipleChoice" -> multipleChoice)
    val actualMultipleSelection = parametersSchema.getMultipleChoice("multipleChoice").get
    val expectedMultipleSelection = Traversable(Selection("onlyChoice", choiceSchema))
    assert(actualMultipleSelection == expectedMultipleSelection)
  }

  test("Getting MultiplierParameter value from schema") {
    val schema = mock[ParametersSchema]
    val parametersSequence = ParametersSequence("", true, schema)
    val schema1 = mock[ParametersSchema]
    val schema2 = mock[ParametersSchema]
    parametersSequence.value = Some(Vector(schema1, schema2))
    val parametersSchema = ParametersSchema("key" -> parametersSequence)
    assert(parametersSchema.getMultiplicatedSchema("key") == parametersSequence.value)
  }

  test("Getting SingleColumnsSelector value from schema") {
    val param = SingleColumnSelectorParameter("description", true)
    val schema = ParametersSchema("x" -> param)
    val parameter = IndexSingleColumnSelection(1)
    param.value = Some(parameter)
    assert(schema.getSingleColumnSelection("x").get == parameter)
  }

  test("Getting ColumnSelector value from schema") {
    val param = ColumnSelectorParameter("description", true)
    val schema = ParametersSchema("x" -> param)
    val values = IndexColumnSelection(Set(1, 3))
    val parameter = MultipleColumnSelection(Vector(values))
    param.value = Some(parameter)
    assert(schema.getColumnSelection("x").get == parameter)
  }

  test("Getting wrong type of parameter value should throw an exception") {
    val parameter = Some("abc")
    val expectedTargetTypeName = "io.deepsense.deeplang.parameters.NumericParameter"
    val param = StringParameter("description", None, true, RegexValidator("a".r))
    val exception = intercept[TypeConversionException] {
      param.value = parameter
      val parametersSchema = ParametersSchema("x" -> param)
      parametersSchema.getDouble("x")
    }
    assert(exception == TypeConversionException(param, expectedTargetTypeName))
  }

  test("Merging two ParametersSchema objects") {
    val param1 = mock[NumericParameter]
    val parametersSchema1 = ParametersSchema("x1" -> param1)
    val param2 = mock[NumericParameter]
    val parametersSchema2 = ParametersSchema("x2" -> param2)

    val mergedSchema = parametersSchema1 ++ parametersSchema2
    assert(mergedSchema.getNumericParameter("x1") eq param1)
    assert(mergedSchema.getNumericParameter("x2") eq param2)
  }
}
