/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.graph

import java.util.UUID

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.{DKnowledge, DOperable, InferContext}
import io.deepsense.graph.Node.Id

case class Graph(nodes: Set[Node] = Set(), edges: Set[Edge] = Set()) {
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
  private lazy val topologicalSort = new TopologicalSort(this)

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

  def markAsInDraft(id: Node.Id): Graph = withChangedNode(id, _.markInDraft)

  def markAsQueued(id: Node.Id): Graph = withChangedNode(id, _.markQueued)

  def markAsRunning(id: Node.Id): Graph = withChangedNode(id, _.markRunning)

  def markAsCompleted(id: Node.Id, results: List[UUID]): Graph = {
    withChangedNode(id, _.markCompleted(results))
  }

  def markAsFailed(id: Node.Id): Graph = withChangedNode(id, _.markFailed)

  def markAsAborted(id: Node.Id): Graph = withChangedNode(id, _.markAborted)

  def reportProgress(id: Node.Id, current: Int): Graph = {
    withChangedNode(id, _.withProgress(current))
  }

  def containsCycle: Boolean = topologicalSort.isSorted

  def size: Int = nodes.size

  /** Returns topologically sorted nodes if the Graph does not contain cycles. */
  def topologicallySorted: Option[List[Node]] = topologicalSort.sortedNodes

  /** Returns graph knowledge with knowledge inferred for the given node. */
  def inferKnowledge(
      node: Node,
      context: InferContext,
      graphKnowledge: GraphKnowledge): GraphKnowledge = {
    val knowledge = for (portIndex <- 0 until predecessors(node.id).size)
      yield inputKnowledgeForInputPort(node, context, graphKnowledge, portIndex)
    val inferredKnowledge = node.operation.inferKnowledge(context)(knowledge.toVector)
    graphKnowledge.addKnowledge(node.id, inferredKnowledge)
  }

  /** Returns port index which contains the given successor. */
  def getSuccessorPort(of: Node.Id, successor: Node): Option[Int] = {
    val successorIndex = successors(of).indexWhere(_.exists(_.nodeId == successor.id))
    if (successorIndex != -1) Some(successorIndex) else None
  }

  /** Returns a graph knowledge with inferred knowledge for every node. */
  def inferKnowledge(context: InferContext): GraphKnowledge = {
    val sorted = topologicallySorted.get
    sorted
      .foldLeft(GraphKnowledge())((knowledge, node) => inferKnowledge(node, context, knowledge))
  }

  /** Returns suitable input knowledge for the given input port index. */
  def inputKnowledgeForInputPort(
      node: Node,
      context: InferContext,
      graphKnowledge: GraphKnowledge,
      portIndex: Int): DKnowledge[DOperable] = {
    // TODO: find a way to delete this cast
    val inPortType = node.operation.inPortTypes(portIndex).asInstanceOf[ru.TypeTag[DOperable]]
    predecessors(node.id)(portIndex) match {
      case None => DKnowledge(context
        .dOperableCatalog
        .concreteSubclassesInstances(inPortType))
      case Some(predecessor) =>
        val outPortIndex = getSuccessorPort(predecessor.nodeId, node).get
        val predecessorKnowledge = graphKnowledge.getKnowledge(predecessor.nodeId)(outPortIndex)
        predecessorKnowledge.filterTypes(inPortType.tpe)
    }
  }

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
    val changedNodes = nodes.map(node => if (node.id == id) f(node) else node)
    copy(nodes = changedNodes)
  }

  private def predecessorsCompleted(node: Node): Boolean = {
    predecessors(node.id).forall(edgeEnd => {
      edgeEnd.isDefined && nodeById(edgeEnd.get.nodeId.value).isCompleted
    })
  }
}
