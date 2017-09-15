package io.deepsense.deeplang.parameters

import spray.json._

/**
 * Represents parameter that can provide default value.
 */
trait CanHaveDefault[T] extends Parameter {

  /** Default value of the parameter. Can be None if not provided. */
  val default: Option[T]

  override def toJson: JsObject = {
    val fields = default match {
      case Some(value) => Map("default" -> defaultValueToJson(value))
      case None => Nil
    }
    JsObject(super.toJson.fields ++ fields)
  }

  protected def defaultValueToJson(defaultValue: T): JsValue
}
