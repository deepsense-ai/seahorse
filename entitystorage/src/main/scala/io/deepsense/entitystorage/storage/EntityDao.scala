/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.storage

import scala.concurrent.Future

import io.deepsense.entitystorage.models.Entity

trait EntityDao {

  def getAll(tenantId: String): Future[List[Entity]]
}
