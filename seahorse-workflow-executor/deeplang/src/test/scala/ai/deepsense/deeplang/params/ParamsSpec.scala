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

import spray.json.DefaultJsonProtocol._
import spray.json._

import ai.deepsense.deeplang.UnitSpec
import ai.deepsense.deeplang.doperations.inout.InputStorageTypeChoice.File
import ai.deepsense.deeplang.exceptions.DeepLangException
import ai.deepsense.deeplang.params.ParameterType._
import ai.deepsense.deeplang.params.choice.{Choice, ChoiceParam}
import ai.deepsense.deeplang.params.exceptions.ParamValueNotProvidedException
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

class ParamsSpec extends UnitSpec {
  import ParamsSpec._
  val graphReader = mock[GraphReader]
  "Class with Params" should {

    "return array of its params ordered asc by index" in {
      val p = WithParams()
      p.params should contain theSameElementsInOrderAs Seq(p.param2, p.param1)
    }
    "validate its params" in {
      val p = WithParams()
      p.set1(4)
      p.validateParams should contain theSameElementsAs
        new ParamValueNotProvidedException(nameOfParam2) +: p.param1.validate(4)
    }
    "describe its params as json ordered as in params Array()" in {
      val p = WithParams()
      p.paramsToJson shouldBe JsArray(
        p.param2.toJson(maybeDefault = None),
        p.param1.toJson(maybeDefault = Some(defaultForParam1))
      )
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
        ), graphReader)
        p.get1 shouldBe 5
        p.get2 shouldBe 6
      }
      "json is null" in {
        val p = WithParams()
        p.set1(4)
        p.setParamsFromJson(JsNull, graphReader)
        p.get1 shouldBe 4
      }
      "value of some param is JsNull" in {
        val p = WithParams()
        p.set1(4)
        p.setParamsFromJson(JsObject(
          p.param1.name -> JsNull,
          p.param2.name -> 6.toJson
        ), graphReader)
        p.get1 shouldBe defaultForParam1
        p.get2 shouldBe 6
      }
      "value of some param that doesn't have default is JsNull" in {
        val p = WithParams()
        p.set2(17)
        p.setParamsFromJson(JsObject(
          p.param2.name -> JsNull
        ), graphReader)
        p.is2Defined shouldBe false
      }
      "ignoreNulls is set" in {
        val p = WithParams()
        p.set1(4)
        p.setParamsFromJson(JsObject(
          p.param1.name -> JsNull,
          p.param2.name -> 6.toJson
        ), graphReader, ignoreNulls = true)
        p.get1 shouldBe 4
        p.get2 shouldBe 6
      }
    }
    "throw Deserialization exception" when {
      "unknown param name is used it should be ignored" in {
        val p = WithParams()
        p.setParamsFromJson(JsObject(
          "unknownName" -> 5.toJson,
          p.param2.name -> 6.toJson), graphReader)
        p.get1 shouldBe defaultForParam1
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
        val p = WithParams()
        val exception = intercept [ParamValueNotProvidedException] {
          p.get2
        }
        exception.name shouldBe p.param2.name
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
  "Params.sameAs" should {
    val valueOne = 100
    val valueTwo = 5
    "return true" when {
      "classes of both objects are the same and object have" +
        " the same parameters with the same values" in {
        val withParamsA1 = new WithParamsA
        val withParamsA2 = new WithParamsA
        withParamsA1.sameAs(withParamsA2) shouldBe true

        withParamsA1.set1(valueOne)
        withParamsA2.set1(valueOne)
        withParamsA1.sameAs(withParamsA2) shouldBe true

        withParamsA1.set2(valueTwo)
        withParamsA2.set2(valueTwo)
        withParamsA1.sameAs(withParamsA2) shouldBe true
      }
      "comparing replicated Params" in {
        val withParamsA1 = new WithParamsA
        val withParamsA2 = withParamsA1.replicate()
        withParamsA1.eq(withParamsA2) shouldBe false
        withParamsA1.sameAs(withParamsA2) shouldBe true
      }
    }
    "return false" when {
      "classes of both objects are different" in {
        val withParamsA = new WithParamsA
        val withParamsB = new WithParamsB
        withParamsA.sameAs(withParamsB) shouldBe false
      }
      "parameters have different values" in {
        val file1 = new File()
        val file2 = new File()
        file1.setSourceFile("path")

        file1.sameAs(file2) shouldBe false

        file1.setSourceFile("path1")
        file2.setSourceFile("path2")

        file1.sameAs(file2) shouldBe false
      }
    }
  }
}

object ParamsSpec extends UnitSpec {
  case class MockException(override val message: String) extends DeepLangException(message)

  case class MockParam(name: String) extends Param[Int] {
    override val description: Option[String] = Some("description")
    override val parameterType: ParameterType = mock[ParameterType]

    override def valueToJson(value: Int): JsValue = value.toJson
    override def valueFromJson(jsValue: JsValue, graphReader: GraphReader): Int = jsValue.convertTo[Int]

    override def validate(value: Int): Vector[DeepLangException] = Vector(MockException(name))

    override def replicate(name: String): MockParam = copy(name = name)
  }

  val defaultForParam1 = 1
  val nameOfParam2 = "name of param2"

  // This class also shows how Params trait is to be used
  case class WithParams() extends Params {
    val param2 = MockParam(nameOfParam2)
    val param1 = MockParam("name of param1")

    val params: Array[Param[_]] = Array(param2, param1)

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

  class WithParamsA extends WithParams
  class WithParamsB extends WithParams

  case class ParamsWithChoice() extends Params {
    val choiceParam = ChoiceParam[ChoiceWithRepeatedParameter](
      name = "choice",
      description = Some("choice"))

    def setChoice(v: ChoiceWithRepeatedParameter): this.type = set(choiceParam, v)

    override val params: Array[Param[_]] = Array(choiceParam)
  }

  sealed trait ChoiceWithRepeatedParameter extends Choice {
    override val choiceOrder: List[Class[_ <: ChoiceWithRepeatedParameter]] = List(
      classOf[ChoiceOne],
      classOf[ChoiceTwo])
  }

  case class ChoiceOne() extends ChoiceWithRepeatedParameter {
    override val name = "one"

    val numericParam = NumericParam(
      name = "x",
      description = Some("numericParam"))

    override val params: Array[Param[_]] = Array(numericParam)
  }

  case class ChoiceTwo() extends ChoiceWithRepeatedParameter {
    override val name = "two"

    val numericParam = NumericParam(
      name = "x",
      description = Some("numericParam"))

    override val params: Array[Param[_]] = Array(numericParam)
  }

  object DeclareParamsFixtures {
    val outsideParam = MockParam("outside name")

    class ParamsFromOutside extends Params {
      val param = MockParam("name")
      val params: Array[Param[_]] = Array(outsideParam, param)
    }

    class ParamsWithNotUniqueNames extends Params {
      val param1 = MockParam("some name")
      val param2 = MockParam(param1.name)
      val params: Array[Param[_]] = Array(param1, param2)
    }

    class NotAllParamsDeclared extends Params {
      val param1 = MockParam("some name")
      val param2 = MockParam("some other name")
      val params: Array[Param[_]] = Array(param1)
    }

    class ParamsRepeated extends Params {
      val param1 = MockParam("some name")
      val param2 = MockParam("some other name")
      val params: Array[Param[_]] = Array(param1, param2, param1)
    }
  }
}
