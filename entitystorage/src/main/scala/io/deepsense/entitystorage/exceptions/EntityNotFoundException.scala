/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.exceptions

import java.util.UUID

import io.deepsense.entitystorage.models.Entity

/**
 * Thrown when the specified entity was not found.
 * @param entityId Identifier of the requested experiment
 */
case class EntityNotFoundException(entityId: Entity.Id)
  extends EntityStorageException(
    UUID.randomUUID(),
    ErrorCodes.EntityNotFound,
    "Entity not found",
    s"Entity with id $entityId not found", None, None)
