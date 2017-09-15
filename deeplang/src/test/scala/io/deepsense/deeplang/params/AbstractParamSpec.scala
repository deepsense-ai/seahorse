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

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import spray.json.{JsObject, JsValue}

abstract class AbstractParamSpec[T, U <: Param[T]]
  extends WordSpec
  with Matchers
  with MockitoSugar {

  def className: String

  def paramFixture: (U, JsValue)  // param + its json description

  def valueFixture: (T, JsValue)  // value + its json description

  val defaultValue: T = valueFixture._1

  className should {
    "serialize itself to JSON" when {
      "default value is not provided" in {
        val (param, expectedJson) = paramFixture
        param.toJson(default = None) shouldBe expectedJson
      }
      "default value is provided" in {
        val (param, expectedJson) = paramFixture
        val expectedJsonWithDefault = JsObject(
          expectedJson.asJsObject.fields + ("default" -> param.valueToJson(defaultValue))
        )
        param.toJson(default = Some(defaultValue)) shouldBe expectedJsonWithDefault
      }
    }
  }

  it should {
    "serialize value to JSON" in {
      val param = paramFixture._1
      val (value, expectedJson) = valueFixture
      param.valueToJson(value) shouldBe expectedJson
    }
  }

  it should {
    "deserialize value from JSON" in {
      val param = paramFixture._1
      val (expectedValue, valueJson) = valueFixture
      val extractedValue = param.valueFromJson(valueJson)
      extractedValue shouldBe expectedValue
    }
  }
}
