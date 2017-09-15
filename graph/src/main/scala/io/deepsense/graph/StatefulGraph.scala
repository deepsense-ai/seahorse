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

package io.deepsense.graph

import scala.util.{Failure, Success, Try}

import io.deepsense.commons.exception.{DeepSenseException, DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph.GraphKnowledge._
import io.deepsense.graph.Node.Id
import io.deepsense.graph.graphstate._
import io.deepsense.graph.nodestate._
import io.deepsense.models.entities.Entity

case class StatefulGraph(
    directedGraph: DirectedGraph,
    states: Map[Node.Id, NodeState],
    state: GraphState)
  extends TopologicallySortable
  with KnowledgeInference
  with NodeInferenceImpl {

  state match {
    case graphstate.Draft =>
      require(allNodesDraft(states), "Draft Graph can have only Draft nodes")
    case graphstate.Running =>
      require(
        nodeRunningOrReadyNodeExist(states),
        "Running graph should have ready nodes or running nodes")
    case graphstate.Completed =>
      require(
        allNodesCompleted(states),
        s"Completed graph can have only Completed nodes but has: $states")
    case graphstate.Aborted =>
      require(
        allNodesFinished(states),
        s"Aborted Graph can have only Completed, Failed or Aborted nodes but has: $states")
    case graphstate.Failed(error) =>
      require(
        allNodesFinished(states),
        s"Failed Graph can have only Completed, Failed or Aborted nodes but has: $states")
  }

  if (!state.isDraft) {
    require(noDraftNodes(states),
      s"Graph in state ${state.name} cannot contain Draft nodes: $states")
  }

  /**
   * Tells the graph that an execution of a node has started.
   */
  def nodeStarted(id: Node.Id): StatefulGraph =
    changeStatus(id)(_.start)

  /**
   * Tells the graph that an exception has occurred during the execution of a node.
   */
  def nodeFailed(id: Node.Id, cause: Exception): StatefulGraph = {
    val description = cause match {
      case e: DeepSenseException => e.failureDescription
      case e => genericNodeFailureDescription(e)
    }
    changeStatus(id)(_.fail(description))
  }

  /**
   * Tells the graph that the execution of a node has successfully finished.
   */
  def nodeFinished(id: Node.Id, results: Seq[Entity.Id]): StatefulGraph =
    changeStatus(id)(_.finish(results))

  /**
   * Tells the graph that the execution has failed. Eg. there was an issue other
   * than node exception that makes execution impossible.
   */
  def fail(errors: FailureDescription): StatefulGraph = {
    val updatedStates = abortUnfinished(states)
    copy(states = updatedStates, state = graphstate.Failed(errors))
  }

  /**
   * Lists all nodes that can be executed (that is: all their predecessors completed successfully).
   */
  def readyNodes: Seq[ReadyNode] = {
    val queuedIds = states.collect { case (id, nodestate.Queued) => id }
    val inputs = queuedIds.collect { case id if predecessorsReady(id) => (id, inputFor(id).get) }
    inputs.map { case (id, input) => ReadyNode(directedGraph.node(id), input) }.toSeq
  }

  /**
   * Tells the graph that it was enqueued for execution.
   */
  def enqueue: StatefulGraph = {
    val updatedStates = states.mapValues(_.enqueue)
    val newState = if (nodes.isEmpty) {
      graphstate.Completed
    } else {
      graphstate.Running
    }
    copy(states = updatedStates, state = newState)
  }

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
        fail(StatefulGraph.genericFailureDescription(ex))
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
      val updatedState = graphstate.Failed(description)
      copy(states = updatedStates, state = updatedState)
    } else {
      this
    }
  }

  protected def updateStates(knowledge: GraphKnowledge): Map[Id, NodeState] = {
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

  protected def inputFor(id: Node.Id): Option[Seq[Entity.Id]] = {
    if (predecessorsReady(id)) {
      val entities = directedGraph
        .predecessors(id).flatten.map {
        case Endpoint(predecessorId, portIndex) =>
          states(predecessorId) match {
            case nodestate.Completed(_, _, results) => results(portIndex)
            case otherState => throw new IllegalStateException(
              s"Cannot collect inputs for node ${directedGraph.node(id)}" +
                s" because one of its predecessors was in '$otherState' " +
                s"instead of Completed: ${directedGraph.node(predecessorId)}")
        }
      }
      Some(entities)
    } else {
      None
    }
  }

  protected def changeStatus(id: Node.Id)(f: (NodeState) => NodeState): StatefulGraph = {
    val updatedStates = states.updated(id, f(states(id)))
    if (nodeRunningOrReadyNodeExist(updatedStates)) {
      copy(states = updatedStates)
    } else {
      if (allNodesCompleted(updatedStates)) {
        copy(states = updatedStates, state = graphstate.Completed)
      } else {
        copy(states = abortUnfinished(updatedStates), state = graphstate.Failed(
          FailureDescription(
            DeepSenseFailure.Id.randomId,
            FailureCode.NodeFailure,
            title = "One or more nodes failed",
            message = Some("Provided workflow could not finish successfully" +
              " because one or more nodes has failed"))))
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

  private def allNodesDraft(states: Map[Node.Id, NodeState]): Boolean =
    states.values.forall(_.isDraft)

  private def nodeRunningOrReadyNodeExist(
      states: Map[Node.Id, NodeState]): Boolean = {
    val readyNodeExists = states.exists {
      case (id, s) if s.isQueued =>
        StatefulGraph.predecessorsReady(id, directedGraph, states)
      case _ => false
    }

    states.values.exists(_.isRunning)  || readyNodeExists
  }

  private def allNodesCompleted(states: Map[Node.Id, NodeState]): Boolean =
    states.values.forall(_.isCompleted)

  private def allNodesFinished(states: Map[Node.Id, NodeState]): Boolean = {
    states.values.forall { s =>
      s.isAborted || s.isCompleted || s.isFailed
    }
  }

  private def noDraftNodes(states: Map[Node.Id, NodeState]): Boolean =
    !states.values.exists(_.isDraft)

  private def abortUnfinished(unfinished: Map[Id, NodeState]): Map[Id, NodeState] = {
    unfinished.mapValues {
      case r: Running => r.abort
      case nodestate.Queued => nodestate.Queued.abort
      case nodestate.Draft => nodestate.Draft.abort
      case x => x
    }
  }
}

object StatefulGraph {
  def apply(
    nodes: Set[Node] = Set(),
    edges: Set[Edge] = Set()): StatefulGraph = {
    val states = nodes.map(n => n.id -> nodestate.Draft).toMap
    StatefulGraph(DirectedGraph(nodes, edges), states, graphstate.Draft)
  }

  protected def predecessorsReady(
      id: Node.Id,
      directedGraph: DirectedGraph,
      states: Map[Node.Id, NodeState]): Boolean = {
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

case class ReadyNode(node: Node, input: Seq[Entity.Id])


