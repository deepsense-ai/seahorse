/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.entitystorage.services

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.utils.Logging
import io.deepsense.entitystorage.storage.EntityDao
import io.deepsense.models.entities.{Entity, InputEntity, UserEntityDescriptor}

class EntityService @Inject() (entityDao: EntityDao)(implicit ec: ExecutionContext)
  extends Logging {

  /**
   * Returns all entities of tenant.
   */
  def getAll(tenantId: String): Future[List[Entity]] = {
    logger.debug("GetAll for tenantId: {}", tenantId)
    entityDao.getAll(tenantId)
  }

  /**
   * Returns reference to a physical storage where data associated with the entity are stored.
   */
  def getEntityData(tenantId: String, entityId: Entity.Id): Future[Option[Entity]] = {
    logger.debug("GetEntityData for tenantId {} and entityId: {}", tenantId, entityId)
    entityDao.get(tenantId, entityId).map(_.map(_.dataOnly))
  }

  /**
   * Returns report associated with the entity.
   */
  def getEntityReport(tenantId: String, entityId: Entity.Id): Future[Option[Entity]] = {
    logger.debug("GetEntityReport for tenantId: {} and entityId: {}", tenantId, entityId)
    entityDao.get(tenantId, entityId).map(_.map(_.reportOnly))
  }

  /**
   * Creates new entity using input params.
   */
  def createEntity(inputEntity: InputEntity): Future[Entity] = {
    val entityId = Entity.Id.randomId
    logger.debug("CreateEntity - generated id: {}, inputEntity.name: {},  input.description: {}",
      entityId, inputEntity.name, inputEntity.description)
    val now = DateTimeConverter.now
    val entity = Entity(
      inputEntity.tenantId,
      entityId,
      inputEntity.name,
      inputEntity.description,
      inputEntity.dClass,
      inputEntity.data,
      inputEntity.report,
      now,
      now,
      inputEntity.saved)
    entityDao.upsert(entity).map(_ => entity)
  }

  /**
   * Updates selected entity with given description.
   * Returns entity with Report if updated entity exists and None otherwise.
   */
  def updateEntity(tenantId: String, userEntity: UserEntityDescriptor): Future[Option[Entity]] = {
    logger.debug("UpdateEntity for tenantId: {}, userEntity.name: {}, userEntity.description {}",
      tenantId, userEntity.name, userEntity.description)
    entityDao.get(tenantId, userEntity.id).flatMap {
      case Some(oldEntity) =>
        val newEntity = oldEntity.updateWith(userEntity)
        entityDao.upsert(newEntity).map(_ => Some(newEntity.reportOnly))
      case None => Future.successful(None)
    }
  }

  /**
   * Removes entity pointed by id or does nothing if it does not exist.
   */
  def deleteEntity(tenantId: String, entityId: Entity.Id): Future[Unit] = {
    logger.debug("DeleteEntity for tenantId: {} and entityId: {}", tenantId, entityId)
    entityDao.delete(tenantId, entityId)
  }
}
