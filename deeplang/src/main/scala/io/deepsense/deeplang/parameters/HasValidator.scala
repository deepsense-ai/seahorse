/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import spray.json.JsObject

/**
 * Represents ParameterHolder with validator.
 */
trait HasValidator extends Parameter {
  val validator: Validator[HeldValue]

  override protected def validateDefined(definedValue: HeldValue) = {
    validator.validate(definedValue)
  }

  override def toJson: JsObject = {
    JsObject(super.toJson.fields + ("validator" -> validator.toJson))
  }
}
