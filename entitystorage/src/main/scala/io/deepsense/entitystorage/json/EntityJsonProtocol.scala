/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.json

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.json.{DateTimeJsonProtocol, IdJsonProtocol}
import io.deepsense.models.entities._

trait EntityJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with IdJsonProtocol
  with DateTimeJsonProtocol
  with DataObjectReportJsonProtocol {

  implicit val dataObjectReferenceFormat = jsonFormat1(DataObjectReference)
  implicit val entityInfoFormat = jsonFormat8(EntityInfo)
  implicit val entityWithReportFormat = jsonFormat2(EntityWithReport)
  implicit val entityUpdateFormat = jsonFormat3(EntityUpdate.apply)
}

object EntityJsonProtocol extends EntityJsonProtocol
