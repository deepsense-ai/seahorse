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

package io.deepsense.workflowexecutor.partialexecution

import io.deepsense.commons.models.Entity
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.{DOperation, CommonExecutionContext, DOperable}
import io.deepsense.graph.DeeplangGraph.DeeplangNode
import io.deepsense.graph.Node
import io.deepsense.graph.Node._
import io.deepsense.models.workflows._
import io.deepsense.reportlib.model.ReportContent

class StatefulWorkflow(
  private val executionContext: CommonExecutionContext,
  val workflowId: Workflow.Id,
  val metadata: WorkflowMetadata,
  private val thirdPartyData: ThirdPartyData,
  private val startingExecution: Execution) extends Logging {

  private var execution: Execution = startingExecution
  private var additionalData = thirdPartyData

  def launch(nodes: Set[Node.Id]): Unit = {
    val newExecution = execution.updateStructure(execution.graph.directedGraph, nodes)
    val inferred = newExecution.inferAndApplyKnowledge(executionContext.inferContext)
    val map: Option[Execution] = inferred.error.map(_ => inferred)
    execution = map.getOrElse(inferred.enqueue)
  }

  def startReadyNodes(): Seq[ReadyNode] = {
    logger.debug("startReadyNodes")
    val readyNodes = execution.readyNodes
    execution = readyNodes.foldLeft(execution) {
      case (runningExecution, readyNode) => runningExecution.nodeStarted(readyNode.node.id)
    }
    readyNodes
  }

  def currentExecution: Execution = execution

  def currentAdditionalData: ThirdPartyData = additionalData

  def executionReport: ExecutionReport = execution.executionReport

  def changesExecutionReport(startingPointExecution: Execution): ExecutionReport =
    ExecutionReport(getChangedNodes(startingPointExecution), execution.graph.executionFailure)

  def workflowWithResults: WorkflowWithResults = WorkflowWithResults(
    workflowId,
    metadata,
    execution.graph.directedGraph,
    additionalData,
    executionReport
  )

  def node(id: Node.Id): DeeplangNode = execution.node(id)

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
  def updateStructure(workflow: Workflow): InferredState = {
    execution = execution match {
      case IdleExecution(_, _) => execution.updateStructure(workflow.graph)
      case _ =>
        logger.warn("Update of the graph during execution is impossible. " +
          "Only `thirdPartyData` updated.")
        execution
    }
    additionalData = workflow.additionalData
    inferState()
  }

  def inferState(): InferredState = {
    val knowledge = execution.inferKnowledge(executionContext.inferContext)
    InferredState(workflowId, knowledge, executionReport)
  }

  private def getChangedNodes(startingPointExecution: Execution): Map[Id, NodeState] = {
    execution.states.filterNot { case (id, stateWithResults) =>
      startingPointExecution.states.contains(id) &&
        stateWithResults.clearKnowledge == startingPointExecution.states(id).clearKnowledge
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
    val execution = executionFactory.apply(graph)
    new StatefulWorkflow(
      executionContext,
      workflow.id,
      workflow.metadata,
      workflow.thirdPartyData,
      execution)
  }
}
