/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.storage

import scala.concurrent.Future

import org.joda.time.DateTime

import io.deepsense.models.entities._

trait EntityDao {

  def getWithReport(tenantId: String, id: Entity.Id): Future[Option[EntityWithReport]]

  def getWithData(tenantId: String, id: Entity.Id): Future[Option[EntityWithData]]

  def getAll(tenantId: String): Future[List[EntityInfo]]

  def create(id: Entity.Id, entity: CreateEntityRequest, created: DateTime): Future[Unit]

  def update(
      tenantId: String,
      id: Entity.Id,
      entity: UpdateEntityRequest,
      updated: DateTime): Future[Unit]

  def delete(tenantId: String, id: Entity.Id): Future[Unit]
}
