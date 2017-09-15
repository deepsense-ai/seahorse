/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.services

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import org.joda.time.DateTime

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.utils.Logging
import io.deepsense.entitystorage.storage.EntityDao
import io.deepsense.models.entities._

class EntityService @Inject() (entityDao: EntityDao)(implicit ec: ExecutionContext)
  extends Logging {

  /**
   * Returns all entities of tenant.
   */
  def getAll(tenantId: String): Future[List[EntityInfo]] = {
    logger.debug("GetAll for tenantId: {}", tenantId)
    entityDao.getAll(tenantId)
  }

  /**
   * Returns reference to a physical storage where data associated with the entity are stored.
   */
  def getEntityData(tenantId: String, entityId: Entity.Id): Future[Option[EntityWithData]] = {
    logger.debug("GetEntityData for tenantId {} and entityId: {}", tenantId, entityId)
    entityDao.getWithData(tenantId, entityId)
  }

  /**
   * Returns report associated with the entity.
   */
  def getEntityReport(tenantId: String, entityId: Entity.Id): Future[Option[EntityWithReport]] = {
    logger.debug("GetEntityReport for tenantId: {} and entityId: {}", tenantId, entityId)
    entityDao.getWithReport(tenantId, entityId)
  }

  /**
   * Creates new entity using input params.
   */
  def createEntity(entity: EntityCreate): Future[Entity.Id] = {
    val entityId = Entity.Id.randomId
    logger.debug("CreateEntity - generated id: {}, inputEntity.name: {},  input.description: {}",
      entityId, entity.name, entity.description)
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
      entity: EntityUpdate): Future[Option[EntityWithReport]] = {

    logger.debug("UpdateEntity for tenantId: {}, userEntity.name: {}, userEntity.description {}",
      tenantId, entity.name, entity.description)
    val now = DateTimeConverter.now
    entityDao.update(tenantId, entityId, entity, updated = now).flatMap {
      _ => entityDao.getWithReport(tenantId, entityId)
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
