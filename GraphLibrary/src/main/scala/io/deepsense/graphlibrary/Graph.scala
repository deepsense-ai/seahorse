/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.graphlibrary

import scala.collection.mutable.{Set, Map}

import io.deepsense.graphlibrary.Node.State
import io.deepsense.graphlibrary.Node.State.{Progress, Status}

/**
 * Execution Graph of the experiment.
 * It can be serialized for the purpose of sending it over the network.
 * Nodes of this graph contain operations and state.
 * State of each node can be changed during the execution.
 */
@SerialVersionUID(100L)
class Graph extends Serializable {
  private case class GraphNode(id: Node.Id, operation: Operation) extends Node {
    var state: State = State.inDraft
    val predecessors: Array[Node] = new Array(operation.degIn)
    val successors: Array[Set[Node]] = Array.fill(operation.degOut) { Set() }

    def addPredecessor(index: Int, node: Node): Unit = predecessors(index) = node

    def addSuccessor(index: Int, node: Node): Unit = successors(index) += node

    override def toString(): String = id.toString
  }

  private val nodes: Map[Node.Id, GraphNode] = Map()

  def addNode(id: Node.Id, operation: Operation): Node = {
    val node = GraphNode(id, operation)
    nodes(id) = node
    node
  }

  def addEdge(nodeFrom: Node.Id, nodeTo: Node.Id, portFrom: Int, portTo: Int): Unit = {
    nodes(nodeFrom).addSuccessor(portFrom, nodes(nodeTo))
    nodes(nodeTo).addPredecessor(portTo, nodes(nodeFrom))
  }

  def readyNodes(): List[Node] = {
    val queuedNodes = nodes.values.filter(_.state.status == Status.QUEUED)
    queuedNodes.filter(_.predecessors.forall(
      (p: Node) => p != null && p.state.status == Status.COMPLETED)).toList
  }

  def getNode(id: Node.Id): Node = nodes(id)

  def markAsInDraft(id: Node.Id): Unit = {
    nodes(id).state = State.inDraft
  }

  def markAsQueued(id: Node.Id): Unit = {
    nodes(id).state = State.queued
  }

  def markAsRunning(id: Node.Id): Unit = {
    val node = nodes(id)
    node.state = State.running(Progress(0, node.operation.total))
  }

  def markAsCompleted(id: Node.Id, results: List[Node.Id]): Unit = {
    val node = nodes(id)
    node.state = node.state.completed(results)
  }

  def markAsFailed(id: Node.Id): Unit = {
    val node = nodes(id)
    node.state = node.state.failed
  }

  def markAsAborted(id: Node.Id): Unit = {
    val node = nodes(id)
    node.state = node.state.aborted
  }

  def reportProgress(id: Node.Id, current: Int): Unit = {
    val node = nodes(id)
    node.state = node.state.withProgress(Progress(current, node.operation.total))
  }

  def size: Int = nodes.size

  def canEqual(other: Any): Boolean = other.isInstanceOf[Graph]

  override def equals(other: Any): Boolean = other match {
    case that: Graph => (that canEqual this) && nodes == that.nodes
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(nodes)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
