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

import spray.json._

/**
 * Represents parameter that can provide default value.
 */
trait CanHaveDefault extends Parameter {

  /** Default value of the parameter. Can be None if not provided. */
  val default: Option[HeldValue]

  /** Returns default if _value is None, _value otherwise */
  override def value: Option[HeldValue] = {
    _value match {
      case Some(_) => _value
      case None => default
    }
  }

  override def jsDescription: Map[String, JsValue] = {
    val result = super.jsDescription
    default match {
      case Some(defaultValue) => result.updated("default", defaultValueToJson(defaultValue))
      case None => result
    }
  }

  protected def defaultValueToJson(defaultValue: HeldValue): JsValue
}
