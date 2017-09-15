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

package ai.deepsense.workflowexecutor

import akka.actor._

import ai.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import ai.deepsense.commons.models.Entity
import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang._
import ai.deepsense.graph.DeeplangGraph.DeeplangNode
import ai.deepsense.graph._
import ai.deepsense.models.json.graph.NodeStatusJsonProtocol
import ai.deepsense.models.workflows._
import ai.deepsense.reportlib.model.ReportContent
import ai.deepsense.workflowexecutor.WorkflowManagerClientActorProtocol.{SaveState, SaveWorkflow}
import ai.deepsense.workflowexecutor.partialexecution._

/**
 * WorkflowExecutorActor coordinates execution of a workflow by distributing work to
 * WorkflowNodeExecutorActors and collecting results.
 */
abstract class WorkflowExecutorActor(
    val executionContext: CommonExecutionContext,
    nodeExecutorFactory: GraphNodeExecutorFactory,
    workflowManagerClientActor: Option[ActorRef],
    publisher: Option[ActorRef],
    terminationListener: Option[ActorRef],
    executionFactory: StatefulGraph => Execution)
  extends Actor
  with Logging
  with NodeStatusJsonProtocol {

  import ai.deepsense.workflowexecutor.WorkflowExecutorActor.Messages._

  val progressReporter = WorkflowProgress()
  val workflowId = Workflow.Id.fromString(self.path.name)

  private[workflowexecutor] var statefulWorkflow: StatefulWorkflow = null

  def ready(): Receive = {
    case Launch(nodes) => launch(nodes)
    case UpdateStruct(workflow) => updateStruct(workflow)
    case Synchronize() => synchronize()
  }

  def launched(): Receive = {
    waitingForFinish().orElse {
      case Abort() => abort()
      case NodeStarted(id) => nodeStarted(id)
    }
  }

  def waitingForFinish(): PartialFunction[Any, Unit] = {
    case NodeCompleted(id, nodeExecutionResult) => nodeCompleted(id, nodeExecutionResult)
    case NodeFailed(id, failureDescription) => nodeFailed(id, failureDescription)
    case Synchronize() => synchronize()
    case UpdateStruct(workflow) => updateStruct(workflow)
    case l: Launch =>
      logger.info("It is illegal to Launch a graph when the execution is in progress.")
  }

  def initWithWorkflow(workflowWithResults: WorkflowWithResults): Unit = {
    statefulWorkflow = StatefulWorkflow(executionContext, workflowWithResults, executionFactory)
    context.become(ready())
    sendInferredState(statefulWorkflow.inferState)
    onInitiated()
  }

  def synchronize(): Unit = {
    sendExecutionReport(statefulWorkflow.executionReport)
    sendInferredState(statefulWorkflow.inferState)
  }

  private def updateStruct(workflow: Workflow): Unit = {
    val removedNodes = statefulWorkflow.getNodesRemovedByWorkflow(workflow)
    statefulWorkflow.updateStructure(workflow)

    removedNodes.foreach (node => {
      val nodeRef = getGraphNodeExecutor(node, Vector.empty)
      nodeRef ! WorkflowNodeExecutorActor.Messages.Delete()
    })

    val workflowWithResults: WorkflowWithResults = statefulWorkflow.workflowWithResults
    workflowManagerClientActor.foreach(_ ! SaveWorkflow(workflowWithResults))
    sendInferredState(statefulWorkflow.inferState)
  }

  def sendInferredState(inferredState: InferredState): Unit = {
    publisher.foreach(_ ! inferredState)
  }

  private def abort(): Unit = {
    val startingPointExecution = statefulWorkflow.currentExecution
    statefulWorkflow.abort()
    updateExecutionState(startingPointExecution)
    statefulWorkflow.currentExecution.graph.runningNodes.foreach(node =>
      executionContext.sparkContext.cancelJobGroup(JobGroupIdForNode(node))
    )
  }

  def launch(nodes: Set[Node.Id]): Unit = {
    val startingPointExecution = statefulWorkflow.currentExecution
    val nodesToExecute = if (nodes.isEmpty) {
      startingPointExecution.graph.notExecutedNodes
    } else {
      nodes
    }
    logger.debug("Launching nodes: {}", nodesToExecute)
    statefulWorkflow.launch(nodesToExecute)
    updateExecutionState(startingPointExecution)
  }

  private def updateExecutionState(startingPointExecution: Execution): Unit = {
    val inferredState = statefulWorkflow.currentExecution match {
      case idle: IdleExecution =>
        logger.debug(s"End of execution")
        terminationListener.foreach(_ ! getExecutionReport)
        context.unbecome()
        context.become(ready())
        Some(statefulWorkflow.inferState)
      case running: RunningExecution =>
        launchReadyNodes()
        context.unbecome()
        context.become(launched())
        None
      case aborted: AbortedExecution =>
        logger.debug("Becoming aborted! - waiting for running nodes to finish")
        context.unbecome()
        context.become(waitingForFinish())
        None
      case _: StartedExecution =>
        throw new NotImplementedError("Case for StartedExecution not implemented in WorkflowExecutorActor")
    }
    val executionReport: ExecutionReport =
      statefulWorkflow.changesExecutionReport(startingPointExecution)
    sendExecutionReport(executionReport)

    inferredState.foreach(inferredState => sendInferredState(inferredState))
  }

  def sendExecutionReport(executionReport: ExecutionReport): Unit = {
    logger.debug(s"Status for '$workflowId': Error: ${executionReport.error}, " +
      s"States of nodes: ${executionReport.nodesStatuses.mkString("\n")}")
    publisher.foreach(_ ! executionReport)
    workflowManagerClientActor.foreach(_ ! SaveState(workflowId, executionReport))
  }

  def executionToReport(execution: Execution): ExecutionReport =
    ExecutionReport(execution.graph.states.mapValues(_.nodeState))

  def launchReadyNodes(): Unit = {
    logger.debug("launchReadyNodes")
    val readyNodes: Seq[ReadyNode] = statefulWorkflow.startReadyNodes()
    readyNodes.foreach { readyNode =>
      val input = readyNode.input.toVector
      val nodeRef = getGraphNodeExecutor(readyNode.node, input)
      nodeRef ! WorkflowNodeExecutorActor.Messages.Start()
      logger.debug(s"Starting node $readyNode")
    }
  }

  private def getGraphNodeExecutor(node: DeeplangNode, dooperable: Vector[DOperable]): ActorRef = {
    val nodeExecutionContext = executionContext.createExecutionContext(
      workflowId, node.id)
    nodeExecutorFactory
      .createGraphNodeExecutor(context, nodeExecutionContext, node, dooperable)
  }

  def nodeStarted(id: Node.Id): Unit = logger.debug("{}", NodeStarted(id))

  def nodeCompleted(
      id: Node.Id,
      nodeExecutionResults: NodeExecutionResults): Unit = {
    logger.debug(s"Node ${statefulWorkflow.node(id)} completed!")
    val startingPointExecution = statefulWorkflow.currentExecution
    statefulWorkflow.nodeFinished(
      id,
      nodeExecutionResults.entitiesId,
      nodeExecutionResults.reports,
      nodeExecutionResults.doperables)
    finalizeNodeExecutionEnd(startingPointExecution)
  }

  def nodeFailed(id: Node.Id, cause: Exception): Unit = {
    logger.warn(s"Node ${statefulWorkflow.node(id)} failed!", cause)
    val startingPointExecution = statefulWorkflow.currentExecution
    statefulWorkflow.nodeFailed(id, cause)
    finalizeNodeExecutionEnd(startingPointExecution)
  }

  def finalizeNodeExecutionEnd(startingPointExecution: Execution): Unit = {
    progressReporter.logProgress(statefulWorkflow.currentExecution)
    updateExecutionState(startingPointExecution)
  }

  def actionWithWorkflowId(id: Workflow.Id)(f: => Any): Any = {
    if (id != workflowId) {
      logger.warn(
        s"Init for a wrong workflow received. Expected workflowId: $workflowId, received: $id.")
    } else {
      f
    }
  }

  def getExecutionReport: ExecutionReport = statefulWorkflow.executionReport

  def execution: Execution = statefulWorkflow.currentExecution

  protected def onInitiated(): Unit = {}
}

