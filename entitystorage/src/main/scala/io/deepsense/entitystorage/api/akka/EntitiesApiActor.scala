/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.api.akka

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.google.inject.Inject

import io.deepsense.entitystorage.services.EntityService
import io.deepsense.models.protocols.EntitiesApiActorProtocol.{Create, Get}

class EntitiesApiActor @Inject()(entityService: EntityService) extends Actor with ActorLogging {

  import context.dispatcher

  override def receive: Receive = {
    case Get(tenantId, id) => entityService.getEntityData(tenantId, id) pipeTo sender()
    case Create(entity) => entityService.createEntity(entity) pipeTo sender()
    case x => unhandled(x)
  }
}
