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
import scala.util.{Failure, Success, Try}

import akka.actor._

import io.deepsense.commons.exception.{FailureCode, DeepSenseFailure, FailureDescription}
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.graph.{CyclicGraphException, GraphKnowledge, Graph, Node}
import io.deepsense.models.entities.Entity
import io.deepsense.models.workflows.EntitiesMap
import io.deepsense.reportlib.model.ReportContent
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Results

/**
 * WorkflowExecutorActor coordinates execution of a workflow by distributing work to
 * WorkflowNodeExecutorActors and collecting results.
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
  val progressReporter = WorkflowProgress()

  override def receive: Receive = {
    case Launch(g, withReports, resultPromise) => launch(g, withReports, resultPromise)
    case NodeStarted(id) => nodeStarted(id)
    case NodeFinished(node, results) => nodeFinished(node, results)
  }

  def launch(g: Graph, generateReports: Boolean, result: Promise[GraphFinished]): Unit = {
    this.generateReports = generateReports
    this.result = result
    graph = g

    val errors = Try(graph.inferKnowledge(executionContext)) match {
      case Success(knowledge) =>
        knowledge.errors.headOption.map(_ =>
          WorkflowExecutorActor.inferenceErrorsFailureDescription(knowledge))
      case Failure(ex: CyclicGraphException) =>
        Some(WorkflowExecutorActor.cyclicGraphFailureDescription)
      case Failure(ex) =>
        Some(WorkflowExecutorActor.genericFailureDescription(ex))
    }

    if (errors.isDefined) {
      logger.error("Workflow is incorrect - cannot launch. Errors: {}", errors.get)
      graph = graph.markFailed(errors.get)
      graph = graph.abortNodes
      endExecution()
    } else {
      graph = graph.markRunning
      logger.debug("launch(graph={})", graph)
      launchReadyNodes(graph)
      if (graph.readyNodes.isEmpty) {
        endExecution()
      }
    }
  }

  def nodeStarted(id: Node.Id): Unit = {
    logger.debug("{}", NodeStarted(id))
    graph = graph.markAsRunning(id)
  }

  def nodeFinished(node: Node, results: Results): Unit = {
    assert(node.isFailed || node.isCompleted)
    logger.debug(s"Node finished: $node")
    graph = graph.withChangedNode(node)

    if (generateReports) {
      Try(collectReports(results)).recover { case e =>
        node.markFailed(e)
        graph = graph.withChangedNode(node)
      }
    }

    dOperableCache = dOperableCache ++ results

    if (graph.readyNodes.nonEmpty) {
      launchReadyNodes(graph)
    } else {
      val nodesRunningCount = graph.runningNodes.size
      if (nodesRunningCount > 0) {
        logger.debug(s"No nodes to run, but there are still $nodesRunningCount running")
      } else {
        endExecution()
      }
    }

    progressReporter.logProgress(graph)
  }

  def launchReadyNodes(graph: Graph): Unit = {
    logger.debug("launchReadyNodes")
    for {
      node <- graph.readyNodes
      if !startedNodes.contains(node.id)
    } {
      val props = Props(createGraphNodeExecutor(executionContext, node, graph, dOperableCache))
      val nodeRef = context.actorOf(props, s"node-executor-${node.id.value.toString}")
      nodeRef ! WorkflowNodeExecutorActor.Messages.Start()
      logger.debug(s"Starting node $node")
    }
    startedNodes = startedNodes ++ graph.readyNodes.map(_.id)
  }

  def endExecution(): Unit = {
    graph = graph.updateState()
    logger.debug(s"End execution, status=${graph.state.status}")
    result.success(GraphFinished(graph, EntitiesMap(dOperableCache, reports)))
    logger.debug("Shutting down the actor system")
    context.become(ignoreAllMessages)
    shutdownSystem
  }

  def ignoreAllMessages: Receive = {
    case x => logger.debug(s"Received message $x after being aborted - ignoring")
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
    case class GraphFinished(graph: Graph, entitiesMap: EntitiesMap) extends Message
  }

  def inferenceErrorsFailureDescription(graphKnowledge: GraphKnowledge): FailureDescription = {
    FailureDescription(
      DeepSenseFailure.Id.randomId,
      FailureCode.IncorrectWorkflow,
      "Incorrect workflow",
      Some("Provided workflow cannot be launched, because it contains errors"),
      details = graphKnowledge.errors.map {
        case (id, errors) => (id.toString, errors.map(_.toString).mkString("\n"))
      }
    )
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

// This is a separate class in order to make logs look better.
case class WorkflowProgress() extends Logging {
  def logProgress(graph: Graph): Unit = {
    val completed = graph.nodes.count(_.isCompleted)
    logger.info(
      s"$completed ${if (completed == 1) "node" else "nodes"} successfully completed, " +
      s"${graph.nodes.count(_.isFailed)} failed, " +
      s"${graph.nodes.count(_.isAborted)} aborted, " +
      s"${graph.nodes.count(_.isRunning)} running, " +
      s"${graph.nodes.count(_.isQueued)} queued.")
  }
}
