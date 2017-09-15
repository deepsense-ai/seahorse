package io.deepsense.deeplang.parameters

import spray.json._

/**
 * Represents parameter that can provide default value.
 */
trait CanHaveDefault extends Parameter {

  /** Default value of the parameter. Can be None if not provided. */
  val default: Option[HeldValue]

  override def toJson: JsObject = {
    val result = super.toJson
    default match {
      case Some(defaultValue) => JsObject(
        result.fields.updated("default", defaultValueToJson(defaultValue)))
      case None => result
    }
  }

  protected def defaultValueToJson(defaultValue: HeldValue): JsValue
}
