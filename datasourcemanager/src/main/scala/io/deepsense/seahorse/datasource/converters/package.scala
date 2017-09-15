/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource

import scalaz.syntax.validation._

import io.deepsense.commons.service.api.CommonApiExceptions

package object converters {

  private [converters] def validateDefined[T](fieldName: String, field: Option[T]) = field match {
    case Some(value) => value.success
    case None => CommonApiExceptions.fieldMustBeDefined(fieldName).failure
  }

}
