/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.models

import java.util.UUID

/**
 * Graph's node model.
 */
case class Node(id: Node.Id)

object Node {
  case class Id(value: UUID)

  object Id {
    implicit def fromUuid(uuid: UUID): Id = Id(uuid)
  }
}

