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

package io.deepsense.workflowexecutor

import scala.collection.mutable

import akka.actor._

import io.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.graph._
import io.deepsense.models.entities.Entity
import io.deepsense.models.workflows.EntitiesMap
import io.deepsense.reportlib.model.ReportContent
import io.deepsense.workflowexecutor.communication.{Connect, ExecutionStatus}
import io.deepsense.workflowexecutor.partialexecution.{Execution, PartialExecution}

/**
 * WorkflowExecutorActor coordinates execution of a workflow by distributing work to
 * WorkflowNodeExecutorActors and collecting results.
 */
class WorkflowExecutorActor(
    executionContext: ExecutionContext,
    nodeExecutorFactory: GraphNodeExecutorFactory,
    executionFactory: ExecutionFactory,
    statusListener: Option[ActorRef],
    publisher: Option[ActorSelection])
  extends Actor
  with Logging {

  import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages._

  val dOperableCache: mutable.Map[Entity.Id, DOperable] = mutable.Map.empty
  val reports: mutable.Map[Entity.Id, ReportContent] = mutable.Map.empty
  val progressReporter = WorkflowProgress()
  val workflowId = self.path.name

  def launched(execution: Execution): Receive = {
    case NodeStarted(id) => nodeStarted(id)
    case NodeCompleted(id, nodeExecutionResult) =>
      nodeCompleted(id,
        nodeExecutionResult,
        execution)
    case NodeFailed(id, failureDescription) =>
      nodeFailed(id, failureDescription, execution)
    case Connect(_) =>
      sendWorkflowStatus(execution)
    case l: Launch =>
      logger.info("It is illegal to Launch a graph when the execution is in progress.")
  }

  private def reset(): Unit = {
    dOperableCache.clear()
    reports.clear()
  }

  def finished(finishedExecution: Execution): Receive = {
    case Launch(graph, nodes) =>
      reset()
      val updatedStructure = finishedExecution.updateStructure(graph)
      launch(updatedStructure, nodes)
    case Connect(_) =>
      sendWorkflowStatus(finishedExecution)
  }

  override def receive: Receive = {
    case Launch(graph, nodes) => // TODO: Pass DirectedGraph instead of StatefulGraph?
      val execution = executionFactory.create(graph)
      launch(execution, nodes)
    case Connect(_) =>
      sendWorkflowStatus(executionFactory.empty)
  }

  def launch(execution: Execution, nodes: Seq[Node.Id]): Unit = {
    val inferred = execution.inferAndApplyKnowledge(executionContext.inferContext)
    val enqueued = if (inferred.state.isFailed) {
      inferred
    } else {
      inferred.enqueue(nodes)
    }
    checkExecutionState(enqueued)
  }

  def checkExecutionState(execution: Execution): Unit = {
    val updatedGraph = execution.state match { // TODO What are the valid states?
      case graphstate.Draft =>
        logger.error("Graph in state 'Draft'! This should not happen!")
        execution
      case graphstate.Running =>
        val launchedGraph = launchReadyNodes(execution)
        context.unbecome()
        context.become(launched(launchedGraph))
        launchedGraph
      case _ =>
        logger.debug(s"End of execution, state=${execution.state}")
        context.unbecome()
        context.become(finished(execution))
        execution
    }
    sendWorkflowStatus(updatedGraph)
  }

  def sendWorkflowStatus(execution: Execution): Unit = {
    val nodeStates = execution.states
    val graphState = execution.state // TODO Is graph state needed?
    val status = ExecutionStatus(
      graphState,
      nodeStates,
      EntitiesMap(dOperableCache.toMap, reports.toMap))

    logger.debug(s"Status for '$workflowId': $status")
    statusListener.foreach(_ ! status)
    publisher.foreach(_ ! status)
  }

  def launchReadyNodes(execution: Execution): Execution = {
    logger.debug("launchReadyNodes")
    execution.readyNodes.foldLeft(execution) {
      case (g, readyNode) =>
        val input = readyNode.input.map(dOperableCache(_)).toVector
        val nodeRef = nodeExecutorFactory
          .createGraphNodeExecutor(context, executionContext, readyNode.node, input)
        nodeRef ! WorkflowNodeExecutorActor.Messages.Start()
        logger.debug(s"Starting node $readyNode")
      g.nodeStarted(readyNode.node.id)
    }
  }

  def nodeStarted(id: Node.Id): Unit = logger.debug("{}", NodeStarted(id))

  def nodeCompleted(
      id: Node.Id,
      nodeExecutionResults: NodeExecutionResults,
      execution: Execution): Unit = {
    logger.debug(s"Node ${execution.node(id)} completed!")
    processResults(nodeExecutionResults)
    val entityIds = nodeExecutionResults.doperables.keys.toSeq
    val updatedGraph = execution.nodeFinished(id, entityIds)
    finalizeNodeExecutionEnd(updatedGraph)
  }

  def nodeFailed(
      id: Node.Id,
      cause: Exception,
      execution: Execution): Unit = {
    logger.warn(s"Node ${execution.node(id)} failed!", cause)
    val withFailedNode = execution.nodeFailed(id, cause)
    finalizeNodeExecutionEnd(withFailedNode)
  }

  def processResults(nodeExecutionResults: NodeExecutionResults): Unit = {
    reports ++= nodeExecutionResults.reports
    dOperableCache ++= nodeExecutionResults.doperables
  }

  def finalizeNodeExecutionEnd(execution: Execution): Unit = {
    progressReporter.logProgress(execution)
    checkExecutionState(execution)
  }
}

