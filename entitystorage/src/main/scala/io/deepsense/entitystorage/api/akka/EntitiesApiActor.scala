/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.api.akka

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.google.inject.Inject

import io.deepsense.entitystorage.api.akka.EntitiesApiActor.Get
import io.deepsense.entitystorage.models.Entity
import io.deepsense.entitystorage.storage.EntityDao

class EntitiesApiActor @Inject()(entityDao: EntityDao) extends Actor with ActorLogging {

  import context.dispatcher

  override def receive: Receive = {
    case Get(tenantId, id) => sendEntity(tenantId, id)
    case x => unhandled(x)
  }

  private def sendEntity(tenantId: String, id: Entity.Id): Unit =
    entityDao.get(tenantId, id) pipeTo sender()
}

object EntitiesApiActor {
  case class Get(tenantId: String, id: Entity.Id)
}
