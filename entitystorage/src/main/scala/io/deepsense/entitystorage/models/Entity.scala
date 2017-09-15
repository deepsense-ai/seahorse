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
}

/**
 * Shorter Entity description used for list presentation
 */
case class EntityDescriptor(
  id: String,
  name: String,
  description: String,
  dClass: String,
  saved: Boolean = true,
  created: DateTime,
  updated: DateTime
)

/**
 * Entity Description that can be provided by user.
 */
case class UserEntityDescription(
  id: String,
  name: String,
  description: String,
  saved: Boolean = true
)
