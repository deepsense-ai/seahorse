/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import org.scalatest.{Matchers, FunSuite}

import io.deepsense.deeplang.parameters.exceptions.TypeConversionException

class ParametersSuite extends FunSuite with Matchers {

  def fillBooleanHolderInSchema(key: String, schema: ParametersSchema, value: Boolean): Unit = {
    schema(key) match {
      case booleanHolder: BooleanParameterHolder => booleanHolder.value = Some(value)
      case _ => throw new IllegalStateException()
    }
  }

  def fillNumericHolderInSchema(key: String, schema: ParametersSchema, value: Double): Unit = {
    schema(key) match {
      case numericHolder: NumericParameterHolder => numericHolder.value = Some(value)
      case _ => throw new IllegalStateException()
    }
  }

  test("Getting Boolean Parameter") {
    val holder = BooleanParameterHolder("example", Some(true), true)
    holder.value = Some(true)
    val parametersSchema = ParametersSchema("x" -> holder)
    assert(parametersSchema.getBoolean("x").get == true)
  }

  test("Getting Numeric Parameter") {
    val holder = NumericParameterHolder("example", Some(3.1), true, RangeValidator(3, 4))
    holder.value = Some(3.2)
    val parametersSchema = ParametersSchema("x" -> holder)
    assert(parametersSchema.getDouble("x").get == 3.2)
  }

  test("Getting String Parameter") {
    val holder = StringParameterHolder("example", Some("default"), true, RegexValidator("a".r))
    holder.value = Some("abc")
    val parametersSchema = ParametersSchema("x" -> holder)
    assert(parametersSchema.getString("x").get == "abc")
  }

  test("Getting Choice Parameters") {
    val holderNumeric = NumericParameterHolder("description1", None, true, RangeValidator(3, 4))
    val holderBoolean = BooleanParameterHolder("description2", None, true)

    val choiceSchema = ParametersSchema("x" -> holderNumeric, "y" -> holderBoolean)
    val possibleChoices = Map("onlyChoice" -> choiceSchema)

    val choice = ChoiceParameterHolder("description", None, true, possibleChoices)
    choice.fill("onlyChoice", schema => {
      fillNumericHolderInSchema("x", schema, 3.5)
      fillBooleanHolderInSchema("y", schema, true)
    })

    val parametersSchema = ParametersSchema("choice" -> choice)
    parametersSchema.getChoice("choice").get match {
      case ChoiceParameter("onlyChoice", chosen) =>
        assert(chosen.getDouble("x").get == 3.5)
        assert(chosen.getBoolean("y").get == true)
    }
  }

  test("Getting MultipleChoice Parameters") {
    val holderNumeric = NumericParameterHolder("description1", None, true, RangeValidator(3, 4))
    val holderBoolean = BooleanParameterHolder("description2", None, true)

    val choiceSchema = ParametersSchema("x" -> holderNumeric, "y" -> holderBoolean)
    val possibleChoices = Map("onlyChoice" -> choiceSchema)

    val multipleChoice = MultipleChoiceParameterHolder("description", None, true, possibleChoices)
    multipleChoice.fill(Map("onlyChoice" -> (schema => {
      fillNumericHolderInSchema("x", schema, 3.5)
      fillBooleanHolderInSchema("y", schema, true)
    })))

    val parametersSchema = ParametersSchema("multipleChoice" -> multipleChoice)
    parametersSchema.getMultipleChoice("multipleChoice").get match {
      case MultipleChoiceParameter(chosen) =>
        val expected = Traversable(ChoiceParameter("onlyChoice", choiceSchema))
        assert(chosen == expected)
        assert(choiceSchema.getDouble("x").get == 3.5)
        assert(choiceSchema.getBoolean("y").get == true)
    }
  }

  test("Getting Parameters from Multiplicator") {
    val holder = BooleanParameterHolder("example", None, true)
    val schema = ParametersSchema("x" -> holder)
    val multiplicator = MultiplicatorParameterHolder("description", None, true, schema)

    val booleanParameter1 = false
    val booleanParameter2 = true

    multiplicator.fill(List(
      schema => fillBooleanHolderInSchema("x", schema, booleanParameter1),
      schema => fillBooleanHolderInSchema("x", schema, booleanParameter2)))

    val parametersSchema = ParametersSchema("key" -> multiplicator)
    parametersSchema.getMultiplicator("key").get match {
      case MultiplicatorParameter(schema1 :: schema2 :: Nil) =>
        assert(schema1.getBoolean("x").get == booleanParameter1)
        assert(schema2.getBoolean("x").get == booleanParameter2)
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
    assert(schema.getColumnSelection("x").get == parameter)
  }

  test("Getting wrong type of parameter should throw an exception") {
    val parameter = Some("abc")
    val expectedTargetTypeName = "scala.Double"
    val exception = intercept[TypeConversionException] {
      val holder = StringParameterHolder("description", None, true, RegexValidator("a".r))
      holder.value = parameter
      val parametersSchema = ParametersSchema("x" -> holder)
      parametersSchema.getDouble("x")
    }
    assert(exception == TypeConversionException(parameter.get, expectedTargetTypeName))
  }
}
