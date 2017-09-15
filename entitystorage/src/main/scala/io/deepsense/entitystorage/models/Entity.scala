/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.models

import org.joda.time.DateTime

import io.deepsense.commons.models

/**
 * Main concept of the entity storage.
 */
case class Entity (
  id: Entity.Id,
  name: String,
  description: String,
  dClass: String,
  created: DateTime,
  updated: DateTime,
  data: DataObject,
  saved: Boolean = true
) {
    if (dClass == DataObjectReport.getClass.getName) {
        require(data.isInstanceOf[DataObjectReport])
    }
}

object Entity {
  type Id = models.Id

  object Id {
    def randomId = models.Id.randomId
  }
}

/**
 * Shorter Entity description used for list presentation
 */
case class EntityDescriptor(
  id: Entity.Id,
  name: String,
  description: String,
  dClass: String,
  saved: Boolean = true,
  created: DateTime,
  updated: DateTime
)

object EntityDescriptor {

  def apply(entity: Entity): EntityDescriptor = {
    EntityDescriptor(entity.id,
      entity.name,
      entity.description,
      entity.dClass,
      entity.saved,
      entity.created,
      entity.updated)
  }
}

/**
 * Entity Description that can be provided by user.
 */
case class UserEntityDescription(
  id: Entity.Id,
  name: String,
  description: String,
  saved: Boolean = true
)
