/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.models

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
  implicit def fromString(uuid: String): Id = Id(UUID.fromString(uuid))

  def randomId: Id = fromUuid(UUID.randomUUID())
}
