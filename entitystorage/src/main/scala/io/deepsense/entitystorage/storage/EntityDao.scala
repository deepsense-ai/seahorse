/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.entitystorage.storage

import scala.concurrent.Future

import io.deepsense.models.entities.Entity

trait EntityDao {

  def get(tenantId: String, id: Entity.Id): Future[Option[Entity]]

  def getAll(tenantId: String): Future[List[Entity]]

  def upsert(entity: Entity): Future[Unit]

  def delete(tenantId: String, id: Entity.Id): Future[Unit]
}
