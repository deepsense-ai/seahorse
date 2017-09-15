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

import scala.util.{Failure, Success, Try}

import io.deepsense.commons.exception.{DeepSenseException, DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.commons.models.Entity
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph.GraphKnowledge._
import io.deepsense.graph.Node.Id
import io.deepsense.graph._
import io.deepsense.graph.nodestate._
import io.deepsense.models.workflows.{ExecutionReport, EntitiesMap, NodeState, NodeStateWithResults}
import io.deepsense.reportlib.model.ReportContent

case class StatefulGraph(
    directedGraph: DirectedGraph,
    states: Map[Node.Id, NodeStateWithResults],
    executionFailure: Option[FailureDescription])
  extends TopologicallySortable
  with KnowledgeInference
  with NodeInferenceImpl
  with Logging {

  require(states.size == directedGraph.nodes.size,
    "A graph should know states of all its nodes (and only its nodes)!")

  /**
   * Tells the graph that an execution of a node has started.
   */
  def nodeStarted(id: Node.Id): StatefulGraph =
    changeState(id)(_.start)

  /**
   * Tells the graph that an exception has occurred during the execution of a node.
   */
  def nodeFailed(id: Node.Id, cause: Exception): StatefulGraph = {
    val description = cause match {
      case e: DeepSenseException => e.failureDescription
      case e => genericNodeFailureDescription(e)
    }
    changeState(id)(_.fail(description))
  }

  /**
   * Tells the graph that the execution of a node has successfully finished.
   */
  def nodeFinished(
      id: Node.Id,
      entitiesIds: Seq[Entity.Id],
      reports: Map[Entity.Id, ReportContent],
      dOperables: Map[Entity.Id, DOperable]): StatefulGraph = {
    changeState(id)(_.finish(entitiesIds, reports, dOperables))
  }

  /**
   * Tells the graph that the execution has failed. Eg. there was an issue other
   * than node exception that makes execution impossible.
   */
  def fail(errors: FailureDescription): StatefulGraph = {
    val updatedStates = abortUnfinished(states)
    copy(states = updatedStates, executionFailure = Some(errors))
  }

  /**
   * Lists all nodes that can be executed (that is: all their predecessors completed successfully).
   */
  def readyNodes: Seq[ReadyNode] = {
    val queuedIds = states.collect { case (id, nodeState) if nodeState.isQueued => id }
    val inputs = queuedIds.collect { case id if predecessorsReady(id) => (id, inputFor(id).get) }
    inputs.map { case (id, input) => ReadyNode(directedGraph.node(id), input) }.toSeq
  }

  /**
   * Tells the graph that it was enqueued for execution.
   */
  def enqueue: StatefulGraph = {
    if (isRunning) {
      throw new IllegalStateException("Cannot enqueue running graph")
    }
    val updatedStates = states.mapValues(state => state.enqueue)
    copy(states = updatedStates)
  }

  def isRunning: Boolean = states.valuesIterator.exists(state => state.isQueued || state.isRunning)

  def withoutFailedNodes: Boolean = !hasFailedNodes

  def hasFailedNodes: Boolean = states.valuesIterator.exists(_.isFailed)

  def size: Int = directedGraph.size

  def node(id: Node.Id): Node = directedGraph.node(id)

  def nodes: Set[Node] = directedGraph.nodes

  // Delegated methods (TopologicallySortable)

  override def topologicallySorted: Option[List[Node]] = directedGraph.topologicallySorted

  override def allPredecessorsOf(id: Id): Set[Node] = directedGraph.allPredecessorsOf(id)

  override def predecessors(id: Id): IndexedSeq[Option[Endpoint]] = directedGraph.predecessors(id)

  override def edges: Set[Edge] = directedGraph.edges

  override def successors(id: Id): IndexedSeq[Set[Endpoint]] = directedGraph.successors(id)

  /**
   * Tells the graph to infer Knowledge.
   * Graph checks if it is still correct in context of the inferred knowledge.
   */
  def inferAndApplyKnowledge(context: InferContext): StatefulGraph = {
    Try(inferKnowledge(context)) match {
      case Success(knowledge) =>
        handleInferredKnowledge(knowledge)
      case Failure(ex: CyclicGraphException) =>
        fail(StatefulGraph.cyclicGraphFailureDescription)
      case Failure(ex) =>
        logger.error("Failed", ex)
        fail(StatefulGraph.genericFailureDescription(ex))
    }
  }

  def updateStates(changedGraph: StatefulGraph): StatefulGraph = {
    val updatedStates = states ++ changedGraph.states
    copy(
      states = updatedStates,
      executionFailure = changedGraph.executionFailure)
  }

  def subgraph(nodes: Set[Node.Id]): StatefulGraph = {
    val directedsubgraph = directedGraph.subgraph(nodes)
    val substatuses = states.filter {
      case (id, _) => directedsubgraph.nodes.map(_.id).contains(id)
    }
    copy(directedsubgraph, substatuses)
  }

  def draft(nodeId: Node.Id): StatefulGraph = {
    copy(states = markChildrenDraft(states, nodeId))
  }

  def enqueueDraft: StatefulGraph = {
    val enqueued = states.mapValues(state => if (state.isDraft) state.enqueue else state)
    copy(states = enqueued)
  }

  def abortQueued: StatefulGraph = {
    val aborted = states.mapValues(state => if (state.isQueued) state.abort else state)
    copy(states = aborted)
  }

  def executionReport: ExecutionReport =
    ExecutionReport(states.mapValues(_.nodeState), executionFailure)

  private def markChildrenDraft(
    states: Map[Node.Id, NodeStateWithResults],
    draftNodeId: Node.Id): Map[Node.Id, NodeStateWithResults] = {
    val children: Set[Node.Id] = directedGraph.successorsOf(draftNodeId)
    val previousState = states.get(draftNodeId)
    val draftedState =
      previousState.map(s => states.updated(draftNodeId, s.draft)).getOrElse(states)
    if (children.isEmpty) {
      draftedState
    } else {
      children.toSeq.foldLeft(draftedState){ (states, node) =>
        markChildrenDraft(states, node)
      }
    }
  }

  protected def handleInferredKnowledge(knowledge: GraphKnowledge): StatefulGraph = {
    if (knowledge.errors.nonEmpty) {
      val description = FailureDescription(
        DeepSenseFailure.Id.randomId,
        FailureCode.IncorrectWorkflow,
        "Incorrect workflow",
        Some("Provided workflow cannot be launched, because it contains errors"),
        details = knowledge.errors.map {
          case (id, errors) => (id.toString, errors.map(_.toString).mkString("\n"))
        }
      )
      val updatedStates = states.mapValues(_.abort)
      copy(states = updatedStates, executionFailure = Some(description))
    } else {
      this
    }
  }

  protected def updateStates(knowledge: GraphKnowledge): Map[Id, NodeStateWithResults] = {
    knowledge.errors.toSeq.foldLeft(states) {
      case (modifiedStates, (id, nodeErrors)) =>
        modifiedStates
          .updated(id, states(id).fail(nodeErrorsFailureDescription(id, nodeErrors)))
    }
  }

  protected def nodeErrorsFailureDescription(
      nodeId: Node.Id,
      nodeErrors: InferenceErrors): FailureDescription = {
    FailureDescription(
      DeepSenseFailure.Id.randomId,
      FailureCode.IncorrectNode,
      title = "Incorrect node",
      message = Some("Node contains errors that prevent workflow from being executed:\n\n" +
        nodeErrors.map(e => "* " + e.message).mkString("\n")))
  }

  protected def predecessorsReady(id: Node.Id): Boolean =
    StatefulGraph.predecessorsReady(id, directedGraph, states)

  protected def inputFor(id: Node.Id): Option[Seq[DOperable]] = {
    if (predecessorsReady(id)) {
      val entities = directedGraph.predecessors(id).flatten.map {
        case Endpoint(predecessorId, portIndex) =>
          states(predecessorId) match {
            case NodeStateWithResults(
                NodeState(nodestate.Completed(_, _, results), _), dOperables) =>
              dOperables(results(portIndex))
            case NodeStateWithResults(NodeState(otherStatus, _), _) =>
              throw new IllegalStateException(
                s"Cannot collect inputs for node ${directedGraph.node(id)}" +
                s" because one of its predecessors was in '$otherStatus' " +
                s"instead of Completed: ${directedGraph.node(predecessorId)}")
        }
      }
      Some(entities)
    } else {
      None
    }
  }

  private def changeState(id: Node.Id)(
      f: (NodeStateWithResults) => NodeStateWithResults): StatefulGraph = {
    val updatedStates = states.updated(id, f(states(id)))
    if (nodeRunningOrReadyNodeExist(updatedStates)) {
      copy(states = updatedStates)
    } else {
      if (allNodesCompleted(updatedStates)) {
        copy(states = updatedStates)
      } else {
        copy(states = abortUnfinished(updatedStates))
      }
    }
  }

  protected def genericNodeFailureDescription(exception: Exception): FailureDescription = {
    FailureDescription(DeepSenseFailure.Id.randomId,
      FailureCode.UnexpectedError, "Execution of a node failed",
      Some(s"Error while executing a node: ${exception.getMessage}"),
      FailureDescription.stacktraceDetails(exception.getStackTrace)
    )
  }

  private def nodeRunningOrReadyNodeExist(states: Map[Node.Id, NodeStateWithResults]): Boolean = {
    val readyNodeExists = states.exists {
      case (id, s) if s.isQueued =>
        StatefulGraph.predecessorsReady(id, directedGraph, states)
      case _ => false
    }

    states.values.exists(_.isRunning)  || readyNodeExists
  }

  private def allNodesCompleted(states: Map[Node.Id, NodeStateWithResults]): Boolean =
    states.values.forall(_.isCompleted)

  private def abortUnfinished(
      unfinished: Map[Id, NodeStateWithResults]): Map[Id, NodeStateWithResults] = {
    unfinished.mapValues {
      case nodeStateWithResults@NodeStateWithResults(state@NodeState(status, _), _) =>
        val newStatus = status match {
        case r: Running => r.abort
        case nodestate.Queued => nodestate.Queued.abort
        case nodestate.Draft => nodestate.Draft.abort
        case x => x
      }
      nodeStateWithResults.copy(nodeState = state.copy(nodeStatus = newStatus))
    }
  }
}

object StatefulGraph {
  def apply(
    nodes: Set[Node] = Set(),
    edges: Set[Edge] = Set()): StatefulGraph = {
    val states = nodes.map(node =>
      node.id -> NodeStateWithResults(NodeState(nodestate.Draft, Some(EntitiesMap())), Map())).toMap
    StatefulGraph(DirectedGraph(nodes, edges), states, None)
  }

  protected def predecessorsReady(
      id: Node.Id,
      directedGraph: DirectedGraph,
      states: Map[Node.Id, NodeStateWithResults]): Boolean = {
    directedGraph.predecessors(id).forall {
      case Some(Endpoint(nodeId, _)) =>
        states(nodeId).isCompleted
      case None =>
        false
    }
  }

  def cyclicGraphFailureDescription: FailureDescription = {
    FailureDescription(DeepSenseFailure.Id.randomId,
      FailureCode.IncorrectWorkflow, "Cyclic workflow",
      Some("Provided workflow cannot be launched, because it contains a cycle")
    )
  }

  def genericFailureDescription(e: Throwable): FailureDescription = {
    FailureDescription(DeepSenseFailure.Id.randomId,
      FailureCode.LaunchingFailure, "Launching failure",
      Some(s"Error while launching workflow: ${e.getMessage}"),
      FailureDescription.stacktraceDetails(e.getStackTrace)
    )
  }
}

case class ReadyNode(node: Node, input: Seq[DOperable])
