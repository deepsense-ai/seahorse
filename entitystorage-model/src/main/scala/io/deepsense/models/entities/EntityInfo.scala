/**
 * Copyright 2015, CodiLime Inc.
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

/**
 * Basic information about entity.
 * @param saved Indicates if entity should be visible to end-user. This can be set to false
 *              if this entity is intermediate result of some workflow and not explicitly
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
