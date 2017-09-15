/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.entitystorage.exceptions

import java.util.UUID

import io.deepsense.commons.exception.{ExceptionDetails, DeepSenseException}

/**
 * Base exception for all Entity Storage exceptions
 */
abstract class EntityStorageException(
    id: UUID,
    code: Int,
    title: String,
    message: String,
    cause: Option[Throwable],
    details: Option[ExceptionDetails])
  extends DeepSenseException(id, code, title, message, cause, details)
