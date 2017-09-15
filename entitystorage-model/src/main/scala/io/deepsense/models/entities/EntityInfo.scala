/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.entities

import org.joda.time.DateTime

/**
 * Basic information about entity.
 * @param saved Indicates if entity should be visible to end-user. This can be set to false
 *              if this entity is intermediate result of some experiment and not explicitly
 *              saved by user.
 */
case class EntityInfo(
    entityId: Entity.Id,
    tenantId: String,
    name: String,
    description: String,
    dClass: String,
    created: DateTime,
    updated: DateTime,
    saved: Boolean = true)

object EntityInfo {
  def apply(
      entity: CreateEntityRequest,
      id: Entity.Id,
      created: DateTime,
      updated: DateTime): EntityInfo =
    EntityInfo(
      id, entity.tenantId, entity.name, entity.description,
      entity.dClass, created, updated, entity.saved)
}

trait WithEntityInfo {
  val info: EntityInfo
}
