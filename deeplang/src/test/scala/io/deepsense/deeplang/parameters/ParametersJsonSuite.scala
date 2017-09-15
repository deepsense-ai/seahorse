package io.deepsense.deeplang.parameters

import org.scalatest.FunSuite
import spray.json._

import io.deepsense.deeplang.parameters.ValidatorType.ValidatorType

class ParametersJsonSuite extends FunSuite {

  case class MockParameter() extends Parameter {
    type HeldValue = Integer

    val parameterType = ParameterType.Numeric

    override val description = "Mock description"

    override val required = false

    var value: Option[Integer] = None

    def replicate = MockParameter()

    protected def definedValueToJson(definedValue: HeldValue) = JsNumber(5)
  }

  case class MockDoubleValidator() extends Validator[Double] {
    def validate(parameter: Double): Unit = ???  // not used in this suite
    protected def configurationToJson: JsObject = ???  // not used in this suite
    override val validatorType: ValidatorType = ValidatorType.Range  // not used in this suite
  }

  case class MockStringValidator() extends Validator[String] {
    def validate(parameter: String): Unit = ???  // not used in this suite
    protected def configurationToJson: JsObject = ???  // not used in this suite
    override val validatorType: ValidatorType = ValidatorType.Range  // not used in this suite
  }

  test("ParametersSchema can provide its json representation") {
    val mockParameter = MockParameter()
    val schema = ParametersSchema("x" -> mockParameter)
    val expectedJson = JsObject("x" -> mockParameter.toJson)
    assert(schema.toJson == expectedJson)
  }

  test("ParametersSchema can provide json representation of its values") {
    val notFilledMockParameter = MockParameter()

    val filledMockParameter = MockParameter()
    filledMockParameter.value = Some(3)

    val schema = ParametersSchema("x" -> notFilledMockParameter, "y" -> filledMockParameter)
    val expectedJson = JsObject(
      "x" -> notFilledMockParameter.valueToJson,
      "y" -> filledMockParameter.valueToJson)
    assert(schema.valueToJson == expectedJson)
  }

  test("Json representation of parameter without default value provided has no 'default field") {
    val booleanParameter = BooleanParameter("description", None, required = false)
    assert(!booleanParameter.toJson.fields.contains("default"))
  }

  test("Json representation of not set parameter value is null") {
    val notFilledMockParameter = MockParameter()
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
    val numericParameter = NumericParameter("", None, required = false, MockDoubleValidator())
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
    val stringParameter = StringParameter("", None, required = false, MockStringValidator())
    val value = "abc"
    stringParameter.value = Some(value)
    assert(stringParameter.valueToJson == JsString(value))
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

  test("Choice parameter can provide json representation of it's value") {
    val mockParameter = MockParameter()
    mockParameter.value = Some(10)
    val filledSchema = ParametersSchema("x" -> mockParameter)
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

  test("Multiple choice parameter can provide json representation of it's value") {
    val mockParameter = MockParameter()
    mockParameter.value = Some(10)
    val filledSchema = ParametersSchema("x" -> mockParameter)
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

  test("Multiplier parameter can provide json representation of it's value") {
    val mockParameter = MockParameter()
    val innerSchema = ParametersSchema("x" -> mockParameter)
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
