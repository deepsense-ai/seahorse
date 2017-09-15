/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 */

package io.deepsense.entitystorage

import java.util.concurrent.TimeUnit

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.models.entities.Entity.Id
import io.deepsense.models.entities.{Entity, InputEntity}

case class EntityStorageClientTestInMemoryImpl(initState: Map[(String, Entity.Id), Entity] = Map())
  extends EntityStorageClient {

  implicit val timeout = FiniteDuration(5, TimeUnit.SECONDS)

  val storage = TrieMap[(String, Entity.Id), Entity](initState.toSeq:_*)

  override def getEntityData(tenantId: String, id: Id)
    (implicit duration: FiniteDuration): Future[Option[Entity]] = {
    Future.successful(storage.get((tenantId, id)))
  }

  override def createEntity(inputEntity: InputEntity)
    (implicit duration: FiniteDuration): Future[Entity] = {
    val now = DateTimeConverter.now
    val entity = Entity(
      inputEntity.tenantId,
      Entity.Id.randomId,
      inputEntity.name,
      inputEntity.description,
      inputEntity.dClass,
      inputEntity.data,
      inputEntity.report,
      now,
      now)
    storage.put((inputEntity.tenantId, entity.id), entity)
    Future.successful(entity)
  }

  def getAllEntities: Seq[Entity] = storage.values.toSeq
}
