/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.protocols

import io.deepsense.models.entities.{Entity, EntityCreate}

object EntitiesApiActorProtocol {
  sealed trait Request
  case class Get(tenantId: String, id: Entity.Id) extends Request
  case class Create(entity: EntityCreate) extends Request
}
