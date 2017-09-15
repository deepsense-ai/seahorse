/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.parameters

import spray.json._

import io.deepsense.deeplang.parameters.ValidatorType.ValidatorType

/** Represents anything that validates parameter. */
@SerialVersionUID(1)
trait Validator[ParameterType] extends Serializable {
  val validatorType: ValidatorType

  def validate(parameter: ParameterType): Unit

  final def toJson: JsObject = {
    import DefaultJsonProtocol._
    JsObject(
      "type" -> validatorType.toString.toJson,
      "configuration" -> configurationToJson)
  }

  protected def configurationToJson: JsObject
}
