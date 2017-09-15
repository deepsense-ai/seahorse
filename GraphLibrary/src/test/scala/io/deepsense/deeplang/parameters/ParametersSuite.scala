/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import org.scalatest.{Matchers, FunSuite}

import io.deepsense.deeplang.parameters.exceptions.TypeConversionException

class ParametersSuite extends FunSuite with Matchers {
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
    choice.value = Some(ChoiceParameter("onlyChoice", choice.options("onlyChoice")))
    holderNumeric.value = Some(NumericParameter(3.5))
    holderBoolean.value = Some(BooleanParameter(true))

    val parametersSchema = ParametersSchema("choice" -> choice)
    parametersSchema.getChoiceParameter("choice").get match {
      case ChoiceParameter("onlyChoice", schema) =>
        assert(schema.getNumericParameter("x").get.value == 3.5)
        assert(schema.getBooleanParameter("y").get.value == true)
      case _ => throw new IllegalStateException
    }
  }

  test("Getting MultipleChoice Parameters") {
    val holderNumeric = NumericParameterHolder("description1", None, true, RangeValidator(3, 4))
    val holderBoolean = BooleanParameterHolder("description2", None, true)
    holderNumeric.value = Some(NumericParameter(3.5))
    holderBoolean.value = Some(BooleanParameter(true))

    val multipleChoiceSchema = ParametersSchema("x" -> holderNumeric, "y" -> holderBoolean)
    val possibleChoices = Map("onlyChoice" -> multipleChoiceSchema)

    val multipleChoice = MultipleChoiceParameterHolder("description", None, true, possibleChoices)
    val chosenParameters = Set(ChoiceParameter("onlyChoice", multipleChoice.options("onlyChoice")))
    multipleChoice.value = Some(MultipleChoiceParameter(chosenParameters))

    val parametersSchema = ParametersSchema("multipleChoice" -> multipleChoice)
    parametersSchema.getMultipleChoiceParameter("multipleChoice").get match {
      case MultipleChoiceParameter(chosen) =>
        assert(chosen.size == 1)
        chosen should contain theSameElementsAs chosenParameters
      case _ => throw new IllegalStateException
    }
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
