/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.experiments

import org.joda.time.DateTime

import io.deepsense.commons.auth.Ownable
import io.deepsense.commons.datetime.DateTimeConverter
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
    created: DateTime,
    updated: DateTime,
    description: String = "",
    state: State = State.draft)
  extends BaseExperiment(name, description, graph)
  with Ownable
  with Serializable {

  /**
   * Creates a copy of the experiment with name, graph, updated, and
   * description fields updated to reflect inputExperiment and updateDateTime
   * input parameters' values.
   *
   * @param inputExperiment The input experiment to update with.
   * @return Updated version of an experiment.
   */
  def updatedWith(inputExperiment: InputExperiment, updateDateTime: DateTime): Experiment = {
    copy(
      name = inputExperiment.name,
      graph = inputExperiment.graph,
      updated = updateDateTime,
      description = inputExperiment.description)
  }

  /**
   * Creates a copy of the experiment with a given graph
   * and updates status of the experiment based upon the status of
   * the nodes in the graph
   *
   * @param graph graph of the experiment
   * @return a copy of the current experiment with the graph inside
   */
  def withGraph(graph: Graph): Experiment =
    copy(graph = graph, state = Experiment.computeExperimentState(graph))

  def markAborted: Experiment = {
    val abortedNodes = graph.nodes.map(n => if (n.isFailed || n.isCompleted) n else n.markAborted)
    copy(graph = graph.copy(nodes = abortedNodes), state = State.aborted)
  }

  def markRunning: Experiment = copy(graph = graph.enqueueNodes, state = State.running)
  def markCompleted: Experiment = copy(state = State.completed)
  def markFailed(message: String): Experiment = copy(state = State.failed(message))

  def isRunning: Boolean = state.status == Experiment.Status.Running
  def isFailed: Boolean = state.status == Experiment.Status.Failed
  def isAborted: Boolean = state.status == Experiment.Status.Aborted
  def isDraft: Boolean = state.status == Experiment.Status.Draft
}

object Experiment {

  def apply(
      id: Experiment.Id,
      tenantId: String,
      name: String,
      graph: Graph): Experiment = {
    val created = DateTimeConverter.now
    new Experiment(id, tenantId, name, graph, created, created)
  }

  type Id = models.Id
  val Id = models.Id

  def computeExperimentState(graph: Graph): Experiment.State = {
    import Experiment.State._
    val nodes = graph.nodes
    if (nodes.isEmpty) {
      completed
    } else if (nodes.forall(_.isDraft)) {
      draft
    } else if (nodes.forall(n => n.isDraft || n.isCompleted)) {
      completed
    } else if (nodes.forall(n => n.isDraft || n.isCompleted || n.isQueued || n.isRunning)) {
      running
    } else if (nodes.exists(_.isFailed)) {
      val failedNodesCount = nodes.count(_.isFailed)
      failed(s"$failedNodesCount")
    } else {
      aborted
    }
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
    def draft: State = State.draft
    def running: State = State.running
    def completed: State = State.completed
    def failed(message: String): State = State.failed(message)
    def aborted: State = State.aborted
  }

  object State {
    val draft = State(Status.Draft, None)
    val running = State(Status.Running, None)
    val completed = State(Status.Completed, None)
    def failed(message: String): State = State(Status.Failed, Some(message))
    val aborted = State(Status.Aborted, None)
  }
}
