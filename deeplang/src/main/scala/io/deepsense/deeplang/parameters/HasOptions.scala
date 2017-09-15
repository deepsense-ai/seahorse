/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters

import spray.json._

/**
 * Represents Parameter with possible choices.
 */
trait HasOptions extends Parameter {
  val options: Map[String, ParametersSchema]

  /**
   * Validates schema assigned to chosen option.
   */
  protected def validateChoice(chosenLabel: String): Unit = {
    options(chosenLabel).validate
  }

  override def toJson: JsObject = {
    val valuesField = "values" -> JsObject(options.mapValues({ option =>
      if (option.isEmpty) JsNull else option.toJson}))
    JsObject(super.toJson.fields + valuesField)
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
