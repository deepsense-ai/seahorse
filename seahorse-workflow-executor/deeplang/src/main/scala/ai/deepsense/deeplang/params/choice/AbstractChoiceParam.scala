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

import scala.reflect.runtime.universe._

import spray.json._

import ai.deepsense.deeplang.TypeUtils
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.exceptions.NoArgumentConstructorRequiredException
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

/**
 * @tparam T Type of choice items available to be chosen.
 * @tparam U Type of stored value. This can be type T or a collection of type T.
 */
abstract class AbstractChoiceParam[T <: Choice, U](implicit tag: TypeTag[T]) extends Param[U] {

  override def extraJsFields: Map[String, JsValue] = Map(
    "values" -> JsArray(choiceInstances.map(_.toJson)_: _*)
  )

  override def valueFromJson(jsValue: JsValue, graphReader: GraphReader): U = jsValue match {
    case JsObject(map) =>
      valueFromJsMap(map, graphReader)
    case _ => throw new DeserializationException(s"Cannot fill choice parameter with $jsValue:" +
      s" object expected.")
  }

  protected def valueFromJsMap(jsMap: Map[String, JsValue], graphReader: GraphReader): U

  val choiceInstances: Seq[T] = {
    val directSubclasses = tag.tpe.typeSymbol.asClass.knownDirectSubclasses
    val instances: Set[T] = for (symbol <- directSubclasses)
      yield TypeUtils.constructorForType(symbol.typeSignature, tag.mirror).getOrElse {
        throw NoArgumentConstructorRequiredException(symbol.asClass.name.decodedName.toString)
      }.newInstance().asInstanceOf[T]
    val allSubclassesDeclared =
      instances.forall(instance => instance.choiceOrder.contains(instance.getClass))
    require(allSubclassesDeclared,
      "Not all choices were declared in choiceOrder map. " +
      s"Declared: {${instances.head.choiceOrder.map(smartClassName(_)).mkString(", ")}}, " +
      s"All choices: {${instances.map(i => smartClassName(i.getClass)).mkString(", ")}}")
    instances.toList.sortBy(choice => choice.choiceOrder.indexOf(choice.getClass))
  }

  private def smartClassName[T](clazz: Class[T]) = {
    val simpleName = clazz.getSimpleName
    if (simpleName == null) clazz.getName else simpleName
  }

  protected lazy val choiceInstancesByName: Map[String, T] = choiceInstances.map {
    case choice => choice.name -> choice
  }.toMap

  protected def choiceFromJson(chosenLabel: String, jsValue: JsValue, graphReader: GraphReader): T = {
    choiceInstancesByName.get(chosenLabel) match {
      case Some(choice) => choice.setParamsFromJson(jsValue, graphReader)
      case None => throw new DeserializationException(s"Invalid choice $chosenLabel in " +
        s" choice parameter. Available choices: ${choiceInstancesByName.keys.mkString(",")}.")
    }
  }

  protected def choiceToJson(value: T): JsObject =
    JsObject(value.name -> value.paramValuesToJson)
}
