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

import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSuite, Matchers}

import io.deepsense.deeplang.parameters.exceptions.{VariableNotDefinedException, TypeConversionException}

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

  test("Getting SingleColumnCreator from schema") {
    val param = mock[SingleColumnCreatorParameter]
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getSingleColumnCreatorParameter("x") eq param)
  }

  test("Getting MultipleColumnCreator from schema") {
    val param = mock[MultipleColumnCreatorParameter]
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getMultipleColumnCreatorParameter("x") eq param)
  }

  test("Getting PrefixBasedColumnCreator from schema") {
    val param = mock[PrefixBasedColumnCreatorParameter]
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getPrefixBasedColumnCreatorParameter("x") eq param)
  }

  test("Getting CodeSnippetParameter from schema") {
    val param = mock[CodeSnippetParameter]
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getCodeSnippetParameter("x") eq param)
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
    val param = BooleanParameter("example", Some(true))
    param.value = Some(true)
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getBoolean("x") == param.value)
  }

  test("Getting NumericParameter value from schema") {
    val param = NumericParameter("example", Some(3.1), RangeValidator(3, 4))
    param.value = Some(3.2)
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getDouble("x") == param.value)
  }

  test("Getting StringParameter value from schema") {
    val param = StringParameter("example", Some("default"), RegexValidator("a".r))
    param.value = Some("abc")
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getString("x") == param.value)
  }

  test("Getting ChoiceParameter value from schema") {
    val choiceSchema = mock[ParametersSchema]
    val possibleChoices = ListMap("onlyChoice" -> choiceSchema)

    val choice = ChoiceParameter("description", None, possibleChoices)
    choice.value = Some("onlyChoice")

    val parametersSchema = ParametersSchema("choice" -> choice)
    assert(parametersSchema.getChoice("choice") == Selection("onlyChoice", choiceSchema))
  }

  test("Getting MultipleChoiceParameter value from schema") {
    val choiceSchema = mock[ParametersSchema]
    val possibleChoices = ListMap("onlyChoice" -> choiceSchema)
    val multipleChoice = MultipleChoiceParameter("", None, possibleChoices)
    multipleChoice.value = Some(Traversable("onlyChoice"))
    val parametersSchema = ParametersSchema("multipleChoice" -> multipleChoice)
    val actualMultipleSelection = parametersSchema.getMultipleChoice("multipleChoice")
    val expectedMultipleSelection = Traversable(Selection("onlyChoice", choiceSchema))
    assert(actualMultipleSelection == expectedMultipleSelection)
  }

  test("Getting MultiplierParameter value from schema") {
    val schema = mock[ParametersSchema]
    val parametersSequence = ParametersSequence("", schema)
    val schema1 = mock[ParametersSchema]
    val schema2 = mock[ParametersSchema]
    parametersSequence.value = Some(Vector(schema1, schema2))
    val parametersSchema = ParametersSchema("key" -> parametersSequence)
    assert(parametersSchema.getMultiplicatedSchema("key") == parametersSequence.value)
  }

  test("Getting SingleColumnsSelector value from schema") {
    val param = SingleColumnSelectorParameter("description", 0)
    val schema = ParametersSchema("x" -> param)
    val parameter = IndexSingleColumnSelection(1)
    param.value = Some(parameter)
    assert(schema.getSingleColumnSelection("x") == parameter)
  }

  test("Getting ColumnSelector value from schema") {
    val param = ColumnSelectorParameter("description", 0)
    val schema = ParametersSchema("x" -> param)
    val values = IndexColumnSelection(Set(1, 3))
    val parameter = MultipleColumnSelection(Vector(values), false)
    param.value = Some(parameter)
    assert(schema.getColumnSelection("x") == parameter)
  }

  test("Getting SingleColumnCreator value from schema") {
    val param = SingleColumnCreatorParameter("description", None)
    val schema = ParametersSchema("x" -> param)
    val value = "abc"
    param.value = Some(value)
    assert(schema.getNewColumnName("x") == param.value)
  }

  test("Getting MultipleColumnCreator value from schema") {
    val param = MultipleColumnCreatorParameter("description", None)
    val schema = ParametersSchema("x" -> param)
    val value = Vector("a", "b", "c")
    param.value = Some(value)
    assert(schema.getNewColumnNames("x") == param.value)
  }

  test("Getting PrefixBasedColumnCreator value from schema") {
    val param = PrefixBasedColumnCreatorParameter("description", None)
    val schema = ParametersSchema("x" -> param)
    val value = "abc"
    param.value = Some(value)
    assert(schema.getNewColumnsPrefix("x") == param.value)
  }

  test("Getting CodeSnippetParameter value from schema") {
    val codeSnippetLang = new CodeSnippetLanguage(CodeSnippetLanguage.Python)
    val param = CodeSnippetParameter("example", Some("default"), codeSnippetLang)
    param.value = Some("def operation_main(data_frame_1):\n  return out_data_frame_1")
    val parametersSchema = ParametersSchema("x" -> param)
    assert(parametersSchema.getCodeSnippet("x") == param.value)
  }

  test("Getting wrong type of parameter value should throw an exception") {
    val parameter = Some("abc")
    val expectedTargetTypeName = "io.deepsense.deeplang.parameters.NumericParameter"
    val param = StringParameter("description", None, RegexValidator("a".r))
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

  test("Comparing two parameters") {
    val param1 = BooleanParameter("xyz", None)
    param1.value = Some(true)
    val param2 = param1.copy()
    assert(param1 == param2)
    param2.value = Some(false)
    assert(param1 != param2)
  }

  test("Substituting placeholders") {
    val schema = Placeholders.createTestSchema

    Placeholders.setParameterValues(schema,
      "test ${string}",
      "${element1} element",
      "${element2} element",
      "${element1} and ${element2} ${string}s in single parameter",
      "${casesensitive}",
      "${CaseSensitive}",
      "${dot.separated.path}",
      "${dash-separated}",
      "'${outer ${inner}}' will be the same as '${outer ${inner}' with another '}'",
      "${wrap with special}",
      "${regex named group test}"
    )

    schema.substitutePlaceholders(Map(
      "default" -> "some value",
      "string" -> "value",
      "element1" -> "first",
      "element2" -> "second",
      "columnselector" -> "changed",
      "casesensitive" -> "lowercase",
      "CaseSensitive" -> "CamelCase",
      "dot.separated.path" -> "with dots",
      "dash-separated" -> "with dashes",
      "outer ${inner" -> "nested",
      "inner" -> "not matched",
      "outer ${inner}' with another '" -> "not matched",
      "inner}' with another '" -> "not matched",
      "wrap with special" -> "${wrapped}",
      "wrapped" -> "not matched",
      "regex named group test" -> "${name}"
    ))

    Placeholders.checkParameterValues(schema,
      "test value",
      "some value",
      "first element",
      "second element",
      "first and second values in single parameter",
      "lowercase",
      "CamelCase",
      "with dots",
      "with dashes",
      "'nested}' will be the same as 'nested' with another '}'",
      "${wrapped}",
      "${name}"
    )
  }

  test("Substituting placeholders throws when variable was not defined") {
    val schema = Placeholders.createTestSchema

    Placeholders.setParameterValues(schema,
      "${unset} value"
    )

    a[VariableNotDefinedException] should be thrownBy {
      schema.substitutePlaceholders(Map(
        "default" -> "some value"
      ))
    }
  }

  private object Placeholders {

    def createTestSchema: ParametersSchema = {
      val stringParam = StringParameter("", None, new AcceptAllRegexValidator)
      val defaultStringParam = StringParameter("", Some("${default}"), new AcceptAllRegexValidator)
      val numericParam = NumericParameter("", None, RangeValidator(0.0, 1.0))

      val sequenceParam = ParametersSequence("", ParametersSchema(
        "string" -> StringParameter("", None, new AcceptAllRegexValidator),
        "numeric" -> NumericParameter("", None, RangeValidator(0.0, 1.0)),
        "column" -> SingleColumnSelectorParameter("", 0)
      ))

      ParametersSchema(
        "string" -> stringParam,
        "default" -> defaultStringParam,
        "numeric" -> numericParam,
        "sequence" -> sequenceParam
      )
    }

    def setParameterValues(
        schema: ParametersSchema,
        stringParamValue: String,
        sequenceParamsValues: String*): Unit = {

      schema.getStringParameter("string").value = stringParamValue
      schema.getNumericParameter("numeric").value = 0.5

      val sequenceParamValue = sequenceParamsValues.map(value => {
        val element = schema.getParametersSequence("sequence").replicateSchema
        element.getStringParameter("string").value = value
        element.getSingleColumnSelectorParameter("column").value =
          NameSingleColumnSelection("${columnselector}")
        element
      }).toVector

      schema.getParametersSequence("sequence").value = sequenceParamValue
    }

    def checkParameterValues(
        schema: ParametersSchema,
        stringParamValue: String,
        defaultParamValue: String,
        sequenceParamsValues: String*): Unit = {

      schema.getStringParameter("string").value shouldBe stringParamValue
      schema.getStringParameter("default").value shouldBe defaultParamValue
      schema.getNumericParameter("numeric").value shouldBe 0.5

      val elements = schema.getParametersSequence("sequence").value

      elements.zipAll(sequenceParamsValues, ParametersSchema(), "").foreach {
        case (element, value) =>
          element.getStringParameter("string").value shouldBe value
          element.getSingleColumnSelectorParameter("column").value shouldBe
            NameSingleColumnSelection("${columnselector}")
      }
    }
  }
}
