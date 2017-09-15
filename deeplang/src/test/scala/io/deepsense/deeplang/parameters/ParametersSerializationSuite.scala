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

import io.deepsense.commons.serialization.Serialization

class ParametersSerializationSuite
  extends FunSuite
  with Matchers
  with MockitoSugar
  with Serialization {

  test("ParametersSchema and its content should be serializable") {
    val param = BooleanParameter("", None)
    val schema = ParametersSchema("x" -> param)
    param.value = Some(false)
    val result = serializeDeserialize(schema)
    result shouldBe schema
    result.getBoolean("x") shouldBe param.value
  }

  test("Parameter and it's value should be serializable even after using json methods") {
    // This test was added because serialization wasn't working
    // only after using methods associated with json protocols.
    val param = BooleanParameter("", None)
    import spray.json.JsBoolean
    param.fillValueWithJson(JsBoolean(true))
    testParameterSerialization(param)
  }

  test("BooleanParameter and it's value should be serializable") {
    val param = BooleanParameter("", None)
    param.value = Some(true)
    testParameterSerialization(param)
  }

  test("NumericParameter, it's validator and it's value should be serializable") {
    val rangeValidator = RangeValidator(3, 4)
    val param = NumericParameter("", None, validator = rangeValidator)
    param.value = 1420
    testParameterSerialization(param)
  }

  test("StringParameter, it's validator and it's value should be serializable") {
    val regexValidator = RegexValidator("xxx".r)
    val param = StringParameter("", None, validator = regexValidator)
    param.value = Some("xyz")
    val result = serializeDeserialize(param)

    // Here we perform checking of RegexValidator contents equality manually,
    // because unfortunately "a".r != "a".r
    val resultValidator = result.validator.asInstanceOf[RegexValidator]
    resultValidator.regex.toString() shouldBe regexValidator.regex.toString()

    // We replace validator of result with original validator,
    // so that we can perform normal equality check on rest of the fields
    val resultWithReplacedValidator = result.copy(validator = regexValidator)
    resultWithReplacedValidator shouldBe param

    result.value shouldBe param.value
  }

  test("ChoiceParameter and it's value should be serializable") {
    val param = ChoiceParameter("", None, options = ListMap.empty)
    param.value = Some("some selection")
    testParameterSerialization(param)
  }

  test("MultipleChoiceParameter and it's value should be serializable") {
    val param = MultipleChoiceParameter("", None, options = ListMap.empty)
    param.value = Some(Seq("first selection", "second selection"))
    testParameterSerialization(param)
  }

  test("ParametersSequence and it's value should be serializable") {
    val param = ParametersSequence("", predefinedSchema = ParametersSchema())
    param.value = Some(Vector(ParametersSchema(
      "x" -> BooleanParameter("", None))))
    testParameterSerialization(param)
  }

  test("SingleColumnSelectorParameter and it's value should be serializable") {
    val param = SingleColumnSelectorParameter("", portIndex = 0)
    param.value = Some(IndexSingleColumnSelection(4))
    testParameterSerialization(param)
  }

  test("ColumnSelectorParameter and it's value should be serializable") {
    val param = ColumnSelectorParameter("", portIndex = 0)
    param.value = Some(MultipleColumnSelection(Vector(NameColumnSelection(Set("xyz"))), false))
    testParameterSerialization(param)
  }

  test("SingleColumnCreatorParameter and it's value should be serializable") {
    val param = SingleColumnCreatorParameter("", None)
    param.value = Some("abc")
    testParameterSerialization(param)
  }

  test("MultipleColumnCreatorParameter and it's value should be serializable") {
    val param = MultipleColumnCreatorParameter("", None)
    param.value = Some(Vector("a", "b", "c"))
    testParameterSerialization(param)
  }

  test("PrefixBasedColumnCreatorParameter and it's value should be serializable") {
    val param = PrefixBasedColumnCreatorParameter("", None)
    param.value = Some("customPrefix")
    testParameterSerialization(param)
  }

  test("CodeSnippetParameter, it's validator and it's value should be serializable") {
    val codeSnippetLang = new CodeSnippetLanguage(CodeSnippetLanguage.Python)
    val param = CodeSnippetParameter("", None, language = codeSnippetLang)
    param.value = Some("xyz")
    val result = serializeDeserialize(param)

    // TODO: Check if really we have to do it manually
    val resultCodeSnippetLang = result.language
    resultCodeSnippetLang shouldBe codeSnippetLang

    // We replace validator of result with original validator,
    // so that we can perform normal equality check on rest of the fields
    val resultWithReplacedValidator = result.copy(language = codeSnippetLang)
    resultWithReplacedValidator shouldBe param

    result.value shouldBe param.value
  }

  private[this] def testParameterSerialization(param: Parameter): Unit = {
    val result = serializeDeserialize(param)
    result shouldBe param
    result.value shouldBe param.value
  }
}
