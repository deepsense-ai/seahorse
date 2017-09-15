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

import java.util.concurrent.TimeUnit

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.models.entities.Entity.Id
import io.deepsense.models.entities.{CreateEntityRequest, Entity, EntityWithData}

/**
 * Thread-safe implementation of EntityStrorageClient that DOES NOT connect to the service.
 * The client mimics the API but stores everything locally in the memory.
 */
case class EntityStorageClientInMemoryImpl(
    initState: Map[(String, Entity.Id), Entity] = Map())
  extends EntityStorageClient {

  implicit val timeout = FiniteDuration(5, TimeUnit.SECONDS)

  val storage = TrieMap[(String, Entity.Id), Entity](initState.toSeq: _*)

  override def getEntityData(tenantId: String, id: Id)
    (implicit duration: FiniteDuration): Future[Option[EntityWithData]] = {
    Future.successful(blockingGetEntityData(tenantId, id))
  }

  private def blockingGetEntityData(tenantId: String, id: Id): Option[EntityWithData] =
    storage.get((tenantId, id)).map(_.dataOnly)

  override def createEntity(createEntity: CreateEntityRequest)
    (implicit duration: FiniteDuration): Future[Entity.Id] = {
    val now = DateTimeConverter.now
    val entityId = Entity.Id.randomId
    storage.put((createEntity.tenantId, entityId), Entity(createEntity, entityId, now, now))
    Future.successful(entityId)
  }

  def getAllEntities: Seq[EntityWithData] = storage.toSeq.map {
    case (_, entity) => entity.dataOnly
  }
}
