/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.models.entities

import org.joda.time.DateTime

import io.deepsense.commons.models

case class Entity(
    override val info: EntityInfo,
    dataReference: Option[DataObjectReference],
    report: DataObjectReport)
  extends WithEntityInfo {

  def dataOnly: EntityWithData = EntityWithData(info, dataReference.get)

  def reportOnly: EntityWithReport = EntityWithReport(info, report)
}

object Entity {
  type Id = models.Id
  val Id = models.Id

  def apply(
      entity: CreateEntityRequest,
      id: Id,
      created: DateTime,
      updated: DateTime): Entity = Entity(
    EntityInfo(entity, id, updated, created), entity.dataReference, entity.report)
}

case class EntityWithReport(
    override val info: EntityInfo,
    report: DataObjectReport)
  extends WithEntityInfo

object EntityWithReport {
  def apply(
      entity: CreateEntityRequest,
      id: Entity.Id,
      created: DateTime,
      updated: DateTime): EntityWithReport = EntityWithReport(
    EntityInfo(entity, id, updated, created), entity.report
  )
}

case class EntityWithData(
    override val info: EntityInfo,
    dataReference: DataObjectReference)
  extends WithEntityInfo

object EntityWithData {
  def apply(
      entity: CreateEntityRequest,
      id: Entity.Id,
      created: DateTime,
      updated: DateTime): EntityWithData = EntityWithData(
    EntityInfo(entity, id, updated, created), entity.dataReference.get
  )
}

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
 */
case class CreateEntityRequest(
    tenantId: String,
    name: String,
    description: String,
    dClass: String,
    dataReference: Option[DataObjectReference],
    report: DataObjectReport,
    saved: Boolean = true)
  extends InputEntityFields

object CreateEntityRequest {
  def apply(
      info: EntityInfo,
      dataReference: Option[DataObjectReference],
      report: DataObjectReport): CreateEntityRequest = CreateEntityRequest(
    info.tenantId, info.name, info.description, info.dClass, dataReference, report, info.saved)

  def apply(entityWithReport: EntityWithReport): CreateEntityRequest = CreateEntityRequest(
    entityWithReport.info, None, entityWithReport.report)
}

/**
 * Contains information needed to update an entity.
 * Note that it is limited to fields that can be modified, i.e. mutable ones.
 */
case class UpdateEntityRequest(
    name: String,
    description: String,
    saved: Boolean = true)
  extends InputEntityFields

object UpdateEntityRequest {
  def apply(entityCreate: CreateEntityRequest): UpdateEntityRequest = UpdateEntityRequest(
    entityCreate.name, entityCreate.description, entityCreate.saved)

  def apply(entity: EntityWithReport): UpdateEntityRequest =
    UpdateEntityRequest(CreateEntityRequest(entity))
}
