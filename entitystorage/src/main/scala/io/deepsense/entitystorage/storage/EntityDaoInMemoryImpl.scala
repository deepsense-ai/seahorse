/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.storage

import scala.concurrent.Future

import io.deepsense.entitystorage.models.Entity
import io.deepsense.entitystorage.models.Entity.Id

class EntityDaoInMemoryImpl extends EntityDao {

  override def getAll(tenantId: String): Future[List[Entity]] = Future.successful(List())

  override def get(tenantId: String, id: Id): Future[Option[Entity]] = Future.successful(None)
}
