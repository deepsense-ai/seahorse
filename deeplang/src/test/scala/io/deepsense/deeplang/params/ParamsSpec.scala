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
import spray.json.DefaultJsonProtocol._

import io.deepsense.deeplang.UnitSpec
import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.parameters.ParameterType._

class ParamsSpec extends UnitSpec {

  case class MockException(override val message: String) extends DeepLangException(message)

  case class MockParam(name: String, override val index: Int = 0) extends Param[Int] {
    override val description: String = "description"
    override val parameterType: ParameterType = mock[ParameterType]

    override def toJson: JsObject = JsObject("name" -> name.toJson)

    override def valueToJson(value: Int): JsValue = value.toJson
    override def valueFromJson(jsValue: JsValue): Int = jsValue.convertTo[Int]

    override def validate(value: Int): Vector[DeepLangException] = Vector(MockException(name))
  }

  val defaultForParam1 = 1

  // This class also shows how Params trait is to be used
  case class WithParams() extends Params {
    val param2 = MockParam("name of param2", index = 1)
    val param1 = MockParam("name of param1", index = 2)

    def set1(v: Int): this.type = set(param1 -> v)
    def set2(v: Int): this.type = set(param2 -> v)

    def get1: Int = $(param1)
    def get2: Int = $(param2)

    def is1Set: Boolean = isSet(param1)

    def is1Defined: Boolean = isDefined(param1)
    def is2Defined: Boolean = isDefined(param2)

    def clear1: this.type = clear(param1)

    setDefault(param1 -> defaultForParam1)
  }

  "Class with Params" should {

    "return array of its params ordered asc by index" in {
      val p = WithParams()
      p.params should contain theSameElementsInOrderAs Seq(p.param2, p.param1)
    }
    "validate its params" in {
      val p = WithParams()
      p.set1(4)
      p.validateParams() should contain theSameElementsAs Seq(p.param1.validate(4)).flatten
    }
    "describe its params as json ordered asc by index" in {
      val p = WithParams()
      p.paramsToJson shouldBe JsArray(p.param2.toJson, p.param1.toJson)
    }
    "describe values of its params as json" in {
      val p = WithParams()
      p.set1(4)
      p.paramValuesToJson shouldBe JsObject(
        p.param1.name -> 4.toJson
      )
    }
    "set values of its params from json" when {
      "some params get overwritten" in {
        val p = WithParams()
        p.set1(4)
        p.setParamsFromJson(JsObject(
          p.param1.name -> 5.toJson,
          p.param2.name -> 6.toJson
        ))
        p.get1 shouldBe 5
        p.get2 shouldBe 6
      }
      "json is null" in {
        val p = WithParams()
        p.set1(4)
        p.setParamsFromJson(JsNull)
        p.get1 shouldBe 4
      }
      "value of some param is JsNull" in {
        val p = WithParams()
        p.set1(4)
        p.setParamsFromJson(JsObject(
          p.param1.name -> JsNull,
          p.param2.name -> 6.toJson
        ))
        p.get1 shouldBe 4
        p.get2 shouldBe 6
      }
    }
    "throw Deserialization exception" when {
      "unknown param name is used" in {
        val p = WithParams()
        a [DeserializationException] shouldBe thrownBy {
          p.setParamsFromJson(JsObject(
            "unknownName" -> 5.toJson,
            p.param2.name -> 6.toJson
          ))
        }
      }
    }
  }
  "Params.isSet" should {
    "return true" when {
      "param value is set" in {
        val p = WithParams()
        p.set1(3)
        p.is1Set shouldBe true
      }
    }
    "return false" when {
      "param value is not set" in {
        val p = WithParams()
        p.is1Set shouldBe false
      }
    }
  }
  "Params.isDefined" should {
    "return true" when {
      "param value is set" in {
        val p = WithParams()
        p.set1(3)
        p.is1Defined shouldBe true
      }
      "param value is not set, but default value is" in {
        val p = WithParams()
        p.is1Defined shouldBe true
      }
    }
    "return false" when {
      "neither value nor default value is set" in {
        val p = WithParams()
        p.is2Defined shouldBe false
      }
    }
  }
  "Params.clear" should {
    "clear param value" in {
      val p = WithParams()
      p.set1(3)
      p.clear1
      p.is1Set shouldBe false
    }
  }
  "Params.get" should {
    "return Some param value" when {
      "it is defined" in {
        val p = WithParams()
        p.set1(3)
        p.get1 shouldBe 3
      }
    }
    "return None" when {
      "param value is not defined" in {

      }
    }
  }
  "Params.$" should {
    "return param value" when {
      "it is defined" in {
        val p = WithParams()
        p.set1(3)
        p.get1 shouldBe 3
      }
    }
    "return default value" when {
      "value is not defined, but default is" in {
        val p = WithParams()
        p.get1 shouldBe defaultForParam1
      }
    }
    "throw exception" when {
      "neither param value nor default is defined" in {
        an [Exception] shouldBe thrownBy {
          val p = WithParams()
          p.get2
        }
      }
    }
  }
  "Params.extractParamMap" should {
    "return enriched paramMap" in {
      val p = WithParams()
      p.set2(8)
      val extraParam = MockParam("c")
      val extraPair = ParamPair(extraParam, 9)
      val extraMap = ParamMap(extraPair)
      p.extractParamMap(extraMap) shouldBe ParamMap(
        ParamPair(p.param1, defaultForParam1),
        ParamPair(p.param2, 8),
        extraPair
      )
    }
  }
}
