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

import spray.json.JsValue

import ai.deepsense.deeplang.UnitSpec
import ParameterType.ParameterType
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

class ParamMapSpec extends UnitSpec {

  class MockParam[T] extends Param[T] {
    // cannot use mockito, because asInstanceOf[Any] won't work
    override val name: String = "name"
    override val description: Option[String] = Some("description")
    override val parameterType: ParameterType = mock[ParameterType]

    override def valueToJson(value: T): JsValue = ???
    override def valueFromJson(jsValue: JsValue, graphReader: GraphReader): T = ???

    override def replicate(name: String): MockParam[T] = new MockParam[T]
  }

  val intParam = new MockParam[Int]
  val intValue = 5
  val intParamPair = ParamPair(intParam, intValue)

  val stringParam = new MockParam[String]
  val stringValue = "abc"
  val stringParamPair = ParamPair(stringParam, stringValue)

  val paramPairs = Seq(intParamPair, stringParamPair)

  "ParamMap" can {
    "be built from ParamPairs" in {
      val paramMap = ParamMap(paramPairs: _*)
      ParamMap().put(stringParamPair, intParamPair) shouldBe paramMap
      ParamMap().put(intParam, intValue).put(stringParam, stringValue) shouldBe paramMap
    }
    "be merged with other param map" in {
      val map1 = ParamMap(paramPairs(0))
      val map2 = ParamMap(paramPairs(1))
      map1 ++ map2 shouldBe ParamMap(paramPairs: _*)
    }
    "be updated with other param map" in {
      val map1 = ParamMap(paramPairs(0))
      val map2 = ParamMap(paramPairs(1))
      map1 ++= map2
      map1 shouldBe ParamMap(paramPairs: _*)
    }
    "return sequence of included ParamPairs" in {
      val paramMap = ParamMap(paramPairs: _*)
      paramMap.toSeq should contain theSameElementsAs paramPairs
    }
  }

  private def mapWithIntParam = ParamMap(intParamPair)

  "ParamMap.put" should {
    "update value of param if it is already defined" in {
      val map = mapWithIntParam
      val newValue = 7
      map.put(intParam, newValue)
      map shouldBe ParamMap().put(intParam, newValue)
    }
  }
  "ParamMap.get" should {
    "return Some value" when {
      "param has value assigned" in {
        mapWithIntParam.get(intParam) shouldBe Some(intValue)
      }
    }
    "return None" when {
      "param has no value assigned" in {
        mapWithIntParam.get(stringParam) shouldBe None
      }
    }
  }

  "ParamMap.getOrElse" should {
    "return value" when {
      "param has value assigned" in {
        mapWithIntParam.getOrElse(intParam, 7) shouldBe intValue
      }
    }
    "return provided default" when {
      "param has no value assigned" in {
        val default = "xxx"
        mapWithIntParam.getOrElse(stringParam, default) shouldBe default
      }
    }
  }

  "ParamMap.apply" should {
    "return value" when {
      "param has value assigned" in {
        mapWithIntParam(intParam) shouldBe intValue
      }
    }
    "throw an Exception" when {
      "param has no value assigned" in {
        a [NoSuchElementException] shouldBe thrownBy {
          mapWithIntParam(stringParam)
        }
      }
    }
  }

  "ParamMap.contains" should {
    "return true" when {
      "param has value assigned" in {
        mapWithIntParam.contains(intParam) shouldBe true
      }
    }
    "return false" when {
      "param has no value assigned" in {
        mapWithIntParam.contains(stringParam) shouldBe false
      }
    }
  }

  "ParamMap.remove" should {
    "remove value and return it" when {
      "param has value assigned" in {
        val map = mapWithIntParam
        map.remove(intParam) shouldBe Some(intValue)
        map shouldBe ParamMap.empty
      }
    }
    "return None" when {
      "param has no value assigned" in {
        mapWithIntParam.remove(stringParam) shouldBe None
      }
    }
  }
}
