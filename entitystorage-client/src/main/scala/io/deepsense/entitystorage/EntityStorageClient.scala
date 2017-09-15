/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import io.deepsense.models.entities.{EntityWithData, Entity, EntityCreate}
import io.deepsense.models.protocols.EntitiesApiActorProtocol.{Create, Get, Request}

trait EntityStorageClient {
  def getEntityData(tenantId: String, id: Entity.Id)
                   (implicit duration: FiniteDuration): Future[Option[EntityWithData]]

  def createEntity(inputEntity: EntityCreate)
                  (implicit duration: FiniteDuration): Future[Entity.Id]
}

class ActorBasedEntityStorageClient(entitiesApiActor: ActorRef) extends EntityStorageClient {

  def getEntityData(tenantId: String, id: Entity.Id)
                   (implicit duration: FiniteDuration): Future[Option[EntityWithData]] = {
    send(Get(tenantId, id))(duration).mapTo[Option[EntityWithData]]
  }

  def createEntity(inputEntity: EntityCreate)
                  (implicit duration: FiniteDuration): Future[Entity.Id] = {
    send(Create(inputEntity))(duration).mapTo[Entity.Id]
  }

  private def send(r: Request)(implicit timeout: Timeout): Future[Any] = entitiesApiActor.ask(r)
}
