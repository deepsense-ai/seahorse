/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.json

import spray.json._

/**
 * Utility that produces JsonFormat for enumeration.
 */
object EnumerationSerializer {
  def jsonEnumFormat[T <: Enumeration](enumeration: T): JsonFormat[T#Value] = {
    new JsonFormat[T#Value] {
      def write(obj: T#Value) = JsString(obj.toString)
      def read(json: JsValue) = json match {
        case JsString(txt) => enumeration.withName(txt)
        case x => throw new DeserializationException(
          s"Expected a value from enum $enumeration instead of $x")
      }
    }
  }
}
