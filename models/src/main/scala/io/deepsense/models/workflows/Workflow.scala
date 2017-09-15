/**
 * Copyright 2015, CodiLime Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.models.workflows

import org.joda.time.DateTime

import io.deepsense.commons.auth.Ownable
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.FailureCode._
import io.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.commons.models
import io.deepsense.commons.utils.Logging
import io.deepsense.graph.{Graph, Node}
import io.deepsense.models.workflows.Workflow.State
import io.deepsense.models.workflows.Workflow.Status.Status

/**
 * Experiment model.
 */
@SerialVersionUID(1)
case class Workflow(
    id: Workflow.Id,
    tenantId: String,
    name: String,
    graph: Graph,
    created: DateTime = DateTimeConverter.now,
    updated: DateTime = DateTimeConverter.now,
    description: String = "",
    state: State = State.draft)
  extends BaseWorkflow(name, description, graph)
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
  def updatedWith(inputExperiment: InputWorkflow, updateDateTime: DateTime): Workflow = {
    copy(
      name = inputExperiment.name,
      graph = inputExperiment.graph,
      updated = updateDateTime,
      description = inputExperiment.description)
  }

  def withNode(node: Node): Workflow =
    copy(graph = graph.withChangedNode(node))

  def markAborted: Workflow = {
    val abortedNodes = graph.nodes.map(n => if (n.isFailed || n.isCompleted) n else n.markAborted)
    copy(graph = graph.copy(nodes = abortedNodes), state = State.aborted)
  }

  def markRunning: Workflow = copy(graph = graph.enqueueNodes, state = State.running)
  def markCompleted: Workflow = copy(state = State.completed)
  def markFailed(details: FailureDescription): Workflow = copy(state = state.failed(details))

  def isRunning: Boolean = state.status == Workflow.Status.Running
  def isFailed: Boolean = state.status == Workflow.Status.Failed
  def isAborted: Boolean = state.status == Workflow.Status.Aborted
  def isDraft: Boolean = state.status == Workflow.Status.Draft
  def isCompleted: Boolean = state.status == Workflow.Status.Completed

  def markNodeFailed(nodeId: Node.Id, reason: Throwable): Workflow = {
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
      Workflow.NodeFailureMessage)
    copy(graph = this.graph.markAsFailed(nodeId, nodeFailureDetails))
      .markFailed(experimentFailureDetails)
  }

  def readyNodes: List[Node] = graph.readyNodes
  def runningNodes: Set[Node] = graph.nodes.filter(_.isRunning)
  def markNodeRunning(id: Node.Id): Workflow =
    copy(graph = graph.markAsRunning(id))

  def updateState(): Workflow = {
    // TODO precise semantics of this method
    // TODO rewrite this method to be more effective (single counting)
    import io.deepsense.models.workflows.Workflow.State._
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
      failed(FailureDescription(failureId, FailureCode.NodeFailure, Workflow.NodeFailureMessage))
    } else {
      aborted
    })
  }
}

object Workflow {

  def apply(
      id: Workflow.Id,
      tenantId: String,
      name: String,
      graph: Graph): Workflow = {
    val created = DateTimeConverter.now
    new Workflow(id, tenantId, name, graph, created, created)
  }

  type Id = models.Id
  val Id = models.Id

  val NodeFailureMessage = "One or more nodes failed in the experiment"

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
