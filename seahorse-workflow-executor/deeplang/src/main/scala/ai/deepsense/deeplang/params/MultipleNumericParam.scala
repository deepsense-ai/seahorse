/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import ai.deepsense.deeplang.exceptions.DeepLangException
import ai.deepsense.deeplang.params.validators.{ComplexArrayValidator, Validator}
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

case class MultipleNumericParam(
    override val name: String,
    override val description: Option[String],
    validator: Validator[Array[Double]] = ComplexArrayValidator.all)
  extends Param[Array[Double]] {

  override val parameterType = ParameterType.MultipleNumeric

  override def valueToJson(value: Array[Double]): JsValue = {
    JsObject(
      "values" -> JsArray(
        JsObject(
          "type" -> JsString("seq"),
          "value" -> JsObject(
            "sequence" -> value.toJson
          )
        )
      )
    )
  }

  override def valueFromJson(jsValue: JsValue, graphReader: GraphReader): Array[Double] = jsValue match {
    case JsObject(map) =>
      map("values")
        .asInstanceOf[JsArray].elements(0)
        .asJsObject.fields("value")
        .asJsObject.fields("sequence")
        .convertTo[Array[Double]]
    case _ => throw new DeserializationException(s"Cannot fill choice parameter with $jsValue:" +
      s" object expected.")
  }

  override def replicate(name: String): MultipleNumericParam = copy(name = name)

  override protected def extraJsFields: Map[String, JsValue] =
    super.extraJsFields ++ Map("validator" -> validator.toJson)

  override def validate(values: Array[Double]): Vector[DeepLangException] = {
    validator.validate(name, values)
  }
}
