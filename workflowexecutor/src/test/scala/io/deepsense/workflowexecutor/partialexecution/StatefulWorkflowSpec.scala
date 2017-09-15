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

import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.models.Id
import io.deepsense.deeplang.CommonExecutionContext
import io.deepsense.graph.nodestate.Draft
import io.deepsense.graph.{DeeplangGraph, Node}
import io.deepsense.models.workflows._

class StatefulWorkflowSpec extends StandardSpec with MockitoSugar {

  "StatefulWorkflow" should {
    val originalGraph: DeeplangGraph = DeeplangGraph()
    val worklfowWithResults = WorkflowWithResults(
      Workflow.Id.randomId,
      WorkflowMetadata(WorkflowType.Batch, "1.0.0"),
      originalGraph,
      ThirdPartyData(),
      ExecutionReport(Map()))
    val newGraph = mock[DeeplangGraph]
    val newThirdPartyData = mock[ThirdPartyData]
    val workflow = Workflow(
      WorkflowMetadata(WorkflowType.Batch, "1.0.0"),
      newGraph,
      newThirdPartyData)
    val nodeId: Id = Node.Id.randomId
    val executionReport =
      ExecutionReport(Map(nodeId -> NodeState(Draft(), Some(EntitiesMap()))))
    "updateStruct" when {
      "execution is idle" in {
        val updatedExecution: IdleExecution = mock[IdleExecution]
        when(updatedExecution.executionReport).thenReturn(executionReport)
        def idleExecutionFactory(graph: StatefulGraph): Execution = {
          val exec = mock[IdleExecution]
          when(exec.updateStructure(newGraph)).thenReturn(updatedExecution)
          exec
        }
        val statefulWorkflow =
          StatefulWorkflow(mock[CommonExecutionContext], worklfowWithResults, idleExecutionFactory)

        val inferredState = statefulWorkflow.updateStructure(workflow)

        statefulWorkflow.currentExecution shouldBe updatedExecution
        statefulWorkflow.currentAdditionalData shouldBe newThirdPartyData
        inferredState.states shouldBe
          ExecutionReport(Map(nodeId -> NodeState(Draft(), None))) // removed reports
      }
    }
    "ignore struct update and only update thirdPartyData when execution is started" in {
      val runningExecution = mock[RunningExecution]
      def runningExecutionFactory(graph: StatefulGraph): Execution = runningExecution
      when(runningExecution.executionReport).thenReturn(executionReport)
      val statefulWorkflow =
        StatefulWorkflow(mock[CommonExecutionContext], worklfowWithResults, runningExecutionFactory)

      val inferredState = statefulWorkflow.updateStructure(workflow)

      statefulWorkflow.currentExecution shouldBe runningExecution // not changed
      statefulWorkflow.currentAdditionalData shouldBe newThirdPartyData // updated
      inferredState.states shouldBe
        ExecutionReport(Map(nodeId -> NodeState(Draft(), None))) // removed reports
    }
  }

}
