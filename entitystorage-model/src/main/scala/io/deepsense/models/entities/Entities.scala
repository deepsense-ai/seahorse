/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.entities

import org.joda.time.DateTime

import io.deepsense.commons.models

object Entity {
  type Id = models.Id
  val Id = models.Id
}

/**
 * Basic information about entity.
 * @param saved Indicates if entity should be visible to end-user. This can be set to false
 *              if this entity is intermediate result of some experiment and not explicitely
 *              saved by user.
 */
case class EntityInfo(
    id: Entity.Id,
    tenantId: String,
    name: String,
    description: String,
    dClass: String,
    created: DateTime,
    updated: DateTime,
    saved: Boolean = true)

trait WithEntityInfo {
  val info: EntityInfo
}

case class EntityWithReport(
    override val info: EntityInfo,
    report: DataObjectReport)
  extends WithEntityInfo

case class EntityWithData(
    override val info: EntityInfo,
    dataReference: DataObjectReference)
  extends WithEntityInfo

/**
 * Fields that can be both created and updated.
 */
trait InputEntityFields {
  val name: String
  val description: String
  val saved: Boolean
}

/**
 * Contains information needed to create an entity.
 * It always contains report and may contain data reference.
 */
case class EntityCreate(
    tenantId: String,
    name: String,
    description: String,
    dClass: String,
    dataReference: Option[DataObjectReference],
    report: DataObjectReport,
    saved: Boolean = true)
  extends InputEntityFields {

  def toEntityInfo(id: Entity.Id, created: DateTime, updated: DateTime): EntityInfo =
    EntityInfo(id, tenantId, name, description, dClass, created, updated, saved)

  def toEntityWithData(id: Entity.Id, created: DateTime, updated: DateTime): EntityWithData =
    EntityWithData(toEntityInfo(id, created, updated), dataReference.get)

  def toEntityWithReport(id: Entity.Id, created: DateTime, updated: DateTime): EntityWithReport =
    EntityWithReport(toEntityInfo(id, created, updated), report)
}

object EntityCreate {
  def apply(
      info: EntityInfo,
      dataReference: Option[DataObjectReference],
      report: DataObjectReport): EntityCreate = EntityCreate(
    info.tenantId, info.name, info.description, info.dClass, dataReference, report, info.saved)

  def apply(entityWithReport: EntityWithReport): EntityCreate = EntityCreate(
    entityWithReport.info, None, entityWithReport.report)
}

/**
 * Contains information needed to update an entity.
 * Note that it is limited to fields that can be modified, i.e. mutable ones.
 */
case class EntityUpdate(
    name: String,
    description: String,
    saved: Boolean = true)
  extends InputEntityFields

object EntityUpdate {
  def apply(entityCreate: EntityCreate): EntityUpdate = EntityUpdate(
    entityCreate.name, entityCreate.description, entityCreate.saved)

  def apply(entity: EntityWithReport): EntityUpdate = EntityUpdate(EntityCreate(entity))
}
