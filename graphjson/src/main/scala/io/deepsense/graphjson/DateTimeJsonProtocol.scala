/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.graphjson

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json.{DeserializationException, JsString, JsValue, JsonFormat}

object DateTimeJsonProtocol {

  implicit object DateTimeJsonFormat extends JsonFormat[DateTime] {
    val formatter = ISODateTimeFormat.dateTime()

    override def write(obj: DateTime): JsValue = {
      JsString(obj.toString(formatter))
    }

    override def read(json: JsValue): DateTime = json match {
      case JsString(value) =>
        DateTime.parse(value, formatter)
      case x => throw new DeserializationException(
        s"Expected JsString with DateTime in ISO8601 but got $x")
    }
  }
}
