/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage

import java.util.concurrent.TimeUnit

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import org.joda.time.DateTime

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.models.entities.Entity.Id
import io.deepsense.models.entities.{EntityWithData, Entity, EntityCreate}

case class EntityStorageClientTestInMemoryImpl(
    initState: Map[(String, Entity.Id), EntityCreate] = Map())
  extends EntityStorageClient {

  case class Record(entityCreate: EntityCreate, created: DateTime)

  implicit val timeout = FiniteDuration(5, TimeUnit.SECONDS)

  val now = DateTimeConverter.now

  val storage = TrieMap[(String, Entity.Id), Record](initState.mapValues(Record(_, now)).toSeq: _*)

  override def getEntityData(tenantId: String, id: Id)
    (implicit duration: FiniteDuration): Future[Option[EntityWithData]] = {
    Future.successful(blockingGetEntityData(tenantId, id))
  }

  private def blockingGetEntityData(tenantId: String, id: Id): Option[EntityWithData] = {
    storage.get((tenantId, id)).map { record =>
      record.entityCreate.toEntityWithData(id, record.created, record.created)
    }
  }

  override def createEntity(entity: EntityCreate)
    (implicit duration: FiniteDuration): Future[Entity.Id] = {
    val now = DateTimeConverter.now
    val entityId = Entity.Id.randomId
    storage.put((entity.tenantId, entityId), Record(entity, now))
    Future.successful(entityId)
  }

  def getAllEntities: Seq[EntityWithData] = storage.toSeq.map { case ((tenantId, id), record) =>
    record.entityCreate.toEntityWithData(id, record.created, record.created)
  }
}
