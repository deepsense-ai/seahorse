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

package io.deepsense.deeplang.params

import spray.json._

import io.deepsense.deeplang.parameters.AcceptAllRegexValidator
import io.deepsense.deeplang.params.exceptions.NoArgumentConstructorRequiredException

case class ClassWithParams() extends Params {
  val string = StringParam("string", "")
  val bool = BooleanParam("bool", "")

  val params = declareParams(string, bool)

  def setBool(b: Boolean): this.type = set(bool, b)
  def setString(s: String): this.type = set(string, s)
}

case class ParamsWithoutNoArgConstructor(x: String) extends Params {
  val params = declareParams()
}

class ParamsSequenceSpec
  extends AbstractParamSpec[Seq[ClassWithParams], ParamsSequence[ClassWithParams]] {

  override def className: String = "ParamsSequence"

  className should {
    "throw an exception when params don't have no-arg constructor" in {
      an [NoArgumentConstructorRequiredException] should be thrownBy
        ParamsSequence[ParamsWithoutNoArgConstructor](name = "paramsSequence", description = "")
    }
  }

  override def paramFixture: (ParamsSequence[ClassWithParams], JsValue) = {
    val paramsSequence = ParamsSequence[ClassWithParams](
      name = "Params sequence name",
      description = "Params sequence description"
    )
    val expectedJson = JsObject(
      "type" -> JsString("multiplier"),
      "name" -> JsString(paramsSequence.name),
      "description" -> JsString(paramsSequence.description),
      "values" -> JsArray(
        JsObject(
          "type" -> JsString("string"),
          "name" -> JsString("string"),
          "description" -> JsString(""),
          "validator" -> JsObject(
            "type" -> JsString("regex"),
            "configuration" -> JsObject(
              "regex" -> JsString(".*")
            )
          )
        ),
        JsObject(
          "type" -> JsString("boolean"),
          "name" -> JsString("bool"),
          "description" -> JsString("")
        )
      )
    )
    (paramsSequence, expectedJson)
  }

  override def valueFixture: (Seq[ClassWithParams], JsValue) = {
    val customParams = Seq(
      ClassWithParams().setBool(true).setString("aaa"),
      ClassWithParams().setBool(false).setString("bbb"))
    val expectedJson = JsArray(
      JsObject(
        "string" -> JsString("aaa"),
        "bool" -> JsTrue
      ),
      JsObject(
        "string" -> JsString("bbb"),
        "bool" -> JsFalse
      )
    )
    (customParams, expectedJson)
  }
}
