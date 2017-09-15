/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.models

import io.deepsense.experimentmanager.app.models.{Id => IdModel}

/**
 * Experiment model.
 */
case class Experiment(
    id: Experiment.Id,
    name: String,
    description: String = "",
    graph: Graph = Graph())
  extends BaseExperiment(name, description, graph)

object Experiment {
  type Id = IdModel

  object Status extends Enumeration {
    type Status = Value
    val INDRAFT = Value(0, "indraft")
    val RUNNING = Value(1, "running")
  }

  case class State(status: Experiment.Status.Value /* , error: Optional[Error] */)
}
