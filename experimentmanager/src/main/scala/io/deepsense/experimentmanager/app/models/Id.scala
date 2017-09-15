/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.models

import java.util.UUID

/**
 * Id model (UUID wrapper)
 * @param value UUID
 */
case class Id(value: UUID) {
  override def toString: String = value.toString
}

object Id {
  implicit def fromUuid(uuid: UUID): Id = Id(uuid)
}

