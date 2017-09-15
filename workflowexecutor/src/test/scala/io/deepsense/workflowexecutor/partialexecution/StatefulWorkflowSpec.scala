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
import spray.json.{JsObject, JsString}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.models.Id
import io.deepsense.deeplang.{CommonExecutionContext, DOperation}
import io.deepsense.graph.nodestate.{Completed, Draft}
import io.deepsense.graph._
import io.deepsense.models.json.workflow.WorkflowTestSupport
import io.deepsense.models.workflows._

class StatefulWorkflowSpec extends WorkflowTestSupport with MockitoSugar {

  "StatefulWorkflow" should {
    val originalGraph: DeeplangGraph = DeeplangGraph()
    val worklfowWithResults = WorkflowWithResults(
      Workflow.Id.randomId,
      WorkflowMetadata(WorkflowType.Batch, "1.0.0"),
      originalGraph,
      JsObject(),
      ExecutionReport(Map()),
      WorkflowInfo.empty())
    val newGraph = mock[DeeplangGraph]
    val newThirdPartyData = mock[JsObject]
    val workflow = Workflow(
      WorkflowMetadata(WorkflowType.Batch, "1.0.0"),
      newGraph,
      newThirdPartyData)
    val nodeId: Id = Node.Id.randomId
    val executionReport =
      ExecutionReport(Map(nodeId -> NodeState(Draft(), Some(EntitiesMap()))))
    "actually updateStruct only" when {
      "execution is idle (but third party should be updated anyway)" in {
        val updatedExecution: IdleExecution = mock[IdleExecution]
        when(updatedExecution.executionReport).thenReturn(executionReport)
        def idleExecutionFactory(graph: StatefulGraph): Execution = {
          val exec = mock[IdleExecution]
          when(exec.updateStructure(newGraph)).thenReturn(updatedExecution)
          exec
        }
        val statefulWorkflow =
          StatefulWorkflow(mock[CommonExecutionContext], worklfowWithResults, idleExecutionFactory)

        statefulWorkflow.updateStructure(workflow)

        statefulWorkflow.currentExecution shouldBe updatedExecution
        statefulWorkflow.currentAdditionalData shouldBe newThirdPartyData
      }
    }
    "ignore struct update and only update thirdPartyData when execution is started" in {
      val runningExecution = mock[RunningExecution]
      def runningExecutionFactory(graph: StatefulGraph): Execution = runningExecution
      when(runningExecution.executionReport).thenReturn(executionReport)
      val statefulWorkflow =
        StatefulWorkflow(mock[CommonExecutionContext], worklfowWithResults, runningExecutionFactory)

      statefulWorkflow.updateStructure(workflow)

      statefulWorkflow.currentExecution shouldBe runningExecution // not changed
      statefulWorkflow.currentAdditionalData shouldBe newThirdPartyData // updated
    }
    "not change states when only thirdPartyData is updated" in {
      val workflowId = Workflow.Id.randomId
      val metadata = WorkflowMetadata(WorkflowType.Batch, "1.0.0")
      val node1Id: Node.Id = Node.Id.randomId
      val node2Id: Node.Id = Node.Id.randomId
      val state1: NodeState = NodeState(
        Completed(DateTimeConverter.now, DateTimeConverter.now, Seq()),
        Some(EntitiesMap()))
      val state2: NodeState = NodeState(
        Completed(DateTimeConverter.now, DateTimeConverter.now, Seq()),
        Some(EntitiesMap()))
      val states = Map(
        node1Id -> NodeStateWithResults(state1, Map(), None),
        node2Id -> NodeStateWithResults(state2, Map(), None))
      val statefulGraph = StatefulGraph(createGraph(node1Id, node2Id), states, None)
      val successfulExecution = IdleExecution(statefulGraph)
      val statefulWorkflow = new StatefulWorkflow(
        mock[CommonExecutionContext],
        workflowId,
        metadata,
        WorkflowInfo.forId(workflowId),
        JsObject(),
        successfulExecution,
        new MockStateInferrer())

      statefulWorkflow.updateStructure(
        Workflow(metadata, createGraph(node1Id, node2Id), JsObject("foo" -> JsString("bar"))))

      val newStates = statefulWorkflow.executionReport.states
      newStates(node1Id) shouldBe state1
      newStates(node2Id) shouldBe state2
    }
  }

  def createGraph(
      node1Id: Node.Id,
      node2Id: Node.Id): DeeplangGraph = {
    val node1 = Node(node1Id, mockOperation(0, 1, DOperation.Id.randomId, "a", "b"))
    val node2 = Node(node2Id, mockOperation(1, 0, DOperation.Id.randomId, "c", "d"))
    DeeplangGraph(
      Set(node1, node2),
      Set(Edge(Endpoint(node1.id, 0), Endpoint(node2.id, 0))))
  }

  private class MockStateInferrer extends StateInferrer {
    override def inferState(execution: Execution): InferredState = mock[InferredState]
  }
}
