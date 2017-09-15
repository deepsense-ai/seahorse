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
import io.deepsense.commons.models.Entity
import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph.DeeplangGraph.DeeplangNode
import io.deepsense.graph.Node.Id
import io.deepsense.graph._
import io.deepsense.models.workflows.{ExecutionReport, NodeStateWithResults}
import io.deepsense.reportlib.model.ReportContent

object Execution {
  def empty: IdleExecution = IdleExecution(StatefulGraph(), Set.empty[Node.Id])

  def apply(graph: StatefulGraph, selectedNodes: Set[Node.Id] = Set.empty): IdleExecution = {
    IdleExecution(graph, selectedNodes)
  }

  def selectedNodes(directedGraph: DeeplangGraph, nodes: Seq[Id]): Set[Id] = {
    val graphNodeIds = directedGraph.nodes.map(_.id)
    val filteredNodes = nodes.filter(graphNodeIds.contains).toSet
    filteredNodes
  }

  def defaultExecutionFactory(graph: StatefulGraph): Execution = {
    Execution(graph)
  }
}

sealed abstract class Execution(val graph: StatefulGraph, running: Boolean) {
  final def node(id: Node.Id): DeeplangNode = graph.node(id)

  def isRunning: Boolean = running

  def executionReport: ExecutionReport = {
    ExecutionReport(graph.states.mapValues(_.nodeState), graph.executionFailure)
  }

  def inferKnowledge(context: InferContext): GraphKnowledge = {
    graph.inferKnowledge(context, graph.memorizedKnowledge)
  }

  type NodeStates = Map[Node.Id, NodeStateWithResults]

  def nodeStarted(id: Node.Id): Execution
  def nodeFailed(id: Node.Id, cause: Exception): Execution
  def nodeFinished(
    id: Node.Id,
    resultsIds: Seq[Entity.Id],
    reports: Map[Entity.Id, ReportContent],
    dOperables: Map[Entity.Id, DOperable]): Execution
  def enqueue: Execution
  def readyNodes: Seq[ReadyNode]
  def error: Option[FailureDescription]
  def states: NodeStates
  def inferAndApplyKnowledge(inferContext: InferContext): Execution
  def updateStructure(directedGraph: DeeplangGraph, nodes: Set[Node.Id] = Set.empty): Execution
  def abort: Execution

}

case class IdleExecution(
    override val graph: StatefulGraph,
    selectedNodes: Set[Node.Id] = Set.empty)
  extends Execution(graph, running = false) {

  override def states: NodeStates = graph.states

  override def nodeFinished(
      id: Node.Id,
      resultsIds: Seq[Entity.Id],
      reports: Map[Entity.Id, ReportContent],
      dOperables: Map[Entity.Id, DOperable]): Execution = {
    throw new IllegalStateException("A node cannot finish in IdleExecution")
  }

  override def nodeFailed(id: Id, cause: Exception): Execution = {
    throw new IllegalStateException("A node cannot fail in IdleExecution")
  }


  override def nodeStarted(id: Id): Execution = {
    throw new IllegalStateException("A node cannot start in IdleExecution")
  }

  override def updateStructure(
      newStructure: DeeplangGraph,
      nodes: Set[Id] = Set.empty): IdleExecution = {
    val selected = Execution.selectedNodes(newStructure, nodes.toSeq)
    val substructure = newStructure.subgraph(selected)
    val newStates = findStates(newStructure, substructure, selected)
    val graph = StatefulGraph(newStructure, newStates, None)
    IdleExecution(graph, selected)
  }

  override def readyNodes: Seq[ReadyNode] = {
    throw new IllegalStateException("IdleExecution has no read nodes!")
  }

  override def enqueue: Execution = {
    val (selected: Set[Id], subgraph: StatefulGraph) = selectedSubgraph
    val enqueuedSubgraph: StatefulGraph = subgraph.enqueueDraft
    if (enqueuedSubgraph.isRunning) {
      RunningExecution(graph, enqueuedSubgraph, selected)
    } else {
      this
    }
  }

  override def inferAndApplyKnowledge(inferContext: InferContext): IdleExecution = {
    val (_, subgraph: StatefulGraph) = selectedSubgraph
    val inferred = subgraph.inferAndApplyKnowledge(inferContext)
    copy(graph = graph.updateStates(inferred))
  }

  override def abort: Execution = {
    throw new IllegalStateException("IdleExecution cannot be aborted!")
  }

  private def selectedSubgraph: (Set[Id], StatefulGraph) = {
    val selected = Execution.selectedNodes(graph.directedGraph, selectedNodes.toSeq)
    val subgraph = graph.subgraph(selected)
    (selected, subgraph)
  }

  private def operationParamsChanged(
      newNode: DeeplangGraph.DeeplangNode,
      graph: StatefulGraph): Boolean = {
    graph.nodes.find(_.id == newNode.id) match {
      case Some(oldNode) =>
        !newNode.value.sameAs(oldNode.value)
      case None => true
    }
  }

  private def findStates(
      newStructure: DeeplangGraph,
      substructure: DeeplangGraph,
      nodes: Set[Node.Id]): NodeStates = {
    val noMissingStates = newStructure.nodes.map {
      case Node(id, _) => id -> states.getOrElse(id, NodeStateWithResults.draft)
    }.toMap

    if (newStructure.containsCycle) {
      noMissingStates.mapValues(_.draft.clearKnowledge)
    } else {
      val wholeGraph = StatefulGraph(newStructure, noMissingStates, None)

      val newNodes = newStructure.nodes.map(_.id).diff(graph.directedGraph.nodes.map(_.id))

      val nodesToExecute = substructure.nodes.filter { case Node(id, _) =>
        nodes.contains(id) || !wholeGraph.states(id).isCompleted
      }.map(_.id)

      val predecessorsChangedNodes = newStructure.nodes.map(_.id).diff(newNodes).filter {
        id => newStructure.predecessors(id) != graph.predecessors(id)
      }

      val changedParameters = newStructure.nodes.collect {
        case node if operationParamsChanged(node, graph) => node.id
      }

      val changedNodes = predecessorsChangedNodes ++ changedParameters
      val nodesNeedingDrafting = newNodes ++ nodesToExecute ++ changedNodes

      val transformGraph = draftNodes(nodesNeedingDrafting) _ andThen
        clearNodesKnowledge(changedNodes)

      transformGraph(wholeGraph).states
    }
  }

  private def draftNodes(
      nodesNeedingDrafting: Set[Node.Id])(
      graph: StatefulGraph): StatefulGraph = {
    nodesNeedingDrafting.foldLeft(graph) {
      case (g, id) => g.draft(id)
    }
  }

  private def clearNodesKnowledge(
      nodesToClear: Set[Node.Id])(
      graph: StatefulGraph): StatefulGraph = {
    nodesToClear.foldLeft(graph) {
      case (g, id) => g.clearKnowledge(id)
    }
  }

  override def error: Option[FailureDescription] = graph.executionFailure
  override def isRunning: Boolean = false
}

