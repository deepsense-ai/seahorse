/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.models.experiments

import io.deepsense.commons.auth.Ownable
import io.deepsense.commons.models
import io.deepsense.graph.Graph
import io.deepsense.models.experiments.Experiment.State
import io.deepsense.models.experiments.Experiment.Status.Status

/**
 * Experiment model.
 */
@SerialVersionUID(1)
case class Experiment(
    id: Experiment.Id,
    tenantId: String,
    name: String,
    graph: Graph,
    description: String = "",
    state: State = State.draft)
  extends BaseExperiment(name, description, graph)
  with Ownable
  with Serializable {

  /**
   * Creates an updated version of the experiment using some input experiment.
   * Rewrites to the new experiment all fields from the other experiment,
   * but does not change id or tenant id.
   * @param inputExperiment The input experiment to update with.
   * @return Updated version of an experiment.
   */
  def updatedWith(inputExperiment: InputExperiment): Experiment = {
    Experiment(
      id, tenantId, inputExperiment.name, inputExperiment.graph, inputExperiment.description)
  }

  def markAborted: Experiment = {
    val abortedNodes = graph.nodes.map(n => if (n.isFailed || n.isCompleted) n else n.markAborted)
    copy(graph = graph.copy(nodes = abortedNodes), state = State.aborted)
  }

  def markRunning: Experiment = copy(graph = graph.enqueueNodes, state = State.running)
  def markCompleted: Experiment = copy(state = State.completed)
  def markFailed(message: String): Experiment = copy(state = State.failed(message))
}

object Experiment {
  type Id = models.Id

  object Id {
    def randomId = models.Id.randomId
  }

  object Status extends Enumeration {
    type Status = Value
    val Draft = Value(0, "DRAFT")
    val Running = Value(1, "RUNNING")
    val Completed = Value(2, "COMPLETED")
    val Failed = Value(3, "FAILED")
    val Aborted = Value(4, "ABORTED")
  }

  case class State(status: Status, error: Option[String]) {
    def draft = State.draft
    def running = State.running
    def completed = State.completed
    def failed(message: String) = State.failed(message)
    def aborted = State.aborted
  }

  object State {
    val draft = State(Status.Draft, None)
    val running = State(Status.Running, None)
    val completed = State(Status.Completed, None)
    def failed(message: String) = State(Status.Failed, Some(message))
    val aborted = State(Status.Aborted, None)
  }
}
