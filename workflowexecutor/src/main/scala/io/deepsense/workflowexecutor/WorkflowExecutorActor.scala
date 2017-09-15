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
import io.deepsense.messageprotocol.WorkflowExecutorProtocol.{Abort, ExecutionStatus}
import io.deepsense.models.entities.Entity
import io.deepsense.models.workflows.EntitiesMap
import io.deepsense.reportlib.model.ReportContent
import io.deepsense.workflowexecutor.communication.Connect

/**
 * WorkflowExecutorActor coordinates execution of a workflow by distributing work to
 * WorkflowNodeExecutorActors and collecting results.
 */
class WorkflowExecutorActor(
    executionContext: ExecutionContext,
    nodeExecutorFactory: GraphNodeExecutorFactory,
    statusListener: Option[ActorRef])
  extends Actor
  with Logging {

  import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages._

  val dOperableCache: mutable.Map[Entity.Id, DOperable] = mutable.Map.empty
  val reports: mutable.Map[Entity.Id, ReportContent] = mutable.Map.empty
  val progressReporter = WorkflowProgress()
  val workflowId = self.path.name

  def launched(graph: StatefulGraph): Receive = {
    case NodeStarted(id) => nodeStarted(id)
    case NodeCompleted(id, nodeExecutionResult) =>
      nodeCompleted(id,
        nodeExecutionResult,
        graph)
    case NodeFailed(id, failureDescription) =>
      nodeFailed(id, failureDescription, graph)
    case Connect(_) =>
      sendWorkflowStatus(Some(graph))
    case Abort(_, nodes) =>
      logger.warn("Aborting graph execution is not supported yet.")
    case l: Launch =>
      logger.info("It is illegal to Launch a graph when the execution is in progress.")
  }

  private def reset(): Unit = {
    dOperableCache.clear()
    reports.clear()
  }

  def finished(finishedGraph: StatefulGraph): Receive = {
    case Launch(graph, _) =>
      reset()
      launch(graph.reset)
    case Connect(_) =>
      sendWorkflowStatus(Some(finishedGraph))
  }

  override def receive: Receive = {
    case Launch(graph, _) =>
      launch(graph.reset)
    case Connect(_) =>
      sendWorkflowStatus(None)
  }

  def launch(graph: StatefulGraph): Unit = {
    val inferred = graph.inferAndApplyKnowledge(executionContext.inferContext)
    val enqueued = if (inferred.state.isFailed) {
      inferred
    } else {
      inferred.enqueue
    }
    checkExecutionState(enqueued)
  }

  def checkExecutionState(graph: StatefulGraph): Unit = {
    graph.state match {
      case graphstate.Draft => logger.error("Graph in state 'Draft'! This should not happen!")
      case graphstate.Running =>
        val launchedGraph = launchReadyNodes(graph)
        context.unbecome()
        context.become(launched(launchedGraph))
      case _ =>
        logger.debug(s"End of execution, state=${graph.state}")
        context.unbecome()
        context.become(finished(graph))
    }
    sendWorkflowStatus(Some(graph))
  }

  def sendWorkflowStatus(graph: Option[StatefulGraph]): Unit = {
    val status = graph match {
      case Some(StatefulGraph(_, nodes, state)) =>
        ExecutionStatus(state, nodes, EntitiesMap(dOperableCache.toMap, reports.toMap))
      case None =>
        ExecutionStatus(graphstate.Draft, Map.empty, EntitiesMap())
    }
    logger.debug(s"Status for '$workflowId': $status")
    statusListener.foreach(_ ! status)
  }

  def launchReadyNodes(graph: StatefulGraph): StatefulGraph = {
    logger.debug("launchReadyNodes")
    graph.readyNodes.foldLeft(graph) {
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
      graph: StatefulGraph): Unit = {
    logger.debug(s"Node ${graph.node(id)} completed!")
    processResults(nodeExecutionResults)
    val entityIds = nodeExecutionResults.doperables.keys.toSeq
    val updatedGraph = graph.nodeFinished(id, entityIds)
    finalizeNodeExecutionEnd(updatedGraph)
  }

  def nodeFailed(
      id: Node.Id,
      cause: Exception,
      graph: StatefulGraph): Unit = {
    logger.warn(s"Node ${graph.node(id)} failed!", cause)
    val withFailedNode = graph.nodeFailed(id, cause)
    finalizeNodeExecutionEnd(withFailedNode)
  }

  def processResults(nodeExecutionResults: NodeExecutionResults): Unit = {
    reports ++= nodeExecutionResults.reports
    dOperableCache ++= nodeExecutionResults.doperables
  }

  def finalizeNodeExecutionEnd(graph: StatefulGraph): Unit = {
    progressReporter.logProgress(graph)
    checkExecutionState(graph)
  }
}

object WorkflowExecutorActor {
  def props(ec: ExecutionContext, statusListener: Option[ActorRef] = None): Props =
    Props(new WorkflowExecutorActor(ec,
      new ProductionGraphNodeExecutorFactory,
      statusListener))
  type Results = Map[Entity.Id, DOperable]

  object Messages {
    sealed trait Message
    case class Launch(
        graph: StatefulGraph,
        nodes: Seq[Node.Id] = Seq.empty)
      extends Message
    case class NodeStarted(nodeId: Node.Id) extends Message
    case class NodeCompleted(id: Node.Id, results: NodeExecutionResults) extends Message
    case class NodeFailed(id: Node.Id, cause: Exception) extends Message
    case class GraphFinished(graph: StatefulGraph, entitiesMap: EntitiesMap) extends Message
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

class ProductionGraphNodeExecutorFactory extends GraphNodeExecutorFactory {

  override def createGraphNodeExecutor(
      context: ActorContext,
      executionContext: ExecutionContext,
      node: Node,
      input: Vector[DOperable]): ActorRef = {
    val props = Props(new WorkflowNodeExecutorActor(executionContext, node, input))
    context.actorOf(props, s"node-executor-${node.id.value.toString}")
  }

}

// This is a separate class in order to make logs look better.
case class WorkflowProgress() extends Logging {
  def logProgress(graph: StatefulGraph): Unit = {
    val states = graph.states.values
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
