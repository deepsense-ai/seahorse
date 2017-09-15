/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.models.experiments

import io.deepsense.commons.auth.Ownable
import io.deepsense.commons.models
import io.deepsense.graph.Graph

/**
 * Experiment model.
 */
@SerialVersionUID(1)
case class Experiment(
    id: Experiment.Id,
    tenantId: String,
    name: String,
    graph: Graph,
    description: String = "")
  extends BaseExperiment(name, description, graph)
  with Ownable
  with Serializable {

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
}

object Experiment {
  type Id = models.Id

  object Id {
    def randomId = models.Id.randomId
  }

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
