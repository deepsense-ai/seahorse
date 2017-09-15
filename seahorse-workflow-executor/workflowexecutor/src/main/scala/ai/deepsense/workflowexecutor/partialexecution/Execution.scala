/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowexecutor.partialexecution

import ai.deepsense.commons.exception.FailureDescription
import ai.deepsense.commons.models.Entity
import ai.deepsense.deeplang.DOperable
import ai.deepsense.deeplang.inference.InferContext
import ai.deepsense.graph.DeeplangGraph.DeeplangNode
import ai.deepsense.graph.Node.Id
import ai.deepsense.graph._
import ai.deepsense.models.workflows.{ExecutionReport, NodeStateWithResults}
import ai.deepsense.reportlib.model.ReportContent

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

sealed abstract class Execution {
  final def node(id: Node.Id): DeeplangNode = graph.node(id)

  def executionReport: ExecutionReport = {
    ExecutionReport(graph.states.mapValues(_.nodeState), graph.executionFailure)
  }

  type NodeStates = Map[Node.Id, NodeStateWithResults]

  def graph: StatefulGraph
  def nodeStarted(id: Node.Id): Execution
  def nodeFailed(id: Node.Id, cause: Exception): Execution
  def nodeFinished(
    id: Node.Id,
    resultsIds: Seq[Entity.Id],
    reports: Map[Entity.Id, ReportContent],
    dOperables: Map[Entity.Id, DOperable]): Execution
  def enqueue: Execution
  def inferAndApplyKnowledge(inferContext: InferContext): Execution
  def abort: Execution

}

case class IdleExecution(
    override val graph: StatefulGraph,
    selectedNodes: Set[Node.Id] = Set.empty)
  extends Execution {

  require(graph.readyNodes.isEmpty, "Idle executor must not have ready nodes")

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

  def updateStructure(
      newStructure: DeeplangGraph,
      nodes: Set[Id] = Set.empty): IdleExecution = {
    val selected = Execution.selectedNodes(newStructure, nodes.toSeq)
    val substructure = newStructure.subgraph(selected)
    val newStates = findStates(newStructure, substructure, selected)
    val graph = StatefulGraph(newStructure, newStates, None)
    IdleExecution(graph, selected)
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
      case Node(id, _) => id -> graph.states.getOrElse(id, NodeStateWithResults.draft)
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

}

abstract class StartedExecution(
  fullGraph: StatefulGraph,
  runningPart: StatefulGraph,
  selectedNodes: Set[Node.Id])
  extends Execution {

  override def graph: StatefulGraph = {
    val mergedStates = fullGraph.states ++ runningPart.states
    // Assumes runningPart is subgraph of fullGraph
    StatefulGraph(fullGraph.directedGraph, mergedStates, runningPart.executionFailure)
  }

  override def nodeFinished(
      id: Node.Id,
      resultsIds: Seq[Entity.Id],
      reports: Map[Entity.Id, ReportContent],
      dOperables: Map[Entity.Id, DOperable]): Execution = {
    withRunningPartUpdated(_.nodeFinished(id, resultsIds, reports, dOperables))
  }

  override def nodeFailed(id: Id, cause: Exception): Execution =
    withRunningPartUpdated(_.nodeFailed(id, cause))

  override def enqueue: Execution = {
    throw new IllegalStateException("An Execution that is not idle cannot be enqueued!")
  }

  override def inferAndApplyKnowledge(inferContext: InferContext): RunningExecution = {
    throw new IllegalStateException("An Execution that is not idle cannot infer knowledge!")
  }

  final def withRunningPartUpdated(
    update: (StatefulGraph) => StatefulGraph): Execution = {
    val updatedRunningPart = update(runningPart)
    val updatedFullGraph = fullGraph.updateStates(updatedRunningPart)

    if (updatedRunningPart.isRunning) {
      copyGraphs(updatedRunningPart, updatedFullGraph)
    } else {
      IdleExecution(updatedFullGraph, selectedNodes)
    }
  }

  protected def copyGraphs(
    updatedRunningPart: StatefulGraph,
    updatedFullGraph: StatefulGraph): Execution
}

case class RunningExecution(
    fullGraph: StatefulGraph,
    runningPart: StatefulGraph,
    selectedNodes: Set[Node.Id])
  extends StartedExecution(fullGraph, runningPart, selectedNodes) {

  override def nodeStarted(id: Id): RunningExecution = {
    val updatedRunningPart = runningPart.nodeStarted(id)
    val updatedFullGraph = fullGraph.updateStates(updatedRunningPart)
    copy(fullGraph = updatedFullGraph, runningPart = updatedRunningPart)
  }

  override def abort: Execution = {
    AbortedExecution(graph, runningPart.abortQueued, selectedNodes)
  }

  override protected def copyGraphs(
    updatedRunningPart: StatefulGraph,
    updatedGraph: StatefulGraph): Execution = {
    RunningExecution(updatedGraph, updatedRunningPart, selectedNodes)
  }

}

case class AbortedExecution(
  val fullGraph: StatefulGraph,
  runningPart: StatefulGraph,
  selectedNodes: Set[Node.Id])
  extends StartedExecution(fullGraph, runningPart, selectedNodes) {

  override def nodeStarted(id: Id): AbortedExecution = {
    throw new IllegalStateException("A node cannot be started when execution is Aborted!")
  }

  override def abort: Execution = {
    throw new IllegalStateException("Once aborted execution cannot be aborted again!")
  }

  override protected def copyGraphs(
    updatedRunningPart: StatefulGraph,
    updatedFullGraph: StatefulGraph): Execution = {
    AbortedExecution(updatedFullGraph, updatedRunningPart, selectedNodes)
  }
}
