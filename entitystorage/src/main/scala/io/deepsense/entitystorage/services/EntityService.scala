/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.services

import scala.concurrent.{ExecutionContext, Future}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.entitystorage.models.{Entity, InputEntity}
import io.deepsense.entitystorage.storage.EntityDao

class EntityService(entityDao: EntityDao)(implicit ec: ExecutionContext) {

  /**
   * Returns reference to a physical storage where data associated with the entity are stored.
   * This operation is not blocking.
   */
  def getEntityData(tenantId: String, entityId: Entity.Id): Future[Option[Entity]] =
    entityDao.get(tenantId, entityId).map(_.map(_.dataOnly))

  /**
   * Returns report associated with the entity.
   * This operation is not blocking.
   */
  def getEntityReport(tenantId: String, entityId: Entity.Id): Future[Option[Entity]] =
    entityDao.get(tenantId, entityId).map(_.map(_.reportOnly))

  /**
   * Creates new entity using input params.
   * This operation is not blocking.
   */
  def createEntity(inputEntity: InputEntity): Unit = {
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
      now,
      inputEntity.saved)
    entityDao.upsert(entity)
  }
}
