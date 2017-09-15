/**
 * Copyright 2015, CodiLime Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.entitystorage

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import io.deepsense.models.entities.{EntityWithData, Entity, CreateEntityRequest}
import io.deepsense.models.protocols.EntitiesApiActorProtocol.{Create, Get, Request}

trait EntityStorageClient {
  def getEntityData(tenantId: String, id: Entity.Id)
                   (implicit duration: FiniteDuration): Future[Option[EntityWithData]]

  def createEntity(inputEntity: CreateEntityRequest)
                  (implicit duration: FiniteDuration): Future[Entity.Id]
}

class ActorBasedEntityStorageClient(entitiesApiActor: ActorRef) extends EntityStorageClient {

  def getEntityData(tenantId: String, id: Entity.Id)
                   (implicit duration: FiniteDuration): Future[Option[EntityWithData]] = {
    send(Get(tenantId, id))(duration).mapTo[Option[EntityWithData]]
  }

  def createEntity(inputEntity: CreateEntityRequest)
                  (implicit duration: FiniteDuration): Future[Entity.Id] = {
    send(Create(inputEntity))(duration).mapTo[Entity.Id]
  }

  private def send(r: Request)(implicit timeout: Timeout): Future[Any] = entitiesApiActor.ask(r)
}
