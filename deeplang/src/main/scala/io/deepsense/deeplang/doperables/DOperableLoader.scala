/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import scala.concurrent.Await

import io.deepsense.entitystorage.EntityStorageClient
import io.deepsense.models.entities.Entity

trait DOperableLoader {

  def load[T](
      entityStorageClient: EntityStorageClient)(
      loader: (String => T))(
      tenantId: String,
      id: Entity.Id): T = {
    import scala.concurrent.duration._
    // TODO: duration from configuration (and possibly a little longer timeout)
    implicit val timeout = 5.seconds
    val entityF = entityStorageClient.getEntityData(tenantId, id)
    val entity = Await.result(entityF, timeout).get
    loader(entity.data.get.url)
  }
}

object DOperableLoader extends DOperableLoader
