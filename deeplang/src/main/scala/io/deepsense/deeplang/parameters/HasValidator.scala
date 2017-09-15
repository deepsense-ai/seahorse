/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.parameters

import spray.json.{JsValue, JsObject}

/**
 * Represents ParameterHolder with validator.
 */
trait HasValidator extends Parameter {
  val validator: Validator[HeldValue]

  override protected def validateDefined(definedValue: HeldValue) = {
    validator.validate(definedValue)
  }

  override def jsDescription: Map[String, JsValue] = {
    super.jsDescription + ("validator" -> validator.toJson)
  }
}
