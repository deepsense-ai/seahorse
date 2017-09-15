/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.api.akka

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.google.inject.Inject

import io.deepsense.entitystorage.api.akka.EntitiesApiActor.{Create, Get}
import io.deepsense.entitystorage.models.{Entity, InputEntity}
import io.deepsense.entitystorage.services.EntityService

class EntitiesApiActor @Inject()(entityService: EntityService) extends Actor with ActorLogging {

  import context.dispatcher

  override def receive: Receive = {
    case Get(tenantId, id) => entityService.getEntityData(tenantId, id) pipeTo sender()
    case Create(inputEntity) => entityService.createEntity(inputEntity)
    case x => unhandled(x)
  }
}

object EntitiesApiActor {
  case class Get(tenantId: String, id: Entity.Id)
  case class Create(inputEntity: InputEntity)
}
