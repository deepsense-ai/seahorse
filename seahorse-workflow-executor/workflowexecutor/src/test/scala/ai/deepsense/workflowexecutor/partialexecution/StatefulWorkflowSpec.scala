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

import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import spray.json.{JsObject, JsString}

import ai.deepsense.commons.datetime.DateTimeConverter
import ai.deepsense.commons.models.Id
import ai.deepsense.deeplang.{CommonExecutionContext, DOperation}
import ai.deepsense.graph.nodestate.{Completed, Draft}
import ai.deepsense.graph._
import ai.deepsense.models.json.workflow.WorkflowTestSupport
import ai.deepsense.models.workflows._

class StatefulWorkflowSpec extends WorkflowTestSupport with MockitoSugar {


  class LocalData {
    val node1Id: Node.Id = Node.Id.randomId
    val node2Id: Node.Id = Node.Id.randomId
    val node3Id: Node.Id = Node.Id.randomId

    val graph1 = createGraph(node1Id, node2Id)
    val graph2 = createGraph(node1Id, node3Id)
    val originalGraph: DeeplangGraph = DeeplangGraph()

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

    val workflowId = Workflow.Id.randomId
    val metadata = WorkflowMetadata(WorkflowType.Batch, "1.0.0")

    val workflowWithResults = WorkflowWithResults(
      workflowId,
      metadata,
      originalGraph,
      JsObject(),
      ExecutionReport(Map()),
      WorkflowInfo.empty())

    val workflow = Workflow(
      WorkflowMetadata(WorkflowType.Batch, "1.0.0"),
      mock[DeeplangGraph],
      mock[JsObject])

    val successfulExecution = IdleExecution(statefulGraph)

    val statefulWorkflow = new StatefulWorkflow(
      mock[CommonExecutionContext],
      workflowId,
      metadata,
      WorkflowInfo.forId(workflowId),
      JsObject(),
      successfulExecution,
      new MockStateInferrer())

    def createGraph(node1Id: Node.Id,
                    node2Id: Node.Id): DeeplangGraph = {
      val node1 = Node(node1Id, mockOperation(0, 1, DOperation.Id.randomId, "a", "b"))
      val node2 = Node(node2Id, mockOperation(1, 0, DOperation.Id.randomId, "c", "d"))
      DeeplangGraph(
        Set(node1, node2),
        Set(Edge(Endpoint(node1.id, 0), Endpoint(node2.id, 0))))
    }
  }

  object ExecutionReportData {
    val nodeId: Id = Node.Id.randomId
    def apply(): ExecutionReport = {
      ExecutionReport(Map(nodeId -> NodeState(Draft(), Some(EntitiesMap()))))
    }
  }


  "StatefulWorkflow" should {

    val workflow = Workflow(
      WorkflowMetadata(WorkflowType.Batch, "1.0.0"),
      mock[DeeplangGraph],
      mock[JsObject])
    val executionReport = ExecutionReportData()

    "actually updateStruct only" when {
      "execution is idle (but third party should be updated anyway)" in {
        val ld = new LocalData
        val updatedExecution = mock[IdleExecution]
        when(updatedExecution.executionReport).thenReturn(executionReport)
        def idleExecutionFactory(graph: StatefulGraph): Execution = {
          val exec = mock[IdleExecution]
          when(exec.updateStructure(workflow.graph)).thenReturn(updatedExecution)
          exec
        }
        val statefulWorkflow =
          StatefulWorkflow(mock[CommonExecutionContext],
            ld.workflowWithResults, idleExecutionFactory)

        statefulWorkflow.updateStructure(workflow)

        statefulWorkflow.currentExecution shouldBe updatedExecution
        statefulWorkflow.currentAdditionalData shouldBe workflow.additionalData
      }
    }
    "ignore struct update and only update thirdPartyData when execution is started" in {
      val ld = new LocalData
      val runningExecution = mock[RunningExecution]
      def runningExecutionFactory(graph: StatefulGraph): Execution = runningExecution
      when(runningExecution.executionReport).thenReturn(executionReport)
      val statefulWorkflow =
        StatefulWorkflow(mock[CommonExecutionContext],
          ld.workflowWithResults, runningExecutionFactory)

      statefulWorkflow.updateStructure(workflow)

      statefulWorkflow.currentExecution shouldBe runningExecution // not changed
      statefulWorkflow.currentAdditionalData shouldBe workflow.additionalData // updated
    }
    "not change states when only thirdPartyData is updated" in {
      val ld = new LocalData

      ld.statefulWorkflow.updateStructure(
        Workflow(ld.metadata, ld.graph1,
          JsObject("foo" -> JsString("bar"))))

      val newStates = ld.statefulWorkflow.executionReport.states
      newStates(ld.node1Id) shouldBe ld.state1
      newStates(ld.node2Id) shouldBe ld.state2
    }
    "saved 0 nodes removed" when {
      "removed 0 nodes" in {
        val ld = new LocalData

        val nodes = ld.statefulWorkflow.
          getNodesRemovedByWorkflow(Workflow(ld.metadata, ld.graph1, mock[JsObject]))
        nodes.size shouldBe 0
      }
    }
    "saved 1 node removed" when {
      "removed 1 node" in {
        val ld = new LocalData

        val nodes =
          ld.statefulWorkflow.
            getNodesRemovedByWorkflow(Workflow(ld.metadata, ld.graph2, mock[JsObject]))
        nodes.size shouldBe 1
        nodes.head.id shouldBe ld.node2Id
      }
    }
  }


  private class MockStateInferrer extends StateInferrer {
    override def inferState(execution: Execution): InferredState = mock[InferredState]
  }
}