object WorkflowExecutorActor {
  def props(
      ec: ExecutionContext,
      publisher: Option[ActorSelection] = None,
      statusListener: Option[ActorRef] = None): Props =
    Props(new WorkflowExecutorActor(ec,
      new GraphNodeExecutorFactoryImpl,
      new PartialExecutionFactoryImpl,
      statusListener,
      publisher))
  type Results = Map[Entity.Id, DOperable]

  object Messages {
    sealed trait Message
    case class Launch(
        graph: DirectedGraph,
        nodes: Seq[Node.Id] = Seq.empty)
      extends Message
    case class NodeStarted(nodeId: Node.Id) extends Message
    case class NodeCompleted(id: Node.Id, results: NodeExecutionResults) extends Message
    case class NodeFailed(id: Node.Id, cause: Exception) extends Message
  }

  def inferenceErrorsDebugDescription(graphKnowledge: GraphKnowledge): FailureDescription = {
    FailureDescription(
      DeepSenseFailure.Id.randomId,
      FailureCode.IncorrectWorkflow,
      title = "Incorrect workflow",
      message = Some("Provided workflow cannot be launched, because it contains errors"),
      details = graphKnowledge.errors.map {
        case (id, errors) => (id.toString, errors.map(_.toString).mkString("\n"))
      }
    )
  }
}

trait GraphNodeExecutorFactory {
  def createGraphNodeExecutor(
    context: ActorContext,
    executionContext: ExecutionContext,
    node: Node,
    input: Vector[DOperable]): ActorRef
}

class GraphNodeExecutorFactoryImpl extends GraphNodeExecutorFactory {

  override def createGraphNodeExecutor(
      context: ActorContext,
      executionContext: ExecutionContext,
      node: Node,
      input: Vector[DOperable]): ActorRef = {
    val props = Props(new WorkflowNodeExecutorActor(executionContext, node, input))
    context.actorOf(props, s"node-executor-${node.id.value.toString}")
  }
}

trait ExecutionFactory {
  def create(directedGraph: DirectedGraph): Execution
  def empty: Execution
}

class PartialExecutionFactoryImpl extends ExecutionFactory {
  override def create(directedGraph: DirectedGraph): Execution = {
    PartialExecution(directedGraph)
  }

  override val empty: Execution = PartialExecution(DirectedGraph())
}

// This is a separate class in order to make logs look better.
case class WorkflowProgress() extends Logging {
  def logProgress(execution: Execution): Unit = {
    val states = execution.states.values
    val completed = states.count(_.isCompleted)
    logger.info(
      s"$completed ${if (completed == 1) "node" else "nodes"} successfully completed, " +
      s"${states.count(_.isFailed)} failed, " +
      s"${states.count(_.isAborted)} aborted, " +
      s"${states.count(_.isRunning)} running, " +
      s"${states.count(_.isQueued)} queued.")
  }
}

case class NodeExecutionResults(
  reports: Map[Entity.Id, ReportContent],
  doperables: Map[Entity.Id, DOperable])
