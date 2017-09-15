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
  tenantId: String,
  id: Entity.Id,
  name: String,
  description: String,
  dClass: String,
  data: Option[DataObjectReference],
  report: Option[DataObjectReport],
  created: DateTime,
  updated: DateTime,
  saved: Boolean = true
) {
    if (dClass == DataObjectReport.getClass.getName) {
        require(data.isInstanceOf[DataObjectReport])
    }

  def reportOnly = copy(data = None)

  def dataOnly = copy(report = None)

  def descriptor = CompactEntityDescriptor(this)
}

object Entity {
  type Id = models.Id

  object Id {
    def randomId = models.Id.randomId
  }
}

/**
 * Data that describe entity to save.
 */
case class InputEntity (
  tenantId: String,
  name: String,
  description: String,
  dClass: String,
  data: Option[DataObjectReference],
  report: Option[DataObjectReport],
  saved: Boolean
) {
    if (dClass == DataObjectReport.getClass.getName) {
        require(data.isInstanceOf[DataObjectReport])
    }
}

/**
 * Shorter Entity description used for list presentation
 */
case class CompactEntityDescriptor(
  tenantId: String,
  id: Entity.Id,
  name: String,
  description: String,
  dClass: String,
  created: DateTime,
  updated: DateTime,
  saved: Boolean = true
)

object CompactEntityDescriptor {

  def apply(entity: Entity): CompactEntityDescriptor = {
    CompactEntityDescriptor(
      entity.tenantId,
      entity.id,
      entity.name,
      entity.description,
      entity.dClass,
      entity.created,
      entity.updated,
      entity.saved)
  }
}

/**
 * Entity Description that can be provided by user.
 */
case class UserEntityDescriptor(
  id: Entity.Id,
  name: String,
  description: String,
  saved: Boolean = true
)