abstract class StartedExecution(
  graph: StatefulGraph,
  runningPart: StatefulGraph,
  selectedNodes: Set[Node.Id])
  extends Execution(graph, running = true) {

  override def states: NodeStates = graph.states ++ runningPart.states

  override def readyNodes: Seq[ReadyNode] = runningPart.readyNodes

  override def nodeFinished(
      id: Node.Id,
      resultsIds: Seq[Entity.Id],
      reports: Map[Entity.Id, ReportContent],
      dOperables: Map[Entity.Id, DOperable]): Execution = {
    withRunningPartUpdated(_.nodeFinished(id, resultsIds, reports, dOperables))
  }

  override def nodeFailed(id: Id, cause: Exception): Execution =
    withRunningPartUpdated(_.nodeFailed(id, cause))

  override def error: Option[FailureDescription] = runningPart.executionFailure

  override def enqueue: Execution = {
    throw new IllegalStateException("An Execution that is not idle cannot be enqueued!")
  }

  override def inferAndApplyKnowledge(inferContext: InferContext): RunningExecution = {
    throw new IllegalStateException("An Execution that is not idle cannot infer knowledge!")
  }

  override def updateStructure(directedGraph: DeeplangGraph, nodes: Set[Id]): Execution =
    throw new IllegalStateException("Structure of an Execution that is not idle cannot be altered!")

  private def withRunningPartUpdated(update: (StatefulGraph) => StatefulGraph): Execution = {
    val updatedRunningPart = update(runningPart)
    val updatedGraph = graph.updateStates(updatedRunningPart)

    if (updatedRunningPart.isRunning) {
      updateState(updatedRunningPart, updatedGraph)
    } else {
      IdleExecution(updatedGraph, selectedNodes)
    }
  }

  protected def updateState(
    updatedRunningPart: StatefulGraph,
    updatedGraph: StatefulGraph): Execution
}

case class RunningExecution(
    override val graph: StatefulGraph,
    runningPart: StatefulGraph,
    selectedNodes: Set[Node.Id])
  extends StartedExecution(graph, runningPart, selectedNodes) {

  override def nodeStarted(id: Id): RunningExecution = {
    val updatedRunningPart = runningPart.nodeStarted(id)
    val updatedGraph = graph.updateStates(updatedRunningPart)
    copy(graph = updatedGraph, runningPart = updatedRunningPart)
  }

  override def abort: AbortedExecution = {
    AbortedExecution(graph, runningPart.abortQueued, selectedNodes)
  }

  override protected def updateState(
    updatedRunningPart: StatefulGraph,
    updatedGraph: StatefulGraph): Execution = {
    RunningExecution(updatedGraph, updatedRunningPart, selectedNodes)
  }
}

case class AbortedExecution(
  override val graph: StatefulGraph,
  runningPart: StatefulGraph,
  selectedNodes: Set[Node.Id])
  extends StartedExecution(graph, runningPart, selectedNodes) {

  override def nodeStarted(id: Id): AbortedExecution = {
    throw new IllegalStateException("A node cannot be started when execution is Aborted!")
  }

  override def abort: Execution = {
    throw new IllegalStateException("Once aborted execution cannot be aborted again!")
  }

  override protected def updateState(
    updatedRunningPart: StatefulGraph,
    updatedGraph: StatefulGraph): Execution = {
    AbortedExecution(updatedGraph, updatedRunningPart, selectedNodes)
  }
}
