/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.models

import java.util.UUID

/**
 * Experiment model.
 */
case class Experiment(id: Experiment.Id, name: String, description: Option[String])

object Experiment {

  case class Id(value: UUID) {
    override def toString: String = value.toString
  }

  object Id {
    implicit def fromUuid(uuid: UUID): Id = Id(uuid)
  }

  object Status extends Enumeration {
    type Status = Value
    val INDRAFT = Value(0, "indraft")
    val RUNNING = Value(1, "running")
  }

  case class State(status: Experiment.Status.Value /*, error: Optional[Error]*/)
}
