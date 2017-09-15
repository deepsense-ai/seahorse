/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.services

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.utils.Logging
import io.deepsense.entitystorage.storage.EntityDao
import io.deepsense.models.entities._

class EntityService @Inject() (entityDao: EntityDao)(implicit ec: ExecutionContext)
  extends Logging {

  /**
   * Returns all saved entities of tenant.
   */
  def getAllSaved(tenantId: String): Future[List[EntityInfo]] = {
    logger.debug(s"GetAllSaved for tenantId: $tenantId")
    entityDao.getAllSaved(tenantId)
  }

  /**
   * Returns reference to a physical storage where data associated with the entity are stored.
   */
  def getEntityData(tenantId: String, entityId: Entity.Id): Future[Option[EntityWithData]] = {
    logger.debug(s"GetEntityData for tenantId $tenantId and entityId: $entityId")
    entityDao.getWithData(tenantId, entityId)
  }

  /**
   * Returns report associated with the entity.
   */
  def getEntityReport(tenantId: String, entityId: Entity.Id): Future[Option[EntityWithReport]] = {
    logger.debug(s"GetEntityReport for tenantId: $tenantId and $entityId")
    entityDao.getWithReport(tenantId, entityId)
  }

  /**
   * Creates new entity using input params.
   */
  def createEntity(entity: CreateEntityRequest): Future[Entity.Id] = {
    val entityId = Entity.Id.randomId
    logger.debug(
      s"CreateEntity - generated id: $entityId, inputEntity.name: ${entity.name}}, " +
        s"inputEntity.description: ${entity.description}")
    val now = DateTimeConverter.now
    entityDao.create(id = entityId, entity, created = now).map { _ => entityId }
  }

  /**
   * Updates selected entity with given description.
   * Returns entity with Report if updated entity exists and None otherwise.
   */
  def updateEntity(
      tenantId: String,
      entityId: Entity.Id,
      entity: UpdateEntityRequest): Future[Option[EntityWithReport]] = {

    logger.debug(
      s"UpdateEntity for tenantId: $tenantId," +
        s"userEntity.name: ${entity.name}, userEntity.description: ${entity.description}")
    val now = DateTimeConverter.now
    entityDao.update(tenantId, entityId, entity, updated = now).flatMap {
      _ => entityDao.getWithReport(tenantId, entityId)
    }
  }

  /**
   * Removes entity pointed by id or does nothing if it does not exist.
   */
  def deleteEntity(tenantId: String, entityId: Entity.Id): Future[Unit] = {
    logger.debug(s"DeleteEntity for tenantId: $tenantId and entityId: $entityId")
    entityDao.delete(tenantId, entityId)
  }
}
