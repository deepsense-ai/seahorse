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

import scala.collection.immutable.Iterable
import scala.util.{Failure, Success, Try}

import ai.deepsense.commons.exception.{DeepSenseException, DeepSenseFailure, FailureCode, FailureDescription}
import ai.deepsense.commons.models.Entity
import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang.inference.InferContext
import ai.deepsense.deeplang.{DOperable, DOperation}
import ai.deepsense.graph.DeeplangGraph.DeeplangNode
import ai.deepsense.graph.GraphKnowledge._
import ai.deepsense.graph.Node.Id
import ai.deepsense.graph._
import ai.deepsense.graph.nodestate._
import ai.deepsense.models.workflows.{EntitiesMap, ExecutionReport, NodeState, NodeStateWithResults}
import ai.deepsense.reportlib.model.ReportContent

case class StatefulGraph(
    directedGraph: DeeplangGraph,
    states: Map[Node.Id, NodeStateWithResults],
    executionFailure: Option[FailureDescription])
  extends TopologicallySortable[DOperation]
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

  def runningNodes: Seq[DeeplangNode] = {
    val queuedIds = states.collect { case (id, nodeState) if nodeState.isRunning => id }
    queuedIds.map(id => node(id))
  }.toSeq

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

  def node(id: Node.Id): DeeplangNode = directedGraph.node(id)

  def nodes: Set[DeeplangNode] = directedGraph.nodes

  // Delegated methods (TopologicallySortable)

  override def topologicallySorted: Option[List[DeeplangNode]] =
    directedGraph.topologicallySorted

  override def allPredecessorsOf(id: Id): Set[DeeplangNode] =
    directedGraph.allPredecessorsOf(id)

  override def predecessors(id: Id): IndexedSeq[Option[Endpoint]] =
    directedGraph.predecessors(id)

  override def edges: Set[Edge] =
    directedGraph.edges

  override def successors(id: Id): IndexedSeq[Set[Endpoint]] =
    directedGraph.successors(id)

  /**
   * Tells the graph to infer Knowledge.
   * Graph checks if it is still correct in context of the inferred knowledge.
   */
  def inferAndApplyKnowledge(context: InferContext): StatefulGraph = {
    Try(inferKnowledge(context, memorizedKnowledge)) match {
      case Success(knowledge) =>
        handleInferredKnowledge(knowledge)
      case Failure(ex: CyclicGraphException) =>
        fail(StatefulGraph.cyclicGraphFailureDescription)
      case Failure(ex) =>
        logger.error("Failed", ex)
        fail(StatefulGraph.genericFailureDescription(ex))
    }
  }

  def memorizedKnowledge: GraphKnowledge = {
    GraphKnowledge(states.flatMap { case (nodeId, nodeState) =>
      nodeState.knowledge.map(
        knowledge => (nodeId, knowledge))
    })
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

  def clearKnowledge(nodeId: Node.Id): StatefulGraph = {
    copy(states = clearChildrenKnowledge(states, nodeId))
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

  def notExecutedNodes: Set[Node.Id] = {
    states.collect { case (nodeId, state) if state.isDraft || state.isAborted => nodeId }.toSet
  }

  private def markChildrenDraft(
      states: Map[Node.Id, NodeStateWithResults],
      draftNodeId: Node.Id): Map[Node.Id, NodeStateWithResults] = {
    recursiveStateUpdate(states, draftNodeId, _.draft)
  }

  private def clearChildrenKnowledge(
      states: Map[Node.Id, NodeStateWithResults],
      nodeToClearId: Node.Id): Map[Node.Id, NodeStateWithResults] = {
    recursiveStateUpdate(states, nodeToClearId, _.clearKnowledge)
  }

  private def recursiveStateUpdate(
        states: Map[Node.Id, NodeStateWithResults],
        nodeId: Node.Id,
        updateNodeState: (NodeStateWithResults => NodeStateWithResults))
      : Map[Node.Id, NodeStateWithResults] = {
    val children: Set[Node.Id] = directedGraph.successorsOf(nodeId)
    val previousState = states.get(nodeId)
    val updatedState =
      previousState.map(s => states.updated(nodeId, updateNodeState(s))).getOrElse(states)
    if (children.isEmpty) {
      updatedState
    } else {
      children.toSeq.foldLeft(updatedState){ (states, node) =>
        recursiveStateUpdate(states, node, updateNodeState)
      }
    }
  }

  protected def handleInferredKnowledge(knowledge: GraphKnowledge): StatefulGraph = {
    if (knowledge.errors.nonEmpty) {
      val description = FailureDescription(
        DeepSenseFailure.Id.randomId,
        FailureCode.IncorrectWorkflow,
        "Incorrect workflow",
        Some("Provided workflow cannot be launched, because it contains errors")
      )
      copy(states = updateStates(knowledge), executionFailure = Some(description))
    } else {
      val updatedStates = states.map { case (nodeId, nodeState) =>
        (nodeId, nodeState.withKnowledge(knowledge.getResult(nodeId)))
      }
      copy(states = updatedStates)
    }
  }

  protected def updateStates(knowledge: GraphKnowledge): Map[Id, NodeStateWithResults] = {
    knowledge.errors.toSeq.foldLeft(states) {
      case (modifiedStates, (id, nodeErrors)) =>
        // Inner workflow nodes are not in the states map; don't try to update their state
        if (states.contains(id)) {
          modifiedStates
            .updated(id, states(id).fail(nodeErrorsFailureDescription(id, nodeErrors)))
        } else {
          modifiedStates
        }
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
                NodeState(nodestate.Completed(_, _, results), _), dOperables, _) =>
              dOperables(results(portIndex))
            case NodeStateWithResults(NodeState(otherStatus, _), _, _) =>
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
    val updatedStates = {
      val newNodeState = f(states(id))
      val withNodeUpdated = states.updated(id, newNodeState)
      val successorsOfFailedAborted = if (newNodeState.isFailed) {
        abortSuccessors(withNodeUpdated, id)
      } else {
        withNodeUpdated
      }
      if (nodeRunningOrReadyNodeExist(successorsOfFailedAborted)) {
        successorsOfFailedAborted
      } else {
        abortUnfinished(successorsOfFailedAborted)
      }
    }
    copy(states = updatedStates)
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

  private def abortUnfinished(
      unfinished: Map[Id, NodeStateWithResults]): Map[Id, NodeStateWithResults] = {
    unfinished.mapValues(abortIfAbortable)
  }

  private def abortIfAbortable(nodeStateWithResults: NodeStateWithResults) = nodeStateWithResults match {
    case NodeStateWithResults(state@NodeState(status, _), _, _) =>
      val newStatus = status match {
        case _: Running | _: Queued | _: Draft => status.abort
        case _ => status
      }
      nodeStateWithResults.copy(nodeState = state.copy(nodeStatus = newStatus))
  }

  private def abortSuccessors(allNodes: Map[Id, NodeStateWithResults], id: Node.Id): Map[Id, NodeStateWithResults] = {
    val unsuccessfulAtBeginning: Set[DeeplangNode] = Set(node(id))
    val sorted = {
      val sortedOpt = topologicallySorted
      assert(sortedOpt.nonEmpty)
      sortedOpt.get
    }
    // if A is a predecessor of B, we will visit A first, so if A is failed/aborted, B will also be aborted.
    val (_, updatedStates) = sorted
      .foldLeft[(Set[DeeplangNode], Map[Id, NodeStateWithResults])](unsuccessfulAtBeginning, allNodes) {
        case ((unsuccessfulNodes, statesMap), node) =>
          if (allPredecessorsOf(node.id).intersect(unsuccessfulNodes).nonEmpty) {
            (unsuccessfulNodes + node, statesMap.updated(node.id, abortIfAbortable(statesMap(node.id))))
          } else {
            (unsuccessfulNodes, statesMap)
          }
      }
    updatedStates
  }
}

object StatefulGraph {
  def apply(
    nodes: Set[DeeplangNode] = Set(),
    edges: Set[Edge] = Set()): StatefulGraph = {
    val states = nodes.map(node =>
      node.id -> NodeStateWithResults(
        NodeState(
          nodestate.Draft(),
          Some(EntitiesMap())),
          Map(),
          None)
    ).toMap
    StatefulGraph(DeeplangGraph(nodes, edges), states, None)
  }

  protected def predecessorsReady(
      id: Node.Id,
      directedGraph: DeeplangGraph,
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

case class ReadyNode(node: DeeplangNode, input: Seq[DOperable])
