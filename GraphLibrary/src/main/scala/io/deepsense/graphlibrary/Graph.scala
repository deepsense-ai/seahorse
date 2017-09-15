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
 * Colours of node.
 *
 * Used for cycle checking. This object should be inner object
 * of Graph. However it makes serialization of Graph impossible.
 * This is why a decision was made to move it outside of Graph.
 */
private[graphlibrary] object Color extends Enumeration {
  type Color = Value
  val WHITE, GREY, BLACK = Value
}

/**
 * Execution Graph of the experiment.
 * It can be serialized for the purpose of sending it over the network.
 * Nodes of this graph contain operations and state.
 * State of each node can be changed during the execution.
 */
@SerialVersionUID(100L)
class Graph extends Serializable {
  private case class GraphNode(id: Node.Id, operation: Operation) extends Node {
    var color: Color.Color = Color.WHITE
    var state: State = State.inDraft
    val predecessors: Array[GraphNode] = new Array(operation.degIn)
    val successors: Array[Set[GraphNode]] = Array.fill(operation.degOut) { Set() }

    def addPredecessor(index: Int, node: GraphNode): Unit = predecessors(index) = node

    def addSuccessor(index: Int, node: GraphNode): Unit = successors(index) += node

    def markWhite: Unit = color = Color.WHITE

    def markGrey: Unit = color = Color.GREY

    def markBlack: Unit = color = Color.BLACK

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

  def containsCycle: Boolean = {
    val cycleFound = nodes.values.exists(onCycle(_))
    restoreColors
    cycleFound
  }

  /**
   * Checks if any node reachable from the start node lays on cycle.
   */
  private def onCycle(node: GraphNode): Boolean = {
    if (node == null) false // nothing is connected to the given input port
    else node.color match {
      case Color.BLACK => false
      case Color.GREY => true
      case Color.WHITE => {
        node.markGrey
        val cycleFound = node.predecessors.exists(onCycle(_))
        node.markBlack
        cycleFound
      }
    }
  }

  private def restoreColors: Unit = nodes.values.foreach(_.markWhite)

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
