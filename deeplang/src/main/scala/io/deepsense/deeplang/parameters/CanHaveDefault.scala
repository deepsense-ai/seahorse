/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.parameters

import spray.json._

/**
 * Represents parameter that can provide default value.
 */
trait CanHaveDefault extends Parameter {

  /** Default value of the parameter. Can be None if not provided. */
  val default: Option[HeldValue]

  override def jsDescription: Map[String, JsValue] = {
    val result = super.jsDescription
    default match {
      case Some(defaultValue) => result.updated("default", defaultValueToJson(defaultValue))
      case None => result
    }
  }

  protected def defaultValueToJson(defaultValue: HeldValue): JsValue
}
