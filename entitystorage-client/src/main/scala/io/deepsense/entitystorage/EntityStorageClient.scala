/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import io.deepsense.models.entities.{Entity, InputEntity}
import io.deepsense.models.protocols.EntitiesApiActorProtocol.{Create, Get, Request}

trait EntityStorageClient {
  def getEntityData(tenantId: String, id: Entity.Id)
                   (implicit duration: FiniteDuration): Future[Option[Entity]]

  def createEntity(inputEntity: InputEntity)
                  (implicit duration: FiniteDuration): Future[Entity]
}

class ActorBasedEntityStorageClient(entitiesApiActor: ActorRef) extends EntityStorageClient {

  def getEntityData(tenantId: String, id: Entity.Id)
                   (implicit duration: FiniteDuration): Future[Option[Entity]] = {
    send(Get(tenantId, id))(duration).mapTo[Option[Entity]]
  }

  def createEntity(inputEntity: InputEntity)
                  (implicit duration: FiniteDuration): Future[Entity] = {
    send(Create(inputEntity))(duration).mapTo[Entity]
  }

  private def send(r: Request)(implicit timeout: Timeout): Future[Any] = entitiesApiActor.ask(r)
}
