/**
 * Copyright 2015, CodiLime Inc.
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

import scala.concurrent.Promise

import akka.actor._

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.graph.{Graph, Node}
import io.deepsense.models.entities.Entity
import io.deepsense.reportlib.model.ReportContent
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Results

/**
 * WorkflowExecutorActor coordinates execution of a workflow by distributing work to
 * WokrflowNodeExecutorActors and collecting results.
 */
class WorkflowExecutorActor(executionContext: ExecutionContext)
  extends Actor with Logging {

  this: GraphNodeExecutorFactory with SystemShutdowner =>

  import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages._

  var generateReports: Boolean = false
  var result: Promise[GraphFinished] = _
  var graph: Graph = _
  var dOperableCache: Results = Map.empty
  var reports: Map[Entity.Id, ReportContent] = Map.empty
  var startedNodes: Set[Node.Id] = Set()

  override def receive: Receive = {
    case Launch(g, withReports, resultPromise) => launch(g, withReports, resultPromise)
    case NodeStarted(id) => nodeStarted(id)
    case NodeFinished(node, results) => nodeFinished(node, results)
  }

  def launch(g: Graph, generateReports: Boolean, result: Promise[GraphFinished]): Unit = {
    this.generateReports = generateReports
    this.result = result
    graph = g.markRunning
    logger.info(">>> Launch(graph={})", graph)
    launchReadyNodes(graph)
    if (graph.readyNodes.isEmpty) {
      endExecution()
    }
  }

  def nodeStarted(id: Node.Id): Unit = {
    logger.info(">>> {}", NodeStarted(id))
    graph = graph.markAsRunning(id)
  }

  def nodeFinished(node: Node, results: Results): Unit = {
    assert(node.isFailed || node.isCompleted)
    logger.info(s">>> nodeFinished(node=$node)")
    graph = graph.withChangedNode(node)

    if (generateReports) {
      collectReports(results)
    }

    dOperableCache = dOperableCache ++ results
    logger.debug(s"<<< NodeCompleted(nodeId=${node.id})")
    if (graph.readyNodes.nonEmpty) {
      launchReadyNodes(graph)
    } else {
      val nodesRunningCount = graph.runningNodes.size
      if (nodesRunningCount > 0) {
        logger.debug(s"No nodes to run, but there are still $nodesRunningCount running")
        logger.debug(s"Awaiting $nodesRunningCount RUNNINGs reporting Completed")
      } else {
        endExecution()
      }
    }
  }

  def launchReadyNodes(graph: Graph): Unit = {
    logger.info(">>> launchReadyNodes(graph={})", graph)
    for {
      node <- graph.readyNodes
      if !startedNodes.contains(node.id)
    } {
      val props = Props(createGraphNodeExecutor(executionContext, node, graph, dOperableCache))
      val nodeRef = context.actorOf(props, s"node-executor-${node.id.value.toString}")
      nodeRef ! WorkflowNodeExecutorActor.Messages.Start()
      logger.info(s"Starting node $node")
    }
    startedNodes = startedNodes ++ graph.readyNodes.map(_.id)
  }

  def endExecution(): Unit = {
    graph = graph.updateState()
    logger.debug("End execution, status={}", graph.state.status)
    result.success(GraphFinished(graph, dOperableCache, reports))
    logger.debug("Shutting down the actor system")
    context.become(ignoreAllMessages)
    shutdownSystem
  }

  def ignoreAllMessages: Receive = {
    case x => logger.info(s"Received message $x after being aborted - ignoring")
  }

  def collectReports(results: Results): Unit = {
    val reports = results.mapValues(_.report.content)
    this.reports = this.reports ++ reports
  }
}

object WorkflowExecutorActor {
  def props(ec: ExecutionContext): Props =
    Props(new WorkflowExecutorActor(ec)
      with ProductionGraphNodeExecutorFactory
      with ProductionSystemShutdowner)

  type Results = Map[Entity.Id, DOperable]

  object Messages {
    sealed trait Message
    case class Launch(
        graph: Graph,
        generateReports: Boolean,
        result: Promise[GraphFinished])
      extends Message
    case class NodeStarted(nodeId: Node.Id) extends Message
    case class NodeFinished(node: Node, results: Results) extends Message
    case class GraphFinished(
        graph: Graph,
        results: Results,
        reports: Map[Entity.Id, ReportContent])
      extends Message
  }
}

trait GraphNodeExecutorFactory {
  def createGraphNodeExecutor(
      executionContext: ExecutionContext,
      node: Node,
      graph: Graph,
      dOperableCache: Results): Actor
}

trait ProductionGraphNodeExecutorFactory extends GraphNodeExecutorFactory {

  override def createGraphNodeExecutor(
      executionContext: ExecutionContext,
      node: Node,
      graph: Graph,
      dOperableCache: Results): Actor =
    new WorkflowNodeExecutorActor(executionContext, node, graph, dOperableCache)
}

trait SystemShutdowner {
  def shutdownSystem: Unit
}

trait ProductionSystemShutdowner extends SystemShutdowner {
  val context: ActorContext
  def shutdownSystem: Unit = context.system.shutdown()
}
