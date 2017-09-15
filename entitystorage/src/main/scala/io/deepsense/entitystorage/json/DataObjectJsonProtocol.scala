/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.json

import com.sun.xml.internal.ws.encoding.soap.DeserializationException
import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.deeplang.doperables.Report
import io.deepsense.entitystorage.models.{DataObject, DataObjectReference, DataObjectReport}


trait DataObjectJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val dataObjectReference = jsonFormat1(DataObjectReference)

  implicit object DataObjectJsonFormat extends JsonFormat[DataObject] {

    override def write(dataObject: DataObject): JsValue = dataObject match {
      case reference: DataObjectReference => reference.toJson
      case report: DataObjectReport => JsObject()
      case _ => throw new DeserializationException(s"Could not serialize object $dataObject")
    }

    override def read(json: JsValue): DataObject = json match {
      case JsObject(fields) =>
        val urlKey: String = "url"
        if (fields.contains(urlKey) && fields.size == 1) {
          DataObjectReference(fields(urlKey).convertTo[String])
        } else {
          DataObjectReport(Report())
        }
      case _ => throw new DeserializationException(s"Could not deserialize object $json")
    }
  }
}

object DataObjectJsonProtocol extends DataObjectJsonProtocol
