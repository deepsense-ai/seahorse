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
import scala.concurrent.Promise

import akka.actor._

import io.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.graph._
import io.deepsense.models.entities.Entity
import io.deepsense.models.workflows.EntitiesMap
import io.deepsense.reportlib.model.ReportContent

/**
 * WorkflowExecutorActor coordinates execution of a workflow by distributing work to
 * WorkflowNodeExecutorActors and collecting results.
 */
class WorkflowExecutorActor(executionContext: ExecutionContext,
    nodeExecutorFactory: GraphNodeExecutorFactory,
    systemShutdowner: SystemShutdowner)
  extends Actor with Logging {

  import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages._

  val dOperableCache: mutable.Map[Entity.Id, DOperable] = mutable.Map.empty
  val reports: mutable.Map[Entity.Id, ReportContent] = mutable.Map.empty
  val progressReporter = WorkflowProgress()

  def launched(graph: StatefulGraph, graphExecutionResult: Promise[GraphFinished]): Receive = {
    case NodeStarted(id) => nodeStarted(id)
    case NodeCompleted(id, nodeExecutionResult) =>
      nodeCompleted(id,
        nodeExecutionResult,
        graph,
        graphExecutionResult)
    case NodeFailed(id, failureDescription) =>
      nodeFailed(id, failureDescription, graph, graphExecutionResult)
  }

  override def receive: Receive = {
    case Launch(g, resultPromise) => launch(g, resultPromise)
  }

  def launch(graph: StatefulGraph, result: Promise[GraphFinished]): Unit = {
    val inferred = graph.inferAndApplyKnowledge(executionContext)
    val enqueued = if (inferred.state.isFailed) {
      inferred
    } else {
      inferred.enqueue
    }
    checkExecutionState(enqueued, result)
  }

  def shutdownSystem(): Unit = systemShutdowner.shutdownSystem(context.system)

  def checkExecutionState(graph: StatefulGraph, result: Promise[GraphFinished]): Unit = {
    graph.state match {
      case graphstate.Draft => logger.error("Graph in state 'Draft'! This should not happen!")
      case graphstate.Running =>
        val launchedGraph = launchReadyNodes(graph)
        context.unbecome()
        context.become(launched(launchedGraph, result))
      case _ =>
        logger.debug(s"End execution, state=${graph.state}")
        result.success(GraphFinished(graph, EntitiesMap(dOperableCache.toMap, reports.toMap)))
        logger.debug("Shutting down the actor system")
        context.become(ignoreAllMessages)
        shutdownSystem()
    }
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
      graph: StatefulGraph,
      result: Promise[GraphFinished]): Unit = {
    logger.debug(s"Node ${graph.node(id)} completed!")
    processResults(nodeExecutionResults)
    val entityIds = nodeExecutionResults.doperables.keys.toSeq
    val updatedGraph = graph.nodeFinished(id, entityIds)
    finalizeNodeExecutionEnd(updatedGraph, result)
  }

  def nodeFailed(
      id: Node.Id,
      cause: Exception,
      graph: StatefulGraph,
      result: Promise[GraphFinished]): Unit = {
    logger.warn(s"Node ${graph.node(id)} failed!", cause)
    val withFailedNode = graph.nodeFailed(id, cause)
    finalizeNodeExecutionEnd(withFailedNode, result)
  }

  def processResults(nodeExecutionResults: NodeExecutionResults): Unit = {
    reports ++= nodeExecutionResults.reports
    dOperableCache ++= nodeExecutionResults.doperables
  }

  def finalizeNodeExecutionEnd(graph: StatefulGraph, result: Promise[GraphFinished]): Unit = {
    progressReporter.logProgress(graph)
    checkExecutionState(graph, result)
  }

  def ignoreAllMessages: Receive = {
    case x => logger.debug(s"Received message $x after being aborted - ignoring")
  }
}

object WorkflowExecutorActor {
  def props(ec: ExecutionContext): Props =
    Props(new WorkflowExecutorActor(ec,
      new ProductionGraphNodeExecutorFactory,
      new ProductionSystemShutdowner))
  type Results = Map[Entity.Id, DOperable]

  object Messages {
    sealed trait Message
    case class Launch(
        graph: StatefulGraph,
        result: Promise[GraphFinished])
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

trait SystemShutdowner {
  def shutdownSystem(system: ActorSystem): Unit
}

class ProductionSystemShutdowner extends SystemShutdowner {
  def shutdownSystem(system: ActorSystem): Unit = system.shutdown()
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
