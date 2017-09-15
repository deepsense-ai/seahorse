/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.experiments

import org.joda.time.DateTime

import io.deepsense.commons.auth.Ownable
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.FailureCode._
import io.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.commons.models
import io.deepsense.commons.utils.Logging
import io.deepsense.graph.{Graph, Node}
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
    created: DateTime = DateTimeConverter.now,
    updated: DateTime = DateTimeConverter.now,
    description: String = "",
    state: State = State.draft)
  extends BaseExperiment(name, description, graph)
  with Ownable
  with Serializable
  with Logging {

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

  def withNode(node: Node): Experiment =
    copy(graph = graph.withChangedNode(node))

  def markAborted: Experiment = {
    val abortedNodes = graph.nodes.map(n => if (n.isFailed || n.isCompleted) n else n.markAborted)
    copy(graph = graph.copy(nodes = abortedNodes), state = State.aborted)
  }

  def markRunning: Experiment = copy(graph = graph.enqueueNodes, state = State.running)
  def markCompleted: Experiment = copy(state = State.completed)
  def markFailed(details: FailureDescription): Experiment = copy(state = state.failed(details))

  def isRunning: Boolean = state.status == Experiment.Status.Running
  def isFailed: Boolean = state.status == Experiment.Status.Failed
  def isAborted: Boolean = state.status == Experiment.Status.Aborted
  def isDraft: Boolean = state.status == Experiment.Status.Draft
  def isCompleted: Boolean = state.status == Experiment.Status.Completed

  def markNodeFailed(nodeId: Node.Id, reason: Throwable): Experiment = {
    val errorId = DeepSenseFailure.Id.randomId
    val failureTitle = s"Node: $nodeId failed. Error Id: $errorId"
    logger.error(failureTitle, reason)
    // TODO: To decision: exception in single node should result in abortion of:
    // (current) only descendant nodes of failed node? / only queued nodes? / all other nodes?
    val nodeFailureDetails = FailureDescription(
      errorId,
      UnexpectedError,
      failureTitle,
      Some(reason.toString),
      FailureDescription.stacktraceDetails(reason.getStackTrace))
    val experimentFailureDetails = FailureDescription(
      errorId,
      NodeFailure,
      Experiment.failureMessage(id))
    copy(graph = this.graph.markAsFailed(nodeId, nodeFailureDetails))
      .markFailed(experimentFailureDetails)
  }

  def readyNodes: List[Node] = graph.readyNodes
  def runningNodes: Set[Node] = graph.nodes.filter(_.isRunning)
  def markNodeRunning(id: Node.Id): Experiment =
    copy(graph = graph.markAsRunning(id))

  def updateState(): Experiment = {
    // TODO precise semantics of this method
    // TODO rewrite this method to be more effective (single counting)
    import io.deepsense.models.experiments.Experiment.State._
    val nodes = graph.nodes
    copy(state = if (nodes.isEmpty) {
      completed
    } else if (nodes.forall(_.isDraft)) {
      draft
    } else if (nodes.forall(n => n.isDraft || n.isCompleted)) {
      completed
    } else if (nodes.forall(n => n.isDraft || n.isCompleted || n.isQueued || n.isRunning)) {
      running
    } else if (nodes.exists(_.isFailed)) {
      val failureId = DeepSenseFailure.Id.randomId
      failed(FailureDescription(failureId, FailureCode.NodeFailure, "Node Failure"))
    } else {
      aborted
    })
  }
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

  def failureMessage(id: Id) = s"One or more nodes failed in the experiment: $id"

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
      val failureId = DeepSenseFailure.Id.randomId
      failed(FailureDescription(failureId, FailureCode.NodeFailure, "Node Failure"))
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

  case class State(status: Status, error: Option[FailureDescription] = None) {
    def draft: State = State.draft
    def running: State = State.running
    def completed: State = State.completed
    def failed(error: FailureDescription): State = State.failed(error)
    def aborted: State = State.aborted
  }

  object State {
    val draft = State(Status.Draft)
    val running = State(Status.Running)
    val completed = State(Status.Completed)
    def failed(error: FailureDescription): State = State(Status.Failed, Some(error))
    val aborted = State(Status.Aborted)
  }
}
