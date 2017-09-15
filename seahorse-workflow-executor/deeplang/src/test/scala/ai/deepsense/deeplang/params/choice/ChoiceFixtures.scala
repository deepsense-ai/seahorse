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

package ai.deepsense.deeplang.params.choice

import spray.json._

import ai.deepsense.deeplang.params.BooleanParam

sealed trait ChoiceABC extends Choice {
  override val choiceOrder: List[Class[_ <: ChoiceABC]] = List(
    classOf[OptionB],
    classOf[OptionC],
    classOf[OptionA])
}

case class OptionA() extends ChoiceABC {
  override val name = "A"

  val bool = BooleanParam(
    name = "bool",
    description = Some("description"))

  override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array(bool)

  def setBool(b: Boolean): this.type = set(bool, b)
}

case class OptionB() extends ChoiceABC {
  override val name = "B"
  override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array()
}

case class OptionC() extends ChoiceABC {
  override val name = "C"
  override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array()
}

sealed trait BaseChoice extends Choice {
  override val choiceOrder: List[Class[_ <: BaseChoice]] =
    List(classOf[ChoiceWithoutNoArgConstructor])
}

case class ChoiceWithoutNoArgConstructor(x: String) extends BaseChoice {
  override val name: String = "choiceWithoutNoArgConstructor"
  override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array()
}

sealed trait ChoiceWithoutDeclaration extends Choice {
  override val choiceOrder: List[Class[_ <: ChoiceWithoutDeclaration]] = List()
}

case class ChoiceWithoutDeclarationInstance() extends ChoiceWithoutDeclaration {
  override val name: String = "choiceWithoutDeclarationInstance"
  override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array()
}

object ChoiceFixtures {

  val values = "values" -> JsArray(
    JsObject(
      "name" -> JsString("B"),
      "schema" -> JsArray()
    ),
    JsObject(
      "name" -> JsString("C"),
      "schema" -> JsArray()
    ),
    JsObject(
      "name" -> JsString("A"),
      "schema" -> JsArray(
        JsObject(
          "type" -> JsString("boolean"),
          "name" -> JsString("bool"),
          "description" -> JsString("description"),
          "isGriddable" -> JsFalse,
          "default" -> JsNull
        )
      )
    )
  )
}
