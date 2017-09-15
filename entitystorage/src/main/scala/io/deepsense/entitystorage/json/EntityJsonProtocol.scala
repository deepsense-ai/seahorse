/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.json

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.json.{DateTimeJsonProtocol, IdJsonProtocol}
import io.deepsense.entitystorage.models.{UserEntityDescription, EntityDescriptor, Entity}

trait EntityJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with NullOptions
  with IdJsonProtocol
  with DateTimeJsonProtocol
  with DataObjectJsonProtocol {

  implicit val entityFormat = jsonFormat9(Entity.apply)
  implicit val entityDescriptorFormat = jsonFormat7(EntityDescriptor.apply)
  implicit val userEntityDescriptionFormat = jsonFormat4(UserEntityDescription)
}

object EntityJsonProtocol extends EntityJsonProtocol
