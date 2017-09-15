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

package io.deepsense.graph

import io.deepsense.commons.exception.FailureCode._
import io.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.commons.utils.Logging
import io.deepsense.graph.Node.Id
import io.deepsense.models.entities.Entity

case class Graph(
    nodes: Set[Node] = Set(),
    edges: Set[Edge] = Set(),
    state: GraphState = GraphState.draft)
  extends KnowledgeInference
  with Logging {

  /** Maps ids of nodes to nodes. */
  val nodeById: Map[Id, Node] = nodes.map(node => node.id -> node).toMap

  /**
   * Successors of the nodes (represented as an edge end).
   * For each node, the sequence of sets represents nodes connected to the node. Each n-th set
   * in the sequence represents nodes connected to the n-th port.
   * If n-th output ports is not used then the sequence contains an empty set on n-th position.
   */
  val successors: Map[Id, IndexedSeq[Set[Endpoint]]] = prepareSuccessors

  /**
   * Predecessors of the nodes (represented as an edge end).
   * For each node, the sequence represents nodes connected to the input ports of the node.
   * If n-th port is not used then the sequence contains None on n-th position.
   */
  val predecessors: Map[Node.Id, IndexedSeq[Option[Endpoint]]] = preparePredecessors

  /**
   * Find all (direct and indirect) predecessors of given node.
   */
  def allPredecessorsOf(id: Node.Id): Set[Node] = {
    predecessors(id).foldLeft(Set[Node]())((acc: Set[Node], predecessor: Option[Endpoint]) =>
      predecessor match {
          case None => acc
          case Some(endpoint) => (acc + nodeById(endpoint.nodeId)) ++
            allPredecessorsOf(endpoint.nodeId)
        })
  }

  def readyNodes: List[Node] = {
    nodes.filter(_.state.status == Status.Queued)
      .filter(predecessorsCompleted)
      .toList
  }

  /**
   * Returns a node with the specified Id or throws a
   *  `NoSuchElementException` when there is no such a node.
   * @param id Id of the node to return
   * @return A node with the specified Id.
   */
  def node(id: Node.Id): Node = nodeById(id.value)

  def markAsDraft(id: Node.Id): Graph = withChangedNode(id, _.markDraft)

  def markAsQueued(id: Node.Id): Graph = withChangedNode(id, _.markQueued)

  def markAsRunning(id: Node.Id): Graph = withChangedNode(id, _.markRunning)

  def markAsCompleted(id: Node.Id, results: List[Entity.Id]): Graph =
    withChangedNode(id, _.markCompleted(results))

  def markAsFailed(id: Node.Id, failureDetails: FailureDescription): Graph =
    withChangedNode(id, _.markFailed(failureDetails))

  def markAsAborted(id: Node.Id): Graph = withChangedNode(id, _.markAborted)

  def reportProgress(id: Node.Id, current: Int): Graph =
    withChangedNode(id, _.withProgress(current))

  def enqueueNodes: Graph =
    copy(nodes = nodes.map(_.markQueued))

  def abortNodes: Graph =
    copy(nodes = nodes.map(_.markAborted))

  def containsCycle: Boolean = new TopologicalSort(this).isSorted

  def size: Int = nodes.size

  /** Returns topologically sorted nodes if the Graph does not contain cycles. */
  def topologicallySorted: Option[List[Node]] = new TopologicalSort(this).sortedNodes

  private def prepareSuccessors: Map[Id, IndexedSeq[Set[Endpoint]]] = {
    import scala.collection.mutable
    val mutableSuccessors: mutable.Map[Node.Id, IndexedSeq[mutable.Set[Endpoint]]] =
      mutable.Map()

    nodes.foreach(node => {
      mutableSuccessors += node.id -> Vector.fill(node.operation.outArity)(mutable.Set())
    })
    edges.foreach(edge => {
      mutableSuccessors(edge.from.nodeId)(edge.from.portIndex) += edge.to
    })
    mutableSuccessors.mapValues(_.map(_.toSet)).toMap
  }

  private def preparePredecessors: Map[Node.Id, IndexedSeq[Option[Endpoint]]] = {
    import scala.collection.mutable
    val mutablePredecessors: mutable.Map[Node.Id, mutable.IndexedSeq[Option[Endpoint]]] =
      mutable.Map()

    nodes.foreach(node => {
      mutablePredecessors +=
        node.id -> mutable.IndexedSeq.fill(node.operation.inArity)(None)
    })
    edges.foreach(edge => {
      mutablePredecessors(edge.to.nodeId)(edge.to.portIndex) = Some(edge.from)
    })
    mutablePredecessors.mapValues(_.toIndexedSeq).toMap
  }

  private def withChangedNode(id: Node.Id, f: Node => Node): Graph = {
    // TODO make it more efficient (change nodes to map)
    val changedNodes = nodes.map(node => if (node.id == id) f(node) else node)
    copy(nodes = changedNodes)
  }

  def withChangedNode(node: Node): Graph = {
    // TODO make it more efficient (change nodes to map)
    val changedNodes = nodes.map(n => if (n.id == node.id) node else n)
    copy(nodes = changedNodes)
  }

  private def predecessorsCompleted(node: Node): Boolean = {
    predecessors(node.id).forall(edgeEnd => {
      edgeEnd.isDefined && nodeById(edgeEnd.get.nodeId.value).isCompleted
    })
  }

  def markAborted: Graph = {
    val abortedNodes = nodes.map(n => if (n.isFailed || n.isCompleted) n else n.markAborted)
    copy(nodes = abortedNodes, state = GraphState.aborted)
  }

  def markRunning: Graph = enqueueNodes.copy(state = GraphState.running)
  def markCompleted: Graph = copy(state = GraphState.completed)
  def markFailed(details: FailureDescription): Graph =
    copy(state = state.failed(details))

  def isRunning: Boolean = state.status == Status.Running
  def isFailed: Boolean = state.status == Status.Failed
  def isAborted: Boolean = state.status == Status.Aborted
  def isDraft: Boolean = state.status == Status.Draft
  def isCompleted: Boolean = state.status == Status.Completed

  def markNodeFailed(nodeId: Node.Id, reason: Throwable): Graph = {
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
    val graphFailureDetails = FailureDescription(
      errorId,
      NodeFailure,
      GraphState.NodeFailureMessage)
    markAsFailed(nodeId, nodeFailureDetails).markFailed(graphFailureDetails)
  }

  def runningNodes: Set[Node] = nodes.filter(_.isRunning)

  /**
   * If graph state is not failed, it returns graph with updated state based on state of its nodes.
   */
  def updateState(): Graph = {
    // TODO precise semantics of this method
    // TODO rewrite this method to be more effective (single counting)
    import io.deepsense.graph.GraphState._
    if (state.status == Status.Failed) {
      this
    } else {
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
        failed(FailureDescription(
          failureId, FailureCode.NodeFailure, GraphState.NodeFailureMessage
        ))
      } else {
        aborted
      })
    }
  }
}
