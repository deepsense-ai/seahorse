package io.deepsense.deeplang.parameters

import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import spray.json._

class ParametersJsonSuite extends FunSuite with MockitoSugar {

  test("ParametersSchema can provide its json representation") {
    val mockParameter1 = mock[Parameter]
    when(mockParameter1.toJson) thenReturn JsObject("mockKey1" -> JsString("mockValue1"))
    val mockParameter2 = mock[Parameter]
    when(mockParameter2.toJson) thenReturn JsObject("mockKey2" -> JsString("mockValue2"))
    val schema = ParametersSchema("x" -> mockParameter1, "y" -> mockParameter2)
    val expectedJson = JsObject("x" -> mockParameter1.toJson, "y" -> mockParameter2.toJson)
    assert(schema.toJson == expectedJson)
  }

  test("ParametersSchema can provide json representation of its values") {
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

  test("Json representation of parameter without default value provided has no 'default field") {
    val booleanParameter = BooleanParameter("description", None, required = false)
    assert(!booleanParameter.toJson.fields.contains("default"))
  }

  test("Json representation of not set parameter value is null") {
    val notFilledMockParameter = mock[Parameter]
    when(notFilledMockParameter.valueToJson) thenCallRealMethod()
    when(notFilledMockParameter.value) thenReturn None
    assert(notFilledMockParameter.valueToJson == JsNull)
  }

  test("Boolean parameter can provide its json representation") {
    val description = "example description"
    val default = true
    val booleanParameter = BooleanParameter(description, Some(default), required = false)

    val expectedJson = JsObject(
      "type" -> JsString("boolean"),
      "description" -> JsString(description),
      "default" -> JsBoolean(default),
      "required" -> JsBoolean(false))

    assert(booleanParameter.toJson == expectedJson)
  }

  test("Boolean parameter can provide json representation of it's value") {
    val booleanParameter = BooleanParameter("", None, required = false)
    val value = true
    booleanParameter.value = Some(value)
    assert(booleanParameter.valueToJson == JsBoolean(value))
  }

  test("Numeric parameter can provide its json representation") {
    val description = "example description"
    val default = 4.5
    val required = false
    val validator = RangeValidator(0.1, 100.1, beginIncluded = true, endIncluded = false, Some(0.2))
    val numericParameter = NumericParameter(description, Some(default), required, validator)

    val expectedJson = JsObject(
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

    assert(numericParameter.toJson == expectedJson)
  }

  test("Numeric parameter can provide json representation of it's value") {
    val mockValidator = mock[Validator[Double]]
    val numericParameter = NumericParameter("", None, required = false, mockValidator)
    val value = 3.14
    numericParameter.value = Some(value)
    assert(numericParameter.valueToJson == JsNumber(value))
  }

  test("String parameter can provide its json representation") {
    val description = "example string parameter description"
    val default = "default value"
    val required = true
    val validator = RegexValidator("xyz".r)
    val stringParameter = StringParameter(description, Some(default), required, validator)

    val expectedJson = JsObject(
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

    assert(stringParameter.toJson == expectedJson)
  }

  test("String parameter can provide json representation of it's value") {
    val mockValidator = mock[Validator[String]]
    val stringParameter = StringParameter("", None, required = false, mockValidator)
    val value = "abc"
    stringParameter.value = Some(value)
    assert(stringParameter.valueToJson == JsString(value))
  }

  test("Choice parameter can provide its json representation") {
    val description = "example choice parameter description"
    val default = "filledChoice"
    val required = true
    val mockParameter = mock[Parameter]
    when(mockParameter.toJson) thenReturn JsObject("mockKey" -> JsString("mockValue"))
    val filledSchema = ParametersSchema("x" -> mockParameter)
    val possibleChoices = Map("filledChoice" -> filledSchema, "emptyChoice" -> ParametersSchema())
    val choiceParameter = ChoiceParameter(description, Some(default), required, possibleChoices)

    val expectedJson = JsObject(
      "type" -> JsString("choice"),
      "description" -> JsString(description),
      "default" -> JsString(default),
      "required" -> JsBoolean(required),
      "values" -> JsObject(
        "filledChoice" -> JsObject("x" -> mockParameter.toJson),
        "emptyChoice" -> JsNull
      )
    )

    assert(choiceParameter.toJson == expectedJson)
  }

  test("Choice parameter can provide json representation of it's value") {
    val filledSchema = mock[ParametersSchema]
    when(filledSchema.valueToJson) thenReturn JsObject("x" -> JsString("y"))
    val possibleChoices = Map("filledChoice" -> filledSchema, "emptyChoice" -> ParametersSchema())
    val choiceParameter = ChoiceParameter("", None, required = false, possibleChoices)
    choiceParameter.fill("filledChoice", { schema => () })

    val expectedJson = JsObject("filledChoice" -> filledSchema.valueToJson)
    assert(choiceParameter.valueToJson == expectedJson)
  }

  test("Multiple choice parameter can provide its json representation") {
    val description = "example multiple choice parameter description"
    val default = Vector("filledChoice", "emptyChoice")
    val required = true
    val mockParameter = mock[Parameter]
    when(mockParameter.toJson) thenReturn JsObject("mockKey" -> JsString("mockValue"))
    val filledSchema = ParametersSchema("x" -> mockParameter)
    val possibleChoices = Map("filledChoice" -> filledSchema, "emptyChoice" -> ParametersSchema())
    val multipleChoiceParameter = MultipleChoiceParameter(
      description, Some(default), required, possibleChoices)

    val expectedJson = JsObject(
      "type" -> JsString("multipleChoice"),
      "description" -> JsString(description),
      "default" -> JsArray(default.map(x => JsString(x))),
      "required" -> JsBoolean(required),
      "values" -> JsObject(
        "filledChoice" -> JsObject("x" -> mockParameter.toJson),
        "emptyChoice" -> JsNull
      )
    )

    assert(multipleChoiceParameter.toJson == expectedJson)
  }

  test("Multiple choice parameter can provide json representation of it's value") {
    val filledSchema = mock[ParametersSchema]
    when(filledSchema.valueToJson) thenReturn JsObject("x" -> JsString("y"))
    val possibleChoices = Map("filledChoice" -> filledSchema, "emptyChoice" -> ParametersSchema())
    val multipleChoiceParameter = MultipleChoiceParameter(
      "", None, required = false, possibleChoices)
    multipleChoiceParameter.fill(Map(
      ("filledChoice", { schema => () }), ("emptyChoice", { schema => () })))

    val expectedJson = JsObject(
      "filledChoice" -> filledSchema.valueToJson,
      "emptyChoice" -> ParametersSchema().valueToJson)

    assert(multipleChoiceParameter.valueToJson == expectedJson)
  }

  test("Multiplier parameter can provide its json representation") {
    val description = "example multiplier parameter description"
    val required = false
    val innerSchema = mock[ParametersSchema]
    when(innerSchema.toJson) thenReturn JsObject("x" -> JsString("y"))
    val multiplierParameter = MultiplierParameter(
      description, required, innerSchema)

    val expectedJson = JsObject(
      "type" -> JsString("multiplier"),
      "description" -> JsString(description),
      "required" -> JsBoolean(required),
      "values" -> innerSchema.toJson
    )

    assert(multiplierParameter.toJson == expectedJson)
  }

  test("Multiplier parameter can provide json representation of it's value") {
    val innerSchema = mock[ParametersSchema]
    when(innerSchema.valueToJson) thenReturn JsObject("x" -> JsString("y"))
    when(innerSchema.replicate) thenReturn innerSchema

    val multiplierParameter = MultiplierParameter("", required = false, innerSchema)
    multiplierParameter.fill(List({schema => ()}))

    val expectedJson = JsArray(innerSchema.valueToJson)
    assert(multiplierParameter.valueToJson == expectedJson)
  }

  test("Single column selector can provide its json representation") {
    val description = "example single selector parameter description"
    val required = false
    val columnSelectorParameter = SingleColumnSelectorParameter(description, required)

    val expectedJson = JsObject(
      "type" -> JsString("selector"),
      "description" -> JsString(description),
      "required" -> JsBoolean(required),
      "isSingle" -> JsBoolean(true))

    assert(columnSelectorParameter.toJson == expectedJson)
  }

  test("Single column selector by index can provide json representation of it's value") {
    val columnSelectorParameter = SingleColumnSelectorParameter("", required = false)
    val value = 4
    columnSelectorParameter.value = Some(IndexSingleColumnSelection(value))

    val expectedJson = JsObject("type" -> JsString("index"), "value" -> JsNumber(value))
    assert(columnSelectorParameter.valueToJson == expectedJson)
  }

  test("Single column selector by name can provide json representation of it's value") {
    val columnSelectorParameter = SingleColumnSelectorParameter("", required = false)
    val value = "some_name"
    columnSelectorParameter.value = Some(NameSingleColumnSelection(value))

    val expectedJson = JsObject("type" -> JsString("column"), "value" -> JsString(value))
    assert(columnSelectorParameter.valueToJson == expectedJson)
  }

  test("Multiple column selector can provide its json representation") {
    val description = "example selector parameter description"
    val required = false
    val columnSelectorParameter = ColumnSelectorParameter(description, required)

    val expectedJson = JsObject(
      "type" -> JsString("selector"),
      "description" -> JsString(description),
      "required" -> JsBoolean(required),
      "isSingle" -> JsBoolean(false))

    assert(columnSelectorParameter.toJson == expectedJson)
  }

  test("Multiple column selector can provide json representation of it's value") {
    val columnSelectorParameter = ColumnSelectorParameter("", required = false)
    columnSelectorParameter.value = Some(MultipleColumnSelection(List(
      NameColumnSelection(List("abc", "def")),
      IndexColumnSelection(List(1, 4, 7)),
      RoleColumnSelection(List(ColumnRole.feature, ColumnRole.ignored)),
      TypeColumnSelection(List(ColumnType.categorical, ColumnType.ordinal))
    )))

    val expectedJson = JsArray(
      JsObject(
        "type" -> JsString("columnList"),
        "values" -> JsArray(JsString("abc"), JsString("def"))),
      JsObject(
        "type" -> JsString("indexList"),
        "values" -> JsArray(JsNumber(1), JsNumber(4), JsNumber(7))
      ),
      JsObject(
        "type" -> JsString("roleList"),
        "values" -> JsArray(JsString("feature"), JsString("ignored"))
      ),
      JsObject(
        "type" -> JsString("typeList"),
        "values" -> JsArray(JsString("categorical"), JsString("ordinal"))
      ))

    assert(columnSelectorParameter.valueToJson == expectedJson)
  }
}
