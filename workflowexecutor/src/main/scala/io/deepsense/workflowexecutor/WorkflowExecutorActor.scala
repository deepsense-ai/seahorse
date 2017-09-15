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
import io.deepsense.graph.Node.Id
import io.deepsense.graph._
import io.deepsense.graph.nodestate.{Completed, NodeState}
import io.deepsense.models.entities.Entity
import io.deepsense.models.workflows.EntitiesMap
import io.deepsense.reportlib.model.ReportContent
import io.deepsense.workflowexecutor.communication.{Connect, ExecutionStatus}
import io.deepsense.workflowexecutor.partialexecution.{Execution, IdleExecution, RunningExecution}

/**
 * WorkflowExecutorActor coordinates execution of a workflow by distributing work to
 * WorkflowNodeExecutorActors and collecting results.
 */
class WorkflowExecutorActor(
    executionContext: ExecutionContext,
    nodeExecutorFactory: GraphNodeExecutorFactory,
    executionFactory: ExecutionFactory,
    terminationListener: Option[ActorRef],
    publisher: Option[ActorSelection])
  extends Actor
  with Logging {

  import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages._

  val dOperableCache: mutable.Map[Entity.Id, DOperable] = mutable.Map.empty
  val reports: mutable.Map[Entity.Id, ReportContent] = mutable.Map.empty
  val progressReporter = WorkflowProgress()
  val workflowId = self.path.name

  def launched(execution: RunningExecution): Receive = {
    case NodeStarted(id) => nodeStarted(id)
    case NodeCompleted(id, nodeExecutionResult) =>
      nodeCompleted(id,
        nodeExecutionResult,
        execution)
    case NodeFailed(id, failureDescription) =>
      nodeFailed(id, failureDescription, execution)
    case Connect(_) =>
      sendExecutionStatus(executionToStatus(execution))
    case l: Launch =>
      logger.info("It is illegal to Launch a graph when the execution is in progress.")
  }

  private def reset(): Unit = {
    dOperableCache.clear()
    reports.clear()
  }

  def finished(finishedExecution: IdleExecution): Receive = {
    case Launch(graph, nodes) =>
      val nodeSet = nodes.toSet
      reset()
      val updatedStructure = finishedExecution.updateStructure(graph, nodeSet)
      launch(updatedStructure, nodes)
    case Connect(_) =>
      sendExecutionStatus(executionToStatus(finishedExecution))
  }

  override def receive: Receive = {
    case Launch(graph, nodes) => // TODO: Pass DirectedGraph instead of StatefulGraph?
      val execution = executionFactory.create(graph, nodes)
      launch(execution, nodes)
    case Connect(_) =>
      sendExecutionStatus(ExecutionStatus(Map.empty, EntitiesMap()))
  }

  def launch(execution: IdleExecution, nodes: Seq[Node.Id]): Unit = {
    val inferred = execution.inferAndApplyKnowledge(executionContext.inferContext)
    val enqueued = inferred.error.map(_ => inferred).getOrElse(inferred.enqueue)
    updateExecutionState(execution, enqueued)
  }

  def updateExecutionState(originalExecution: Execution, executionInProcess: Execution): Unit = {
    val updatedExecution = executionInProcess match {
      case idle: IdleExecution =>
        logger.debug(s"End of execution")
        terminationListener.foreach(_ ! executionToStatus(executionInProcess))
        context.unbecome()
        context.become(finished(idle))
        idle
      case running: RunningExecution =>
        val launchedGraph = launchReadyNodes(running)
        context.unbecome()
        context.become(launched(launchedGraph))
        launchedGraph
    }

    val executionStatus: ExecutionStatus =
      calculateExecutionStatus(originalExecution, updatedExecution)
    sendExecutionStatus(executionStatus)
  }

  private def calculateExecutionStatus(
      originalExecution: Execution,
      updatedExecution: Execution): ExecutionStatus = {
    val updatedStates: Map[Id, NodeState] = getChangedNodes(originalExecution, updatedExecution)
    val entitiesMap: EntitiesMap = createEntitiesMap(updatedStates.values.toSeq)
    ExecutionStatus(updatedStates, entitiesMap, updatedExecution.error)
  }

  private def getChangedNodes(
      originalExecution: Execution,
      updatedExecution: Execution): Map[Id, NodeState] = {
    updatedExecution.states.filter(p => p._2 != originalExecution.states(p._1))
  }

  private def createEntitiesMap(states: Seq[NodeState]): EntitiesMap = {
    val entities: Seq[Entity.Id] = states.flatMap(_ match {
      case Completed(_, _, results: Seq[Id]) => results
      case _ => Seq.empty
    })
    val reportsContents = entities.map(id => id -> reports(id)).toMap
    val dOperables = entities.map(id => id -> dOperableCache(id)).toMap
    EntitiesMap(dOperables, reportsContents)
  }

  def sendExecutionStatus(executionStatus: ExecutionStatus): Unit = {
    logger.debug(s"Status for '$workflowId': $executionStatus")
    publisher.foreach(_ ! executionStatus)
  }

  def executionToStatus(execution: Execution): ExecutionStatus = {
    ExecutionStatus(
      execution.states,
      EntitiesMap(dOperableCache.toMap, reports.toMap), execution.error)
  }

  def launchReadyNodes(execution: RunningExecution): RunningExecution = {
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
    val updatedExecution = execution.nodeFinished(id, entityIds)
    finalizeNodeExecutionEnd(execution, updatedExecution)
  }

  def nodeFailed(
      id: Node.Id,
      cause: Exception,
      execution: Execution): Unit = {
    logger.warn(s"Node ${execution.node(id)} failed!", cause)
    val withFailedNode = execution.nodeFailed(id, cause)
    finalizeNodeExecutionEnd(execution, withFailedNode)
  }

  def processResults(nodeExecutionResults: NodeExecutionResults): Unit = {
    reports ++= nodeExecutionResults.reports
    dOperableCache ++= nodeExecutionResults.doperables
  }

  def finalizeNodeExecutionEnd(
      originalExecution: Execution,
      executionInProcess: Execution): Unit = {
    progressReporter.logProgress(executionInProcess)
    updateExecutionState(originalExecution, executionInProcess)
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
    case class Abort() extends Message
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
  def create(directedGraph: DirectedGraph, nodes: Seq[Node.Id]): IdleExecution
  def empty: Execution
}

class PartialExecutionFactoryImpl extends ExecutionFactory {
  override def create(directedGraph: DirectedGraph, nodes: Seq[Node.Id]): IdleExecution = {
    Execution(directedGraph, nodes)
  }

  override val empty: Execution = Execution.empty
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
