/**
 * Copyright 2015, CodiLime Inc.
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
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSuite, Matchers}
import spray.json._

class ParametersJsonSuite extends FunSuite with Matchers with MockitoSugar {

  test("ParametersSchema can provide its json representation") {
    val mockParameter1 = mock[Parameter]
    when(mockParameter1.jsDescription) thenReturn Map("mockKey1" -> JsString("mockValue1"))
    val mockParameter2 = mock[Parameter]
    when(mockParameter2.jsDescription) thenReturn Map("mockKey2" -> JsString("mockValue2"))
    val schema = ParametersSchema("y" -> mockParameter1, "x" -> mockParameter2)
    val expectedJson = JsArray(
      JsObject("name" -> JsString("y"), "mockKey1" -> JsString("mockValue1")),
      JsObject("name" -> JsString("x"), "mockKey2" -> JsString("mockValue2"))
    )
    assert(schema.toJson == expectedJson)
  }

  test("Parameters in jsRepresentation of ParametersSchema are in order of insertion") {
    val param = mock[Parameter]
    when(param.jsDescription) thenReturn Map.empty[String, JsValue]
    val schema = ParametersSchema(
      "z" -> param, "b" -> param, "a" -> param, "y" -> param, "x" -> param, "c" -> param
    )
    val expectedJson = JsArray(
      JsObject("name" -> JsString("z")),
      JsObject("name" -> JsString("b")),
      JsObject("name" -> JsString("a")),
      JsObject("name" -> JsString("y")),
      JsObject("name" -> JsString("x")),
      JsObject("name" -> JsString("c"))
    )
    assert(schema.toJson == expectedJson)
  }

  test("ParametersSchema can provide json representation of values of its parameters") {
    val mockParameter1 = mock[Parameter]
    when(mockParameter1.valueToJson) thenReturn JsObject("mockKey1" -> JsString("mockValue1"))
    val mockParameter2 = mock[Parameter]
    when(mockParameter2.valueToJson) thenReturn JsObject("mockKey2" -> JsString("mockValue2"))

    val schema = ParametersSchema("x" -> mockParameter1, "y" -> mockParameter2)
    val expectedJson = JsObject(
      "x" -> mockParameter1.valueToJson,
      "y" -> mockParameter2.valueToJson)
    assert(schema.valueToJson == expectedJson)
  }

  test("ParametersSchema can be filled based on json") {
    val mockParameter1 = mock[Parameter]
    val mockParameter2 = mock[Parameter]
    val mockParameter3 = mock[Parameter]
    val innerJsValue1 = JsObject("a" -> JsString("b"))
    val innerJsValue2 = JsObject("c" -> JsString("d"))

    val schema = ParametersSchema(
      "x" -> mockParameter1,
      "y" -> mockParameter2,
      "z" -> mockParameter3)  // Note: not all parameters have to be filled
    schema.fillValuesWithJson(JsObject("x" -> innerJsValue1, "y" -> innerJsValue2))
    verify(mockParameter1).fillValueWithJson(innerJsValue1)
    verify(mockParameter2).fillValueWithJson(innerJsValue2)
  }

  test("ParametersSchema can be filled with JsNull and nothing happens") {
    val mockParameter1 = mock[Parameter]
    val mockParameter2 = mock[Parameter]
    val schema = ParametersSchema(
      "x" -> mockParameter1,
      "y" -> mockParameter2)
    schema.fillValuesWithJson(JsNull)
  }

  test("ParametersSchema throws when filled with json containing unknown label") {
    val mockParameter1 = mock[Parameter]
    val mockParameter2 = mock[Parameter]
    val schema = ParametersSchema("x" -> mockParameter1, "y" -> mockParameter2)

    a [DeserializationException] should be thrownBy {
      schema.fillValuesWithJson(JsObject("x" -> JsNull, "y" -> JsNull, "z" -> JsNull))
    }
  }

  test("Json representation of parameter without default value provided has no 'default field") {
    val booleanParameter = BooleanParameter("description", None, required = false)
    assert(!booleanParameter.jsDescription.contains("default"))
  }

  test("Json representation of value of not set parameter value is null") {
    val notFilledMockParameter = mock[Parameter]
    when(notFilledMockParameter.valueToJson) thenCallRealMethod()
    when(notFilledMockParameter.value) thenReturn None
    assert(notFilledMockParameter.valueToJson == JsNull)
  }

  test("Boolean parameter can provide its json representation") {
    val description = "example description"
    val default = true
    val booleanParameter = BooleanParameter(description, Some(default), required = false)

    val expectedFields = Map(
      "type" -> JsString("boolean"),
      "description" -> JsString(description),
      "default" -> JsBoolean(default),
      "required" -> JsBoolean(false))

    assert(booleanParameter.jsDescription == expectedFields)
  }

  test("Boolean parameter can provide json representation of it's value") {
    val booleanParameter = BooleanParameter("", None, required = false)
    val value = true
    booleanParameter.value = Some(value)
    assert(booleanParameter.valueToJson == JsBoolean(value))
  }

  test("Boolean parameter can be filled with json") {
    val booleanParameter = BooleanParameter("", None, required = false)
    val value = true
    booleanParameter.fillValueWithJson(JsBoolean(value))
    assert(booleanParameter.value == Some(value))
  }

  test("Boolean parameter can be filled with JsNull") {
    val booleanParameter = BooleanParameter("", None, required = false)
    booleanParameter.fillValueWithJson(JsNull)
    assert(booleanParameter.value == None)
  }

  test("Numeric parameter can provide its json representation") {
    val description = "example description"
    val default = 4.5
    val required = false
    val validator = RangeValidator(0.1, 100.1, beginIncluded = true, endIncluded = false, Some(0.2))
    val numericParameter = NumericParameter(description, Some(default), required, validator)

    val expectedFields = Map(
      "type" -> JsString("numeric"),
      "description" -> JsString(description),
      "default" -> JsNumber(default),
      "required" -> JsBoolean(required),
      "validator" -> JsObject(
        "type" ->  JsString("range"),
        "configuration" -> JsObject(
          "begin" -> JsNumber(0.1),
          "end" -> JsNumber(100.1),
          "beginIncluded" -> JsBoolean(true),
          "endIncluded" -> JsBoolean(false),
          "step" -> JsNumber(0.2)
        )
      )
    )

    assert(numericParameter.jsDescription == expectedFields)
  }

  test("Numeric parameter can provide json representation of it's value") {
    val mockValidator = mock[Validator[Double]]
    val numericParameter = NumericParameter("", None, required = false, mockValidator, value = None)
    val value = 3.14
    numericParameter.value = Some(value)
    assert(numericParameter.valueToJson == JsNumber(value))
  }

  test("Numeric parameter can be filled with json") {
    val mockValidator = mock[Validator[Double]]
    val numericParameter = NumericParameter("", None, required = false, mockValidator, value = None)
    val value = 3.15
    numericParameter.fillValueWithJson(JsNumber(value))
    assert(numericParameter.value == Some(value))
  }

  test("Numeric parameter can be filled with JsNull") {
    val mockValidator = mock[Validator[Double]]
    val numericParameter = NumericParameter("", None, required = false, mockValidator, value = None)
    numericParameter.fillValueWithJson(JsNull)
    assert(numericParameter.value == None)
  }

  test("String parameter can provide its json representation") {
    val description = "example string parameter description"
    val default = "default value"
    val required = true
    val validator = RegexValidator("xyz".r)
    val stringParameter = StringParameter(description, Some(default), required, validator)

    val expectedFields = Map(
      "type" -> JsString("string"),
      "description" -> JsString(description),
      "default" -> JsString(default),
      "required" -> JsBoolean(required),
      "validator" -> JsObject(
        "type" ->  JsString("regex"),
        "configuration" -> JsObject(
          "regex" -> JsString("xyz")
        )
      )
    )
    assert(stringParameter.jsDescription == expectedFields)
  }

  test("String parameter can provide json representation of it's value") {
    val mockValidator = mock[Validator[String]]
    val stringParameter = StringParameter("", None, required = false, mockValidator, value = None)
    val value = "abc"
    stringParameter.value = Some(value)
    assert(stringParameter.valueToJson == JsString(value))
  }

  test("String parameter can be filled with json") {
    val mockValidator = mock[Validator[String]]
    val stringParameter = StringParameter("", None, required = false, mockValidator, value = None)
    val value = "abcd"
    stringParameter.fillValueWithJson(JsString(value))
    assert(stringParameter.value == Some(value))
  }

  test("String parameter can be filled with JsNull") {
    val mockValidator = mock[Validator[String]]
    val stringParameter = StringParameter("", None, required = false, mockValidator, value = None)
    stringParameter.fillValueWithJson(JsNull)
    assert(stringParameter.value == None)
  }

  test("Choice parameter can provide its json representation") {
    val description = "example choice parameter description"
    val default = "filledChoice"
    val required = true
    val mockParameter = mock[Parameter]
    when(mockParameter.jsDescription) thenReturn Map("mockKey" -> JsString("mockValue"))
    val filledSchema = ParametersSchema("x" -> mockParameter)
    val possibleChoices = ListMap(
      "filledChoice" -> filledSchema,
      "emptyChoice" -> ParametersSchema()
    )
    val choiceParameter = ChoiceParameter(description, Some(default), required, possibleChoices)

    val expectedFields = Map(
      "type" -> JsString("choice"),
      "description" -> JsString(description),
      "default" -> JsString(default),
      "required" -> JsBoolean(required),
      "values" -> JsArray(
        JsObject(
          "name" -> JsString("filledChoice"),
          "schema" -> JsArray(
            JsObject("name" -> JsString("x"), "mockKey" -> JsString("mockValue"))
          )
        ),
        JsObject(
          "name" -> JsString("emptyChoice"),
          "schema" -> JsNull
        )
      )
    )

    assert(choiceParameter.jsDescription == expectedFields)
  }

  test("Order of options in ChoiceParameter jsRepresentation is order of their insertion") {
    val description = "example multiple choice parameter description"
    val required = true
    val (possibleChoices, choicesJsDescription) = choicesAndJsFieldsForParametersWithOptions
    val choiceParameter = MultipleChoiceParameter(
      description, None, required, possibleChoices)

    val expectedFields = Map(
      "type" -> JsString("multipleChoice"),
      "description" -> JsString(description),
      "required" -> JsBoolean(required),
      "values" -> choicesJsDescription
    )

    assert(choiceParameter.jsDescription == expectedFields)
  }

  test("Choice parameter can provide json representation of it's value") {
    val filledSchema = mock[ParametersSchema]
    when(filledSchema.valueToJson) thenReturn JsObject("x" -> JsString("y"))
    val possibleChoices = ListMap(
      "filledChoice" -> filledSchema,
      "emptyChoice" -> ParametersSchema()
    )
    val choiceParameter = ChoiceParameter("", None, required = false, possibleChoices, value = None)
    choiceParameter.value = Some("filledChoice")

    val expectedJson = JsObject("filledChoice" -> filledSchema.valueToJson)
    assert(choiceParameter.valueToJson == expectedJson)
  }

  test("Choice parameter can be filled with json") {
    val filledSchema = mock[ParametersSchema]
    val possibleChoices = ListMap(
      "filledChoice" -> filledSchema,
      "emptyChoice" -> ParametersSchema()
    )
    val choiceParameter = ChoiceParameter("", None, required = false, possibleChoices, value = None)
    val innerJsValue = JsString("mock inner value")
    choiceParameter.fillValueWithJson(JsObject("filledChoice" -> innerJsValue))
    verify(filledSchema).fillValuesWithJson(innerJsValue)
  }

  test("Choice parameter throws when multiple options are chosen in json") {
    val filledSchema = mock[ParametersSchema]
    val possibleChoices = ListMap(
      "filledChoice" -> filledSchema, "emptyChoice" -> ParametersSchema()
    )
    val choiceParameter = ChoiceParameter("", None, required = false, possibleChoices, value = None)
    a [DeserializationException] should be thrownBy {
      choiceParameter.fillValueWithJson(JsObject(
        "filledChoice" -> JsString("mock1"),
        "emptyChoice" -> JsString("mock2")))
    }
  }

  test("Choice parameter throws when non-existing option is chosen in json") {
    val possibleChoices = ListMap("onlyChoice" -> ParametersSchema())
    val choiceParameter = ChoiceParameter("", None, required = false, possibleChoices, value = None)
    a [DeserializationException] should be thrownBy {
      choiceParameter.fillValueWithJson(JsObject("nonExistingChoice" -> JsString("mock1")))
    }
  }

  test("Choice parameter can be filled with JsNull") {
    val choiceParameter = ChoiceParameter("", None, required = false, ListMap.empty, value = None)
    choiceParameter.fillValueWithJson(JsNull)
    assert(choiceParameter.value == None)
  }

  test("Multiple choice parameter can provide its json representation") {
    val description = "example multiple choice parameter description"
    val default = Vector("filledChoice", "emptyChoice")
    val required = true
    val mockParameter = mock[Parameter]
    when(mockParameter.jsDescription) thenReturn Map("mockKey" -> JsString("mockValue"))
    val filledSchema = ParametersSchema("x" -> mockParameter)
    val possibleChoices = ListMap(
      "filledChoice" -> filledSchema,
      "emptyChoice" -> ParametersSchema()
    )
    val multipleChoiceParameter = MultipleChoiceParameter(
      description, Some(default), required, possibleChoices)

    val expectedFields = Map(
      "type" -> JsString("multipleChoice"),
      "description" -> JsString(description),
      "default" -> JsArray(default.map(x => JsString(x))),
      "required" -> JsBoolean(required),
      "values" -> JsArray(
        JsObject(
          "name" -> JsString("filledChoice"),
          "schema" -> JsArray(
            JsObject("name" -> JsString("x"), "mockKey" -> JsString("mockValue"))
          )
        ),
        JsObject(
          "name" -> JsString("emptyChoice"),
          "schema" -> JsNull
        )
      )
    )

    assert(multipleChoiceParameter.jsDescription == expectedFields)
  }

  test("Order of options in MultipleChoiceParameter jsRepresentation is order of their insertion") {
    val description = "example multiple choice parameter description"
    val required = true
    val (possibleChoices, choicesJsDescription) = choicesAndJsFieldsForParametersWithOptions
    val multipleChoiceParameter = MultipleChoiceParameter(
      description, None, required, possibleChoices)

    val expectedFields = Map(
      "type" -> JsString("multipleChoice"),
      "description" -> JsString(description),
      "required" -> JsBoolean(required),
      "values" -> choicesJsDescription
    )

    assert(multipleChoiceParameter.jsDescription == expectedFields)
  }

  test("Multiple choice parameter can provide json representation of it's value") {
    val filledSchema = mock[ParametersSchema]
    when(filledSchema.valueToJson) thenReturn JsObject("x" -> JsString("y"))
    val possibleChoices = ListMap(
      "filledChoice" -> filledSchema,
      "emptyChoice" -> ParametersSchema()
    )
    val multipleChoiceParameter = MultipleChoiceParameter(
      "", None, required = false, possibleChoices, value = None)
    multipleChoiceParameter.value = Some(Traversable("filledChoice", "emptyChoice"))

    val expectedJson = JsObject(
      "filledChoice" -> filledSchema.valueToJson,
      "emptyChoice" -> ParametersSchema().valueToJson)

    assert(multipleChoiceParameter.valueToJson == expectedJson)
  }

  test("Multiple choice parameter can be filled with json") {
    val schema1 = mock[ParametersSchema]
    val schema2 = mock[ParametersSchema]
    val possibleChoices = ListMap("choice1" -> schema1, "choice2" -> schema2)
    val multipleChoiceParameter = MultipleChoiceParameter(
      "", None, required = false, possibleChoices, value = None)
    val innerJsValue1 = JsString("mock inner value 1")
    val innerJsValue2 = JsString("mock inner value 2")
    multipleChoiceParameter.fillValueWithJson(JsObject(
      "choice1" -> innerJsValue1, "choice2" -> innerJsValue2))
    verify(schema1).fillValuesWithJson(innerJsValue1)
    verify(schema2).fillValuesWithJson(innerJsValue2)
  }

  test("Multiple choice parameter throws when non-existing option is chosen in json") {
    val possibleChoices = ListMap("onlyChoice" -> ParametersSchema())
    val multipleChoiceParameter = MultipleChoiceParameter(
      "", None, required = false, possibleChoices, value = None)
    a [DeserializationException] should be thrownBy {
      multipleChoiceParameter.fillValueWithJson(JsObject("nonExistingChoice" -> JsString("mock1")))
    }
  }

  test("Multiple Choice parameter can be filled with JsNull") {
    val multipleChoiceParameter = MultipleChoiceParameter(
      "", None, required = false, ListMap.empty, value = None)
    multipleChoiceParameter.fillValueWithJson(JsNull)
    assert(multipleChoiceParameter.value == None)
  }

  test("Parameters sequence can provide its json representation") {
    val description = "example parameter description"
    val required = false
    val innerSchema = mock[ParametersSchema]
    when(innerSchema.toJson) thenReturn JsObject("x" -> JsString("y"))
    val parametersSequence = ParametersSequence(
      description, required, innerSchema)

    val expectedFields = Map(
      "type" -> JsString("multiplier"),
      "description" -> JsString(description),
      "required" -> JsBoolean(required),
      "values" -> innerSchema.toJson
    )
    assert(parametersSequence.jsDescription == expectedFields)
  }

  test("Parameters sequence can provide json representation of it's value") {
    val innerSchema = mock[ParametersSchema]
    when(innerSchema.valueToJson) thenReturn JsObject("x" -> JsString("y"))
    val parametersSequence = ParametersSequence("", required = false, innerSchema, value = None)
    parametersSequence.value = Some(Vector(innerSchema))
    val expectedJson = JsArray(innerSchema.valueToJson)
    assert(parametersSequence.valueToJson == expectedJson)
  }

  test("Parameters sequence can be filled with json") {
    val innerSchema = mock[ParametersSchema]
    when(innerSchema.replicate) thenReturn innerSchema
    val parametersSequence = ParametersSequence("", required = false, innerSchema, value = None)
    val innerJsValue1 = JsString("mock inner value 1")
    val innerJsValue2 = JsString("mock inner value 2")
    parametersSequence.fillValueWithJson(JsArray(innerJsValue1, innerJsValue2))
    verify(innerSchema).fillValuesWithJson(innerJsValue1)
    verify(innerSchema).fillValuesWithJson(innerJsValue2)
  }

  test("Parameters sequence can be filled with JsNull") {
    val parametersSequence = ParametersSequence(
      "", required = false, ParametersSchema(), value = None)
    parametersSequence.fillValueWithJson(JsNull)
    assert(parametersSequence.value == None)
  }

  test("Single column selector can provide its json representation") {
    val description = "example single selector parameter description"
    val required = false
    val columnSelectorParameter = SingleColumnSelectorParameter(description, required, 2)

    val expectedFields = Map(
      "type" -> JsString("selector"),
      "description" -> JsString(description),
      "required" -> JsBoolean(required),
      "isSingle" -> JsBoolean(true),
      "portIndex" -> JsNumber(2))

    assert(columnSelectorParameter.jsDescription == expectedFields)
  }

  test("Single column selector by index can provide json representation of it's value") {
    val columnSelectorParameter = SingleColumnSelectorParameter("", required = false, portIndex = 0)
    val value = 4
    columnSelectorParameter.value = Some(IndexSingleColumnSelection(value))

    val expectedJson = JsObject("type" -> JsString("index"), "value" -> JsNumber(value))
    assert(columnSelectorParameter.valueToJson == expectedJson)
  }

  test("Single column selector by name can provide json representation of it's value") {
    val columnSelectorParameter = SingleColumnSelectorParameter("", required = false, portIndex = 0)
    val value = "some_name"
    columnSelectorParameter.value = Some(NameSingleColumnSelection(value))

    val expectedJson = JsObject("type" -> JsString("column"), "value" -> JsString(value))
    assert(columnSelectorParameter.valueToJson == expectedJson)
  }

  test("Single column selector can be filled with json selection by index") {
    val columnSelectorParameter = SingleColumnSelectorParameter("", required = false, portIndex = 0)
    val someValue = 4
    columnSelectorParameter.fillValueWithJson(JsObject(
      "type" -> JsString("index"),
      "value" -> JsNumber(someValue)))
    assert(columnSelectorParameter.value.get == IndexSingleColumnSelection(someValue))
  }

  test("Single column selector can be filled with json selection by name") {
    val columnSelectorParameter = SingleColumnSelectorParameter("", required = false, portIndex = 0)
    val someName = "someName"
    columnSelectorParameter.fillValueWithJson(JsObject(
      "type" -> JsString("column"),
      "value" -> JsString(someName)))
    assert(columnSelectorParameter.value.get == NameSingleColumnSelection(someName))
  }

  test("Single column selector can be filled with JsNull") {
    val columnSelectorParameter = SingleColumnSelectorParameter("", required = false, portIndex = 0)
    columnSelectorParameter.fillValueWithJson(JsNull)
    assert(columnSelectorParameter.value == None)
  }

  test("Multiple column selector can provide its json representation") {
    val description = "example selector parameter description"
    val required = false
    val columnSelectorParameter = ColumnSelectorParameter(description, required, portIndex = 2)

    val expectedFields = Map(
      "type" -> JsString("selector"),
      "description" -> JsString(description),
      "required" -> JsBoolean(required),
      "isSingle" -> JsBoolean(false),
      "portIndex" -> JsNumber(2))

    assert(columnSelectorParameter.jsDescription == expectedFields)
  }

  test("Multiple column selector can provide json representation of it's value") {
    val columnSelectorParameter = ColumnSelectorParameter("", required = false, portIndex = 0)
    columnSelectorParameter.value = Some(MultipleColumnSelection(Vector(
      NameColumnSelection(Set("abc", "def")),
      IndexColumnSelection(Set(1, 4, 7)),
      IndexRangeColumnSelection(Some(5), Some(6)),
      TypeColumnSelection(Set(ColumnType.categorical))
    ), false))

    val expectedJson = JsArray(
      JsObject(
        "type" -> JsString("columnList"),
        "values" -> JsArray(JsString("abc"), JsString("def"))),
      JsObject(
        "type" -> JsString("indexList"),
        "values" -> JsArray(JsNumber(1), JsNumber(4), JsNumber(7))
      ),
      JsObject(
        "type" -> JsString("indexRange"),
        "values" -> JsArray(JsNumber(5), JsNumber(6))
      ),
      JsObject(
        "type" -> JsString("typeList"),
        "values" -> JsArray(JsString("categorical"))
      ))

    assert(columnSelectorParameter.valueToJson == expectedJson)
  }

  test("Multiple column selector can be filled with json") {
    val columnSelectorParameter = ColumnSelectorParameter("", required = false, portIndex = 0)
    columnSelectorParameter.fillValueWithJson(JsObject(
      "selections" -> JsArray(
        JsObject(
          "type" -> JsString("columnList"),
          "values" -> JsArray(JsString("abc"), JsString("def"))),
        JsObject(
          "type" -> JsString("indexList"),
          "values" -> JsArray(JsNumber(1), JsNumber(4), JsNumber(7))
        ),
        JsObject(
          "type" -> JsString("indexRange"),
          "values" -> JsArray(JsNumber(5), JsNumber(6))
        ),
        JsObject(
          "type" -> JsString("typeList"),
          "values" -> JsArray(JsString("categorical"))
        )
      ),
      "excluding" -> JsBoolean(false)
    ))

    val expectedValue = Some(MultipleColumnSelection(Vector(
      NameColumnSelection(Set("abc", "def")),
      IndexColumnSelection(Set(1, 4, 7)),
      IndexRangeColumnSelection(Some(5), Some(6)),
      TypeColumnSelection(Set(ColumnType.categorical))
    ), false))
    assert(columnSelectorParameter.value == expectedValue)
  }

  test("Multiple column selector with excluding can be filled with json") {
    val columnSelectorParameter = ColumnSelectorParameter("", required = false, portIndex = 0)
    columnSelectorParameter.fillValueWithJson(JsObject(
      "selections" -> JsArray(
        JsObject(
          "type" -> JsString("columnList"),
          "values" -> JsArray(JsString("abc"), JsString("def"))),
        JsObject(
          "type" -> JsString("indexList"),
          "values" -> JsArray(JsNumber(1), JsNumber(4), JsNumber(7))
        ),
        JsObject(
          "type" -> JsString("indexRange"),
          "values" -> JsArray(JsNumber(5), JsNumber(6))
        ),
        JsObject(
          "type" -> JsString("typeList"),
          "values" -> JsArray(JsString("categorical"))
        )
      ),
      "excluding" -> JsBoolean(true)
    ))

    val expectedValue = Some(MultipleColumnSelection(Vector(
      NameColumnSelection(Set("abc", "def")),
      IndexColumnSelection(Set(1, 4, 7)),
      IndexRangeColumnSelection(Some(5), Some(6)),
      TypeColumnSelection(Set(ColumnType.categorical))
    ), true))
    assert(columnSelectorParameter.value == expectedValue)
  }

  test("Column selector can be filled with JsNull") {
    val columnSelectorParameter = ColumnSelectorParameter("", required = false, portIndex = 0)
    columnSelectorParameter.fillValueWithJson(JsNull)
    assert(columnSelectorParameter.value == None)
  }

  test("IndexRangeColumnSelection can be filled with an empty or too short list") {
    val columnSelectorParameter = ColumnSelectorParameter("", required = false, portIndex = 0)
    columnSelectorParameter.fillValueWithJson(JsObject(
      "selections" -> JsArray(
        JsObject(
          "type" -> JsString("indexRange"),
          "values" -> JsArray()
        ),
        JsObject(
          "type" -> JsString("indexRange"),
          "values" -> JsArray(JsNumber(1))
        )
      ),
      "excluding" -> JsBoolean(false)
    ))

    val expectedValue = Some(MultipleColumnSelection(Vector(
      IndexRangeColumnSelection(None, None),
      IndexRangeColumnSelection(Some(1), Some(1))
    ), false))
    assert(columnSelectorParameter.value == expectedValue)
  }

  test("Empty IndexRangeColumnSelection with missing should be represented as an empty array") {
    IndexRangeColumnSelection(None, None).toJson shouldBe
      JsObject(
        "type" -> JsString("indexRange"),
        "values" -> JsArray()
      )
  }

  test("IndexRangeColumnSelection can not be filled with a too long list") {
    val columnSelectorParameter = ColumnSelectorParameter("", required = false, portIndex = 0)
    a [DeserializationException] should be thrownBy {
      columnSelectorParameter.fillValueWithJson(JsArray(
        JsObject(
          "type" -> JsString("indexRange"),
          "values" -> JsArray(JsNumber(1), JsNumber(2), JsNumber(3))
        )
      ))
    }
  }

  test("SingleColumnCreator parameter can provide its json representation") {
    val description = "example single column creator parameter description"
    val default = "defaultColumnName"
    val required = true
    val singleColumnCreatorParameter = SingleColumnCreatorParameter(
      description, Some(default), required)

    val expectedFields = Map(
      "type" -> JsString("creator"),
      "description" -> JsString(description),
      "default" -> JsString(default),
      "required" -> JsBoolean(required)
    )
    assert(singleColumnCreatorParameter.jsDescription == expectedFields)
  }

  test("SingleColumnCreator parameter can provide json representation of it's value") {
    val singleColumnCreatorParameter = SingleColumnCreatorParameter(
      "", None, required = false)
    val value = "abc"
    singleColumnCreatorParameter.value = Some(value)
    assert(singleColumnCreatorParameter.valueToJson == JsString(value))
  }

  test("SingleColumnCreator parameter can be filled with json") {
    val singleColumnCreatorParameter = SingleColumnCreatorParameter(
      "", None, required = false)
    val value = "abcd"
    singleColumnCreatorParameter.fillValueWithJson(JsString(value))
    assert(singleColumnCreatorParameter.value == Some(value))
  }

  test("SingleColumnCreator parameter can be filled with JsNull") {
    val singleColumnCreatorParameter = SingleColumnCreatorParameter(
      "", None, required = false)
    singleColumnCreatorParameter.fillValueWithJson(JsNull)
    assert(singleColumnCreatorParameter.value == None)
  }

  test("MultipleColumnCreator parameter can provide its json representation") {
    val description = "example multiple column creator parameter description"
    val default = Vector("col1", "col2", "col3")
    val required = true
    val multipleColumnCreatorParameter = MultipleColumnCreatorParameter(
      description, Some(default), required)

    val expectedFields = Map(
      "type" -> JsString("multipleCreator"),
      "description" -> JsString(description),
      "default" -> JsArray(default.map(JsString(_))),
      "required" -> JsBoolean(required)
    )
    assert(multipleColumnCreatorParameter.jsDescription == expectedFields)
  }

  test("MultipleColumnCreator parameter can provide json representation of it's value") {
    val multipleColumnCreatorParameter = MultipleColumnCreatorParameter(
      "", None, required = false)
    val value = Vector("a", "b", "c")
    multipleColumnCreatorParameter.value = Some(value)
    assert(multipleColumnCreatorParameter.valueToJson == JsArray(value.map(JsString(_))))
  }

  test("MultipleColumnCreator parameter can be filled with json") {
    val multipleColumnCreatorParameter = MultipleColumnCreatorParameter(
      "", None, required = false)
    val value = Vector("a", "b", "c")
    multipleColumnCreatorParameter.fillValueWithJson(JsArray(value.map(JsString(_))))
    assert(multipleColumnCreatorParameter.value == Some(value))
  }

  test("MultipleColumnCreator parameter can be filled with JsNull") {
    val multipleColumnCreatorParameter = MultipleColumnCreatorParameter(
      "", None, required = false)
    multipleColumnCreatorParameter.fillValueWithJson(JsNull)
    assert(multipleColumnCreatorParameter.value == None)
  }

  test("PrefixBasedColumnCreator parameter can provide its json representation") {
    val description = "example prefix based column creator parameter description"
    val default = "defaultPrefix"
    val required = true
    val prefixBasedCreatorParameter = PrefixBasedColumnCreatorParameter(
      description, Some(default), required)

    val expectedFields = Map(
      "type" -> JsString("prefixBasedCreator"),
      "description" -> JsString(description),
      "default" -> JsString(default),
      "required" -> JsBoolean(required)
    )
    assert(prefixBasedCreatorParameter.jsDescription == expectedFields)
  }

  test("PrefixBasedColumnCreator parameter can provide json representation of it's value") {
    val prefixBasedCreatorParameter = PrefixBasedColumnCreatorParameter(
      "", None, required = false)
    val value = "abc"
    prefixBasedCreatorParameter.value = Some(value)
    assert(prefixBasedCreatorParameter.valueToJson == JsString(value))
  }

  test("PrefixBasedColumnCreator parameter can be filled with json") {
    val prefixBasedCreatorParameter = PrefixBasedColumnCreatorParameter(
      "", None, required = false)
    val value = "abcd"
    prefixBasedCreatorParameter.fillValueWithJson(JsString(value))
    assert(prefixBasedCreatorParameter.value == Some(value))
  }

  test("PrefixBasedColumnCreator parameter can be filled with JsNull") {
    val prefixBasedCreatorParameter = PrefixBasedColumnCreatorParameter(
      "", None, required = false)
    prefixBasedCreatorParameter.fillValueWithJson(JsNull)
    assert(prefixBasedCreatorParameter.value == None)
  }

  private def choicesAndJsFieldsForParametersWithOptions: (
      ListMap[String, ParametersSchema], JsValue) = {

    val choices = ListMap(
      "z" -> ParametersSchema(),
      "c" -> ParametersSchema(),
      "y" -> ParametersSchema(),
      "b" -> ParametersSchema(),
      "x" -> ParametersSchema(),
      "a" -> ParametersSchema()
    )
    val expectedJsDescription = JsArray(
      JsObject("name" -> JsString("z"), "schema" -> JsNull),
      JsObject("name" -> JsString("c"), "schema" -> JsNull),
      JsObject("name" -> JsString("y"), "schema" -> JsNull),
      JsObject("name" -> JsString("b"), "schema" -> JsNull),
      JsObject("name" -> JsString("x"), "schema" -> JsNull),
      JsObject("name" -> JsString("a"), "schema" -> JsNull)
    )
    (choices, expectedJsDescription)
  }
}
