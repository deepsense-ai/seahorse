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

import ai.deepsense.deeplang.params.ParameterType.ParameterType
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

/**
 * This parameter type is used to forward parameters from the DOperation to its input DOperables.
 * Value of this parameter is a Json that can be inserted
 * as values of parameters of DOperable that is used in DOperation.
 * For example, DOperation Transform dynamically renders parameters of its input Transformer.
 * @param inputPort number of port on which the DOperable is received
 */
class DynamicParam(
    override val name: String,
    override val description: Option[String],
    val inputPort: Int)
  extends Param[JsValue] {

  override protected def extraJsFields: Map[String, JsValue] = Map("inputPort" -> inputPort.toJson)

  override val parameterType: ParameterType = ParameterType.Dynamic

  override def valueToJson(value: JsValue): JsValue = value

  override def valueFromJson(jsValue: JsValue, graphReader: GraphReader): JsValue = {
    // It makes no sense to store JsNull values in DynamicParameter's value.
    // No value has the same meaning as a JsNull.
    // Storing JsNulls makes comparing DynamicParameters' values hard.
    // For example: {} and {'someParam': null} have the same meaning but they are not equal.
    // Thus, we are removing null values from the json to achieve sane equality test results.
    def removeNullValues(jsValue: JsValue): JsValue = {
      jsValue match {
        case JsObject(fields) =>
          val cleanedUpFields = fields.collect {
            case (key, value) if value != JsNull =>
              (key, removeNullValues(value))
          }
          JsObject(cleanedUpFields)
        case x => x
      }
    }
    removeNullValues(jsValue)
  }

  override def replicate(name: String): DynamicParam =
    new DynamicParam(name, description, inputPort)
}
