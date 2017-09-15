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

import ai.deepsense.deeplang.exceptions.DeepLangException
import ai.deepsense.deeplang.params.ParameterType
import spray.json.DefaultJsonProtocol._
import spray.json._
import scala.reflect.runtime.universe._

import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

case class MultipleChoiceParam[T <: Choice](
    override val name: String,
    override val description: Option[String])
  (implicit tag: TypeTag[T])
  extends AbstractChoiceParam[T, Set[T]] {

  override protected def serializeDefault(choices: Set[T]): JsValue =
    JsArray(choices.toSeq.map(choice => JsString(choice.name)): _*)

  val parameterType = ParameterType.MultipleChoice

  override def valueToJson(value: Set[T]): JsValue =
    value.foldLeft(JsObject())(
      (acc: JsObject, choice: T) => JsObject(acc.fields ++ choiceToJson(choice).fields))

  protected override def valueFromJsMap(jsMap: Map[String, JsValue], graphReader: GraphReader): Set[T] = {
    jsMap.toList.map {
      case (label, innerJsValue) => choiceFromJson(label, innerJsValue, graphReader)
    }.toSet
  }

  override def validate(value: Set[T]): Vector[DeepLangException] = {
    value.toVector.flatMap { _.validateParams }
  }

  override def replicate(name: String): MultipleChoiceParam[T] = copy(name = name)
}
