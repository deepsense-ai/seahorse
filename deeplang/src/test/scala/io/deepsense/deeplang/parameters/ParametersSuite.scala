/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import org.scalatest.{FunSuite, Matchers}

import io.deepsense.deeplang.parameters.exceptions.TypeConversionException

class ParametersSuite extends FunSuite with Matchers {

  test("Getting Boolean Parameter") {
    val param = BooleanParameter("example", Some(true), true)
    param.value = Some(true)
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getBoolean("x").get == true)
  }

  test("Getting Numeric Parameter") {
    val param = NumericParameter("example", Some(3.1), true, RangeValidator(3, 4))
    param.value = Some(3.2)
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getDouble("x").get == 3.2)
  }

  test("Getting String Parameter") {
    val param = StringParameter("example", Some("default"), true, RegexValidator("a".r))
    param.value = Some("abc")
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getString("x").get == "abc")
  }

  test("Getting Choice Parameters") {
    val paramNumeric = NumericParameter("description1", None, true, RangeValidator(3, 4))
    val paramBoolean = BooleanParameter("description2", None, true)

    val choiceSchema = ParametersSchema("x" -> paramNumeric, "y" -> paramBoolean)
    val possibleChoices = Map("onlyChoice" -> choiceSchema)

    val choice = ChoiceParameter("description", None, true, possibleChoices)
    choice.fill("onlyChoice", schema => {
      schema.getNumericParameter("x").value = Some(3.5)
      schema.getBooleanParameter("y").value = Some(true)
    })

    val parametersSchema = ParametersSchema("choice" -> choice)
    parametersSchema.getChoice("choice").get match {
      case Selection("onlyChoice", chosen) =>
        assert(chosen.getDouble("x").get == 3.5)
        assert(chosen.getBoolean("y").get == true)
    }
  }

  test("Getting MultipleChoice Parameters") {
    val paramNumeric = NumericParameter("description1", None, true, RangeValidator(3, 4))
    val paramBoolean = BooleanParameter("description2", None, true)

    val choiceSchema = ParametersSchema("x" -> paramNumeric, "y" -> paramBoolean)
    val possibleChoices = Map("onlyChoice" -> choiceSchema)

    val multipleChoice = MultipleChoiceParameter("description", None, true, possibleChoices)
    multipleChoice.fill(Map("onlyChoice" -> (schema => {
      schema.getNumericParameter("x").value = Some(3.5)
      schema.getBooleanParameter("y").value = Some(true)
    })))

    val parametersSchema = ParametersSchema("multipleChoice" -> multipleChoice)
    parametersSchema.getMultipleChoice("multipleChoice").get match {
      case MultipleSelection(chosen) =>
        val expected = Traversable(Selection("onlyChoice", choiceSchema))
        assert(chosen == expected)
        assert(choiceSchema.getDouble("x").get == 3.5)
        assert(choiceSchema.getBoolean("y").get == true)
    }
  }

  test("Getting Parameters from Multiplier") {
    val param = BooleanParameter("example", None, true)
    val schema = ParametersSchema("x" -> param)
    val multiplicator = MultiplierParameter("description", None, true, schema)

    val booleanParameter1 = Some(false)
    val booleanParameter2 = Some(true)

    multiplicator.fill(List(
      schema => schema.getBooleanParameter("x").value = booleanParameter1,
      schema => schema.getBooleanParameter("x").value = booleanParameter2))

    val parametersSchema = ParametersSchema("key" -> multiplicator)
    parametersSchema.getMultiplicated("key").get match {
      case Multiplied(schema1 :: schema2 :: Nil) =>
        assert(schema1.getBoolean("x") == booleanParameter1)
        assert(schema2.getBoolean("x") == booleanParameter2)
    }
  }

  test("Getting single columns selector parameter") {
    val param = SingleColumnSelectorParameter("description", true)
    val schema = ParametersSchema("x" -> param)
    val parameter = IndexSingleColumnSelection(1)
    param.value = Some(parameter)
    assert(schema.getSingleColumnSelection("x").get == parameter)
  }

  test("Getting multiple columns selector parameter") {
    val param = ColumnSelectorParameter("description", true)
    val schema = ParametersSchema("x" -> param)
    val values = IndexColumnSelection(List(1, 3))
    val parameter = MultipleColumnSelection(List(values))
    param.value = Some(parameter)
    assert(schema.getColumnSelection("x").get == parameter)
  }

  test("Getting wrong type of parameter should throw an exception") {
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
}
