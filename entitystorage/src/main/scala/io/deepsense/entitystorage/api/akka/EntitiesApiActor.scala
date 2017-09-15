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
import io.deepsense.entitystorage.services.EntityService
import io.deepsense.models.entities.{Entity, InputEntity}

class EntitiesApiActor @Inject()(entityService: EntityService) extends Actor with ActorLogging {

  import context.dispatcher

  override def receive: Receive = {
    case Get(tenantId, id) => entityService.getEntityData(tenantId, id) pipeTo sender()
    case Create(inputEntity) => entityService.createEntity(inputEntity) pipeTo sender()
    case x => unhandled(x)
  }
}

object EntitiesApiActor {
  sealed trait Request
  case class Get(tenantId: String, id: Entity.Id) extends Request
  case class Create(inputEntity: InputEntity) extends Request
}
