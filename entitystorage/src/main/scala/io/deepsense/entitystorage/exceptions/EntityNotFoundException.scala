/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.entitystorage.exceptions

import io.deepsense.commons.exception.DeepSenseException
import io.deepsense.models.entities.Entity

/**
 * Thrown when the specified entity was not found.
 * @param entityId Identifier of the requested experiment
 */
case class EntityNotFoundException(entityId: Entity.Id)
  extends EntityStorageException(
    DeepSenseException.Id.randomId,
    ErrorCodes.EntityNotFound,
    "Entity not found",
    s"Entity with id $entityId not found",
    None,
    None)
