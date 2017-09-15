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

import spray.json.JsObject

import ai.deepsense.commons.models.Entity
import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang.{CommonExecutionContext, DOperable}
import ai.deepsense.graph.DeeplangGraph.DeeplangNode
import ai.deepsense.graph.Node
import ai.deepsense.graph.Node._
import ai.deepsense.models.workflows._
import ai.deepsense.reportlib.model.ReportContent

class StatefulWorkflow(
  private val executionContext: CommonExecutionContext,
  val workflowId: Workflow.Id,
  val metadata: WorkflowMetadata,
  val workflowInfo: WorkflowInfo,
  private val thirdPartyData: JsObject,
  private val startingExecution: Execution,
  private val stateInferrer: StateInferrer) extends Logging {

  private var execution: Execution = startingExecution
  private var additionalData = thirdPartyData

  def getNodesRemovedByWorkflow(workflow: Workflow) : Set[DeeplangNode] = {
    val previousNodes = execution.graph.nodes
    val newNodes = workflow.graph.nodes
    val removedNodesId = previousNodes.map(node => node.id).diff(
      newNodes.map(node => node.id))
    previousNodes.filter(node => removedNodesId.contains(node.id))
  }

  def launch(nodes: Set[Node.Id]): Unit = {
    execution match {
      case idleExecution: IdleExecution => {
        val newExecution = idleExecution.updateStructure(execution.graph.directedGraph, nodes)
        val inferred = newExecution.inferAndApplyKnowledge(executionContext.inferContext)
        val map: Option[Execution] = inferred.graph.executionFailure.map(_ => inferred)
        execution = map.getOrElse(inferred.enqueue)
      }
      case notIdle => throw new IllegalStateException(
        s"Only IdleExecution can be launched. Execution: $notIdle")
    }
  }

  def startReadyNodes(): Seq[ReadyNode] = {
    val readyNodes = execution.graph.readyNodes
    execution = readyNodes.foldLeft(execution) {
      case (runningExecution, readyNode) => runningExecution.nodeStarted(readyNode.node.id)
    }
    readyNodes
  }

  def currentExecution: Execution = execution

  def currentAdditionalData: JsObject = additionalData

  def executionReport: ExecutionReport = execution.executionReport

  def changesExecutionReport(startingPointExecution: Execution): ExecutionReport =
    ExecutionReport(getChangedNodes(startingPointExecution), execution.graph.executionFailure)

  def workflowWithResults: WorkflowWithResults = WorkflowWithResults(
    workflowId,
    metadata,
    execution.graph.directedGraph,
    additionalData,
    executionReport,
    workflowInfo
  )

  def node(id: Node.Id): DeeplangNode = execution.node(id)

  def nodeStarted(id: Node.Id): Unit = {
    execution = execution.nodeStarted(id)
  }

  def nodeFinished(
      id: Node.Id,
      entitiesIds: Seq[Entity.Id],
      reports: Map[Entity.Id, ReportContent],
      dOperables: Map[Entity.Id, DOperable]): Unit = {
    execution = execution.nodeFinished(id, entitiesIds, reports, dOperables)
  }

  def nodeFailed(id: Node.Id, cause: Exception): Unit = {
    execution = execution.nodeFailed(id, cause)
  }

  def abort(): Unit = {
    execution = execution.abort
  }

  /**
    * When execution is running struct update will be ignored.
    */
  def updateStructure(workflow: Workflow): Unit = {
    execution = execution match {
      case idleExecution: IdleExecution => idleExecution.updateStructure(workflow.graph)
      case _ =>
        logger.warn("Update of the graph during execution is impossible. " +
          "Only `thirdPartyData` updated.")
        execution
    }
    additionalData = workflow.additionalData
  }

  def inferState: InferredState = {
    stateInferrer.inferState(execution)
  }

  private def getChangedNodes(startingPointExecution: Execution): Map[Id, NodeState] = {
    execution.graph.states.filterNot { case (id, stateWithResults) =>
      startingPointExecution.graph.states.contains(id) &&
        stateWithResults.clearKnowledge == startingPointExecution.graph.states(id).clearKnowledge
    }.mapValues(_.nodeState)
  }
}

object StatefulWorkflow extends Logging {

  def apply(
      executionContext: CommonExecutionContext,
      workflow: WorkflowWithResults,
      executionFactory: StatefulGraph => Execution): StatefulWorkflow = {
    val states = workflow.executionReport.states
    val noMissingStates = workflow.graph.nodes.map { case node =>
      states.get(node.id)
        .map(state => node.id -> NodeStateWithResults(state.draft, Map(), None))
        .getOrElse(node.id -> NodeStateWithResults.draft)
    }.toMap
    val graph = StatefulGraph(workflow.graph, noMissingStates, workflow.executionReport.error)
    val execution = executionFactory(graph)
    new StatefulWorkflow(
      executionContext,
      workflow.id,
      workflow.metadata,
      workflow.workflowInfo,
      workflow.thirdPartyData,
      execution,
      new DefaultStateInferrer(executionContext, workflow.id)
    )
  }
}

trait StateInferrer {

  def inferState(execution: Execution): InferredState
}

class DefaultStateInferrer(
    executionContext: CommonExecutionContext,
    workflowId: Workflow.Id)
  extends StateInferrer {

  override def inferState(execution: Execution): InferredState = {
    val knowledge = execution.graph.inferKnowledge(
      executionContext.inferContext, execution.graph.memorizedKnowledge)
    InferredState(workflowId, knowledge, execution.executionReport.statesOnly)
  }
}
