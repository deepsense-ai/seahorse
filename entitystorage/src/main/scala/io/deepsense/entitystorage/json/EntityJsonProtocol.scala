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

  implicit val dataObjectReferenceFormat = jsonFormat2(DataObjectReference.apply)
  implicit val entityInfoFormat = jsonFormat8(EntityInfo.apply)
  implicit val entityWithReportFormat = jsonFormat2(EntityWithReport.apply)
  implicit val entityUpdateFormat = jsonFormat3(UpdateEntityRequest.apply)
}

object EntityJsonProtocol extends EntityJsonProtocol
