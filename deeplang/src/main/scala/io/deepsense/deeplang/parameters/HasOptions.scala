/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.parameters

import scala.collection.immutable.ListMap

import spray.json.DefaultJsonProtocol._
import spray.json._

/**
 * Represents Parameter with possible choices.
 */
trait HasOptions extends Parameter {
  val options: ListMap[String, ParametersSchema]

  /**
   * Validates schema assigned to chosen option.
   */
  protected def validateChoice(chosenLabel: String): Unit = {
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
