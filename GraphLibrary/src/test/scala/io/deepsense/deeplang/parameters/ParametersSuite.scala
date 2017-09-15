/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import org.scalatest.{Matchers, FunSuite}

import io.deepsense.deeplang.parameters.exceptions.TypeConversionException

class ParametersSuite extends FunSuite with Matchers {

  def fillBooleanHolderInSchema(
      key: String,
      schema: ParametersSchema,
      value: BooleanParameter): Unit = {
    schema(key) match {
      case booleanHolder: BooleanParameterHolder => booleanHolder.value = Some(value)
      case _ => throw new IllegalStateException()
    }
  }

  def fillNumericHolderInSchema(
      key: String,
      schema: ParametersSchema,
      value: NumericParameter): Unit = {
    schema(key) match {
      case numericHolder: NumericParameterHolder => numericHolder.value = Some(value)
      case _ => throw new IllegalStateException()
    }
  }

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
    val holder = StringParameterHolder("example", "default", true, RegexValidator("a".r))
    holder.value = Some(StringParameter("abc"))
    val parametersSchema = ParametersSchema("x" -> holder)
    assert(parametersSchema.getStringParameter("x").get.value == "abc")
  }

  test("Getting Choice Parameters") {
    val holderNumeric = NumericParameterHolder("description1", None, true, RangeValidator(3, 4))
    val holderBoolean = BooleanParameterHolder("description2", None, true)

    val choiceSchema = ParametersSchema("x" -> holderNumeric, "y" -> holderBoolean)
    val possibleChoices = Map("onlyChoice" -> choiceSchema)

    val choice = ChoiceParameterHolder("description", None, true, possibleChoices)
    choice.fill("onlyChoice", schema => {
      fillNumericHolderInSchema("x", schema, NumericParameter(3.5))
      fillBooleanHolderInSchema("y", schema, BooleanParameter(true))
    })

    val parametersSchema = ParametersSchema("choice" -> choice)
    parametersSchema.getChoiceParameter("choice").get match {
      case ChoiceParameter("onlyChoice", chosen) =>
        assert(chosen.getNumericParameter("x").get.value == 3.5)
        assert(chosen.getBooleanParameter("y").get.value == true)
    }
  }

  test("Getting MultipleChoice Parameters") {
    val holderNumeric = NumericParameterHolder("description1", None, true, RangeValidator(3, 4))
    val holderBoolean = BooleanParameterHolder("description2", None, true)

    val choiceSchema = ParametersSchema("x" -> holderNumeric, "y" -> holderBoolean)
    val possibleChoices = Map("onlyChoice" -> choiceSchema)

    val multipleChoice = MultipleChoiceParameterHolder("description", None, true, possibleChoices)
    multipleChoice.fill(Map("onlyChoice" -> (schema => {
      fillNumericHolderInSchema("x", schema, NumericParameter(3.5))
      fillBooleanHolderInSchema("y", schema, BooleanParameter(true))
    })))

    val parametersSchema = ParametersSchema("multipleChoice" -> multipleChoice)
    parametersSchema.getMultipleChoiceParameter("multipleChoice").get match {
      case MultipleChoiceParameter(chosen) =>
        val expected = Traversable(ChoiceParameter("onlyChoice", choiceSchema))
        assert(chosen == expected)
        assert(choiceSchema.getNumericParameter("x").get.value == 3.5)
        assert(choiceSchema.getBooleanParameter("y").get.value == true)
    }
  }

  test("Getting Parameters from Multiplicator") {
    val holder = BooleanParameterHolder("example", None, true)
    val schema = ParametersSchema("x" -> holder)
    val multiplicator = MultiplicatorParameterHolder("description", None, true, schema)

    val booleanParameter1 = BooleanParameter(false)
    val booleanParameter2 = BooleanParameter(true)

    multiplicator.fill(List(
      schema => fillBooleanHolderInSchema("x", schema, booleanParameter1),
      schema => fillBooleanHolderInSchema("x", schema, booleanParameter2)))

    val parametersSchema = ParametersSchema("key" -> multiplicator)
    parametersSchema.getMultiplicatorParameter("key").get match {
      case MultiplicatorParameter(schema1 :: schema2 :: Nil) =>
        assert(schema1.getBooleanParameter("x").get == booleanParameter1)
        assert(schema2.getBooleanParameter("x").get == booleanParameter2)
    }
  }

  test("Getting single columns selector parameter") {
    val holder = SingleColumnSelectorParameterHolder("description", true)
    val schema = ParametersSchema("x" -> holder)
    val parameter = IndexSingleColumnSelection(1)
    holder.value = Some(parameter)
    assert(schema.getSingleColumnSelection("x").get == parameter)
  }

  test("Getting multiple columns selector parameter") {
    val holder = MultipleColumnSelectorParameterHolder("description", true)
    val schema = ParametersSchema("x" -> holder)
    val values = IndexColumnSelection(List(1, 3))
    val parameter = MultipleColumnSelection(List(values))
    holder.value = Some(parameter)
    assert(schema.getMultipleColumnSelection("x").get == parameter)
  }

  test("Getting wrong type of parameter should throw an exception") {
    val parameter = Some(StringParameter("abc"))
    val expectedTargetTypeName = "io.deepsense.deeplang.parameters.NumericParameter"
    val exception = intercept[TypeConversionException] {
      val holder = StringParameterHolder("description", None, true, RegexValidator("a".r))
      holder.value = parameter
      val parametersSchema = ParametersSchema("x" -> holder)
      parametersSchema.getNumericParameter("x")
    }
    assert(exception == TypeConversionException(parameter.get, expectedTargetTypeName))
  }
}
