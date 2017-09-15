/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.models

import io.deepsense.experimentmanager.app.models.{Id => IdModel}
import io.deepsense.experimentmanager.auth.Ownable

/**
 * Experiment model.
 */
case class Experiment(
    id: Experiment.Id,
    tenantId: String,
    name: String,
    description: String = "",
    graph: Graph = Graph())
  extends BaseExperiment(name, description, graph)
  with Ownable {

  /**
   * Creates an updated version of the experiment using other experiment.
   * Rewrites to the new experiment all fields from the other experiment,
   * but does not change id or tenant id.
   * @param other The other experiment to update with.
   * @return Updated version of an experiment.
   */
  def updatedWith(other: Experiment): Experiment = {
    Experiment(id, tenantId, other.name, other.description, other.graph)
  }
}

object Experiment {
  type Id = IdModel

  object Status extends Enumeration {
    type Status = Value
    val INDRAFT = Value(0, "indraft")
    val RUNNING = Value(1, "running")
  }

  case class State(status: Experiment.Status.Value /* , error: Optional[Error] */)
}
