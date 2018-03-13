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

import java.lang.reflect.Constructor

import scala.reflect.runtime.universe._

import spray.json._

import ai.deepsense.deeplang.TypeUtils
import ai.deepsense.deeplang.exceptions.DeepLangException
import ai.deepsense.deeplang.params.exceptions.NoArgumentConstructorRequiredException
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

case class ParamsSequence[T <: Params](
    override val name: String,
    override val description: Option[String])
    (implicit tag: TypeTag[T])
  extends Param[Seq[T]] {

  val parameterType = ParameterType.Multiplier

  override def valueToJson(value: Seq[T]): JsValue = {
    val cells = for (params <- value) yield params.paramValuesToJson
    JsArray(cells: _*)
  }

  private val constructor: Constructor[_] = TypeUtils.constructorForTypeTag(tag).getOrElse {
    throw NoArgumentConstructorRequiredException(tag.tpe.typeSymbol.asClass.name.decodedName.toString)
  }

  private def innerParamsInstance: T = constructor.newInstance().asInstanceOf[T]

  override def valueFromJson(jsValue: JsValue, graphReader: GraphReader): Seq[T] = jsValue match {
    case JsArray(vector) =>
      for (innerJsValue <- vector) yield {
        innerParamsInstance.setParamsFromJson(innerJsValue, graphReader)
      }
    case _ => throw new DeserializationException(s"Cannot fill parameters sequence" +
      s"with $jsValue: array expected.")
  }

  override def extraJsFields: Map[String, JsValue] = Map(
    "values" -> innerParamsInstance.paramsToJson
  )

  override def replicate(name: String): ParamsSequence[T] = copy(name = name)

  override def validate(value: Seq[T]): Vector[DeepLangException] = {
    value.flatMap(_.validateParams).toVector
  }
}
