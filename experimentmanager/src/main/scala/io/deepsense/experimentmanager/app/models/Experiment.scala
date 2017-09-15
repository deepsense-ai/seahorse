/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.models

import io.deepsense.experimentmanager.app.models.{Id => IdModel}
import io.deepsense.experimentmanager.auth.Ownable
import io.deepsense.graph.Graph

/**
 * Experiment model.
 */
case class Experiment(
    id: Id,
    tenantId: String,
    name: String,
    graph: Graph,
    description: String = "")
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
    Experiment(id, tenantId, other.name, other.graph, other.description)
  }

  /**
   * Creates an updated version of the experiment using an input experiment.
   * Updates the name, the description and the graph of the experiment.
   * @param inputExperiment Input experiment.
   * @return Updated experiment.
   */
  def updatedWith(inputExperiment: InputExperiment): Experiment = {
    copy(
      name = inputExperiment.name,
      description = inputExperiment.description,
      graph = inputExperiment.graph
    )
  }
}

object Experiment {
  type Id = IdModel

  object Status extends Enumeration {
    type Status = Value
    val InDraft = Value(0, "indraft")
    val Running = Value(1, "running")
    val Completed = Value(2, "completed")
    val Failed = Value(3, "failed")
    val Aborted = Value(4, "aborted")
  }

  case class State(status: Experiment.Status.Value /* , error: Optional[Error] */)
}
