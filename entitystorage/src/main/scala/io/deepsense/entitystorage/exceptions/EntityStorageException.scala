/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.entitystorage.exceptions

import io.deepsense.commons.exception.{DeepSenseException, ExceptionDetails}

/**
 * Base exception for all Entity Storage exceptions
 */
abstract class EntityStorageException(
    id: DeepSenseException.Id,
    code: Int,
    title: String,
    message: String,
    cause: Option[Throwable],
    details: Option[ExceptionDetails])
  extends DeepSenseException(id, code, title, message, cause, details)
