/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.workflowexecutor.partialexecution

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph.Node.Id
import io.deepsense.graph.nodestate.NodeState
import io.deepsense.graph.{DirectedGraph, Node, ReadyNode, StatefulGraph, nodestate}
import io.deepsense.models.entities.Entity

object Execution {
  def empty: IdleExecution = IdleExecution(StatefulGraph(), Set.empty[Node.Id])

  def apply(directedGraph: DirectedGraph, nodes: Seq[Node.Id] = Seq.empty): IdleExecution = {
    val selected: Set[Node.Id] = selectedNodes(directedGraph, nodes)
    empty.updateStructure(directedGraph, selected)
  }

  def selectedNodes(directedGraph: DirectedGraph, nodes: Seq[Id]): Set[Id] = {
    val graphNodeIds = directedGraph.nodes.map(_.id)
    val filteredNodes = nodes.filter(graphNodeIds.contains).toSet
    val selected = if (filteredNodes.isEmpty) graphNodeIds else filteredNodes
    selected
  }
}

sealed trait ExecutionLike {
  type NodeStates = Map[Node.Id, NodeState]

  def node(id: Node.Id): Node
  def nodeStarted(id: Node.Id): Execution
  def nodeFailed(id: Node.Id, cause: Exception): Execution
  def nodeFinished(id: Node.Id, results: Seq[Entity.Id]): Execution
  def enqueue: Execution
  def readyNodes: Seq[ReadyNode]
  def error: Option[FailureDescription]
  def states: NodeStates
  def isRunning: Boolean
  def inferAndApplyKnowledge(inferContext: InferContext): Execution
  def updateStructure(directedGraph: DirectedGraph, nodes: Set[Node.Id]): Execution
}

sealed abstract class Execution(graph: StatefulGraph, running: Boolean) extends ExecutionLike {
  override def node(id: Node.Id): Node = graph.node(id)

  override def isRunning: Boolean = running
}

case class IdleExecution(
    graph: StatefulGraph,
    selectedNodes: Set[Node.Id])
  extends Execution(graph, running = false) {

  override def states: NodeStates = graph.states

  override def nodeFinished(id: Node.Id, results: Seq[Entity.Id]): Execution = {
    throw new IllegalStateException("A node cannot finish in IdleExecution")
  }

  override def nodeFailed(id: Id, cause: Exception): Execution = {
    throw new IllegalStateException("A node cannot fail in IdleExecution")
  }


  override def nodeStarted(id: Id): Execution = {
    throw new IllegalStateException("A node cannot start in IdleExecution")
  }

  override def updateStructure(newStructure: DirectedGraph, nodes: Set[Id]): IdleExecution = {
    val selected = Execution.selectedNodes(newStructure, nodes.toSeq)
    val substructure = newStructure.subgraph(selected)
    val newState = findStates(newStructure, substructure, selected)
    val graph = StatefulGraph(newStructure, newState, None)
    IdleExecution(graph, selected)
  }

  override def readyNodes: Seq[ReadyNode] = {
    throw new IllegalStateException("IdleExecution has no read nodes!")
  }

  override def enqueue: RunningExecution = {
    val (selected: Set[Id], subgraph: StatefulGraph) = selectedSubgraph
    RunningExecution(graph, subgraph.enqueueDraft, selected)
  }

  override def inferAndApplyKnowledge(inferContext: InferContext): IdleExecution = {
    val (_, subgraph: StatefulGraph) = selectedSubgraph
    val inferred = subgraph.inferAndApplyKnowledge(inferContext)
    copy(graph = graph.updateStates(inferred))
  }

  private def selectedSubgraph: (Set[Id], StatefulGraph) = {
    val selected = Execution.selectedNodes(graph.directedGraph, selectedNodes.toSeq)
    val subgraph = graph.subgraph(selected)
    (selected, subgraph)
  }

  private def findStates(
      newStructure: DirectedGraph,
      substructure: DirectedGraph,
      nodes: Set[Node.Id]): NodeStates = {
    val noMissingStates = newStructure.nodes.foldLeft(states) { (s, n) =>
      if (s.contains(n.id)) {
        s
      } else {
        s.updated(n.id, nodestate.Draft)
      }
    }

    val bigGraph = StatefulGraph(newStructure, noMissingStates, None)

    substructure.nodes.foldLeft(bigGraph){
      case (g, node) =>
        if (g.states(node.id).isCompleted && !nodes.contains(node.id)) {
          g
        } else {
          g.draft(node.id)
        }
    }.states
  }

  override def error: Option[FailureDescription] = graph.executionFailure
  override def isRunning: Boolean = false
}

case class RunningExecution(
    graph: StatefulGraph,
    runningPart: StatefulGraph,
    selectedNodes: Set[Node.Id])
  extends Execution(graph, running = true) {

  override def states: NodeStates = graph.states ++ runningPart.states

  override def updateStructure(directedGraph: DirectedGraph, nodes: Set[Id]): Execution =
    throw new IllegalStateException("Structure of a RunningExecution cannot be altered!")

  override def readyNodes: Seq[ReadyNode] = runningPart.readyNodes

  override def nodeFinished(id: Id, results: Seq[Entity.Id]): Execution =
    withRunningPartUpdated(_.nodeFinished(id, results))

  override def nodeFailed(id: Id, cause: Exception): Execution =
    withRunningPartUpdated(_.nodeFailed(id, cause))

  override def nodeStarted(id: Id): RunningExecution = {
    val updatedRunningPart = runningPart.nodeStarted(id)
    val updatedGraph = graph.updateStates(updatedRunningPart)
    copy(graph = updatedGraph, runningPart = updatedRunningPart)
  }

  override def enqueue: Execution = {
    throw new IllegalStateException("RunningExecution can not be executed again!")
  }

  override def inferAndApplyKnowledge(inferContext: InferContext): RunningExecution = {
    throw new IllegalStateException("RunningExecution cannot infer knowledge!")
  }

  private def withRunningPartUpdated(f: (StatefulGraph) => StatefulGraph): Execution = {
    val updatedRunningPart = f(runningPart)
    val updatedGraph = graph.updateStates(updatedRunningPart)

    if (updatedRunningPart.isRunning) {
      copy(graph = updatedGraph, runningPart = updatedRunningPart)
    } else {
      IdleExecution(updatedGraph, selectedNodes)
    }
  }

  override def error: Option[FailureDescription] = runningPart.executionFailure
}
