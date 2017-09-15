/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.livy

import java.util.NoSuchElementException

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.sessionmanager.service.livy.requests.Create
import io.deepsense.sessionmanager.service.livy.responses.{Batch, BatchList, BatchState}

trait LivyJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val statusFormat = new RootJsonFormat[BatchState.Value]{
    override def write(obj: BatchState.Value): JsValue = JsString(obj.toString)

    override def read(json: JsValue): BatchState.Value = {
      json match {
        case JsString(value) => try {
          BatchState.withName(value)
        } catch {
          case e: NoSuchElementException =>
            deserializationError(s"Unknown Batch status value '$value'", e)
        }
        case x => deserializationError(s"Expected 'status' of type String but got: $x")
      }
    }
  }

  implicit val createFormat = jsonFormat5(Create)
  implicit val batchFormat = jsonFormat2(Batch)
  implicit val batchListFormat = jsonFormat1(BatchList)
}
