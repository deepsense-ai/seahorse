/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.json

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.json.{DateTimeJsonProtocol, IdJsonProtocol}
import io.deepsense.models.entities.{CompactEntityDescriptor, DataObjectReference, Entity, UserEntityDescriptor}

trait EntityJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with IdJsonProtocol
  with DateTimeJsonProtocol
  with DataObjectReportJsonProtocol {

  implicit val dataObjectReferenceFormat = jsonFormat1(DataObjectReference)
  implicit val entityFormat = jsonFormat10(Entity.apply)
  implicit val entityDescriptorFormat = jsonFormat8(CompactEntityDescriptor.apply)
  implicit val userEntityDescriptorFormat = jsonFormat4(UserEntityDescriptor.apply)
}

object EntityJsonProtocol extends EntityJsonProtocol
