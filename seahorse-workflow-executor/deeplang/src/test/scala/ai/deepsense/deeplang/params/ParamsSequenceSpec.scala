/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.params

import spray.json._
import ai.deepsense.deeplang.params.exceptions.NoArgumentConstructorRequiredException
import ai.deepsense.deeplang.params.validators.AcceptAllRegexValidator

case class ClassWithParams() extends Params {
  val string = StringParam("string", None)
  val bool = BooleanParam("bool", None)

  val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array(string, bool)

  def setBool(b: Boolean): this.type = set(bool, b)
  def setString(s: String): this.type = set(string, s)
}

case class ParamsWithoutNoArgConstructor(x: String) extends Params {
  val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array()
}

class ParamsSequenceSpec
  extends AbstractParamSpec[Seq[ClassWithParams], ParamsSequence[ClassWithParams]] {

  override def className: String = "ParamsSequence"

  className should {
    "throw an exception when params don't have no-arg constructor" in {
      an [NoArgumentConstructorRequiredException] should be thrownBy
        ParamsSequence[ParamsWithoutNoArgConstructor](name = "paramsSequence", description = None)
    }
  }

  override def paramFixture: (ParamsSequence[ClassWithParams], JsValue) = {
    val description = "Params sequence description"
    val paramsSequence = ParamsSequence[ClassWithParams](
      name = "Params sequence name",
      description = Some(description)
    )
    val expectedJson = JsObject(
      "type" -> JsString("multiplier"),
      "name" -> JsString(paramsSequence.name),
      "description" -> JsString(description),
      "default" -> JsNull,
      "isGriddable" -> JsFalse,
      "values" -> JsArray(
        JsObject(
          "type" -> JsString("string"),
          "name" -> JsString("string"),
          "description" -> JsString(""),
          "default" -> JsNull,
          "isGriddable" -> JsFalse,
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
          "description" -> JsString(""),
          "isGriddable" -> JsFalse,
          "default" -> JsNull
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
