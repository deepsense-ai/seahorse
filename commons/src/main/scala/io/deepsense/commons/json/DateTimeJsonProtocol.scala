/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.commons.json

import org.joda.time.DateTime
import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.datetime.DateTimeConverter


trait DateTimeJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object DateTimeJsonFormat extends JsonFormat[DateTime] {

    override def write(obj: DateTime): JsValue = {
      JsString(DateTimeConverter.convertToString(obj))
    }

    override def read(json: JsValue): DateTime = json match {
      case JsString(value) =>
        DateTimeConverter.fromString(value)
      case x => throw new DeserializationException(
        s"Expected JsString with DateTime in ISO8601 but got $x")
    }
  }
}

object DateTimeJsonProtocol extends DateTimeJsonProtocol
