package io.deepsense.deeplang.parameters

import org.scalatest.FunSuite
import spray.json._

class ParametersJsonSuite extends FunSuite {

  case class MockParameter() extends Parameter {
    type HeldValue = Integer

    val parameterType = ParameterType.Numeric

    override val description = "Mock description"

    override val required = true

    def value = Some(5)

    def replicate = ???
  }

  test("ParametersSchema can provide its json representation") {
    val mockParameter = MockParameter()
    val schema = ParametersSchema("x" -> mockParameter)
    val expectedJson = JsObject("x" -> mockParameter.toJson)
    assert(schema.toJson == expectedJson)
  }

  test("Parameter without default value provided has null in 'default field") {
    val booleanParameter = BooleanParameter("description", None, required = false)
    assert(!booleanParameter.toJson.fields.contains("default"))
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

  test("Choice parameter can provide its json representation") {
    val description = "example choice parameter description"
    val default = "filledChoice"
    val required = true
    val mockParameter = MockParameter()
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

  test("Multiple choice parameter can provide its json representation") {
    val description = "example multiple choice parameter description"
    val default = Vector("filledChoice", "emptyChoice")
    val required = true
    val mockParameter = MockParameter()
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

  test("Multiplier parameter can provide its json representation") {
    val description = "example multiplier parameter description"
    val required = false
    val mockParameter = MockParameter()
    val innerSchema = ParametersSchema("x" -> mockParameter)
    val multiplierParameter = MultiplierParameter(
      description, required, innerSchema)

    val expectedJson = JsObject(
      "type" -> JsString("multiplier"),
      "description" -> JsString(description),
      "required" -> JsBoolean(required),
      "values" -> JsObject("x" -> mockParameter.toJson)
    )

    assert(multiplierParameter.toJson == expectedJson)
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
}
