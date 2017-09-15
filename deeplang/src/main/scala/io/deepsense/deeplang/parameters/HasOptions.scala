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

package io.deepsense.deeplang.parameters

import scala.collection.immutable.ListMap

import spray.json.DefaultJsonProtocol._
import spray.json._

import io.deepsense.deeplang.exceptions.DeepLangException

/**
 * Represents Parameter with possible choices.
 */
trait HasOptions extends Parameter {
  val options: ListMap[String, ParametersSchema]

  /**
   * Validates schema assigned to chosen option.
   */
  protected def validateChoice(chosenLabel: String): Vector[DeepLangException] = {
    options(chosenLabel).validate
  }

  override def jsDescription: Map[String, JsValue] = {
    val valuesField = "values" -> JsArray(options.map { case (name, schema) =>
      JsObject(
        "name" -> name.toJson,
        "schema" -> (if (schema.isEmpty) JsNull else schema.toJson)
      )
    }.toVector)
    super.jsDescription + valuesField
  }

  protected def choiceToJson(chosenLabel: String): (String, JsValue) = {
    chosenLabel -> options(chosenLabel).valueToJson
  }

  protected def choiceFromJson(chosenLabel: String, jsValue: JsValue) = {
    options.get(chosenLabel) match {
      case Some(schema) => schema.fillValuesWithJson(jsValue)
      case None => throw new DeserializationException(s"Invalid choice $chosenLabel in " +
        s"choice parameter. Available choices: ${options.keys.mkString(",")}.")
    }
  }

  protected def selectionForChoice(chosenLabel: String): Selection = {
    Selection(chosenLabel, options(chosenLabel))
  }
}