object WorkflowExecutorActor {

  type Results = Map[Entity.Id, DOperable]

  object Messages {
    sealed trait Message
    case class Launch(nodes: Set[Node.Id] = Set.empty)
      extends Message
    case class NodeStarted(nodeId: Node.Id) extends Message
    case class NodeCompleted(id: Node.Id, results: NodeExecutionResults) extends Message
    case class NodeFailed(id: Node.Id, cause: Exception) extends Message
    case class Abort() extends Message
    case class Init() extends Message
    case class UpdateStruct(workflow: Workflow) extends Message
    case class Synchronize() extends Message
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
    node: DeeplangNode,
    input: Vector[DOperable]): ActorRef
}

class GraphNodeExecutorFactoryImpl extends GraphNodeExecutorFactory {

  override def createGraphNodeExecutor(
      context: ActorContext,
      executionContext: ExecutionContext,
      node: DeeplangNode,
      input: Vector[DOperable]): ActorRef = {
    val props = Props(new WorkflowNodeExecutorActor(executionContext, node, input))
      .withDispatcher("node-executor-dispatcher")
    context.actorOf(props, s"node-executor-${node.id.value.toString}")
  }
}

// This is a separate class in order to make logs look better.
case class WorkflowProgress() extends Logging {
  def logProgress(execution: Execution): Unit = {
    val states = execution.graph.states.values
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
  entitiesId: Seq[Entity.Id],
  reports: Map[Entity.Id, ReportContent],
  doperables: Map[Entity.Id, DOperable])
