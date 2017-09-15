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

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest._
import org.scalatest.concurrent.{Eventually, ScalaFutures, ScaledTimeSpans}
import org.scalatest.mock.MockitoSugar

import io.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.{DOperable, DOperation, ExecutionContext}
import io.deepsense.graph.Node.Id
import io.deepsense.graph.nodestate.NodeState
import io.deepsense.graph.{Node, ReadyNode, StatefulGraph, graphstate}
import io.deepsense.messageprotocol.WorkflowExecutorProtocol.{ExecutionStatus}
import io.deepsense.models.entities.Entity
import io.deepsense.models.workflows.Workflow
import io.deepsense.reportlib.model.ReportContent
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages._
import io.deepsense.workflowexecutor.WorkflowNodeExecutorActor.Messages.Start
import io.deepsense.workflowexecutor.communication.Connect

class WorkflowExecutorActorSpec
  extends TestKit(ActorSystem("WorkflowExecutorActorSpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with MockitoSugar
  with Eventually
  with ScalaFutures
  with ScaledTimeSpans {

  "WorkflowExecutorActor" when {
    "received Connect" when {
      "no graph was sent" should {
        "send empty status" in {
          val probe = TestProbe()
          val statusListener = TestProbe()
          val wea = TestActorRef(new WorkflowExecutorActor(
            mock[ExecutionContext],
            mock[GraphNodeExecutorFactory],
            Some(statusListener.ref)))
          probe.send(wea, Connect(Workflow.Id.randomId))
          verifyStatusSent(statusListener)
        }
      }
      "a graph was sent" should {
        "send graph's status" in {
          val (probe, wea, graph, _, _, statusListener) = readyNodesFixture()
          become(wea, graph)
          probe.send(wea, Connect(Workflow.Id.randomId))
          verifyStatusSent(statusListener)
        }
      }
    }
    "received Launch" should {
      "enqueue graph" in {
        val (probe, wea, graph, _, _, _) = readyNodesFixture()
        probe.send(wea, Launch(graph))
        eventually {
          verify(graph).enqueue
        }
      }
      "infer and apply graph knowledge and pass it to graph" in {
        val (probe, wea, graph, _, _, _) = readyNodesFixture()
        probe.send(wea, Launch(graph))
        eventually {
          verify(graph).inferAndApplyKnowledge(executionContext.inferContext)
        }
      }
      "launch ready nodes and send status if inference ok" in {
        val (probe, wea, graph, readyNodes, executors, statusListener) = readyNodesFixture()
        probe.send(wea, Launch(graph))
        verifyNodesStarted(graph, readyNodes, executors)
        verifyStatusSent(statusListener)
      }
      "infer graph knowledge, fail graph and send status on exception" in {
        val probe = TestProbe()
        val statusListener = TestProbe()
        val graph = readyGraphWithThrowingInference()
        val wea = TestActorRef(new WorkflowExecutorActor(
          executionContext,
          nodeExecutorFactory(),
          Some(statusListener.ref)))
        probe.send(wea, Launch(graph))
        eventually {
          verify(graph).inferAndApplyKnowledge(executionContext.inferContext)
        }

        verifyStatus(statusListener){ _.graphState shouldBe 'Failed }
      }
    }
    "received NodeCompleted" should {
      "tell graph about it" in {
        val (probe, wea, graph, _, _, _) = readyNodesFixture()
        become(wea, graph)
        val (completedId, nodeCompletedMessage, results) = nodeCompleted()
        probe.send(wea, nodeCompletedMessage)
        eventually {
          verify(graph).nodeFinished(completedId, results.doperables.keys.toSeq)
        }
      }
      "launch ready nodes and send status" when {
        "ready nodes exist" in {
          val (probe, wea, graph, readyNodes, executors, statusListener) = readyNodesFixture()
          become(wea, graph)
          val (_, nodeCompletedMessage, _) = nodeCompleted()
          probe.send(wea, nodeCompletedMessage)
          verifyNodesStarted(graph, readyNodes, executors)
          verifyStatusSent(statusListener)
        }
      }
      "send graph status" when {
        "there is no ready nodes" in {
          val (probe, wea, graph, _, _, statusListener) = noReadyNodesFixture()
          become(wea, graph)
          val (_, nodeCompletedMessage, _) = nodeCompleted()
          probe.send(wea, nodeCompletedMessage)
          verifyStatusSent(statusListener)
        }
      }
      "finish execution, if graph Completed/Failed" in {
        val (probe, wea, graph, _, _, statusListener) = readyNodesFixture()
        val completed = completedGraph()
        when(graph.nodeFinished(any(), any())).thenReturn(completed)
        become(wea, graph)
        val (_, nodeCompletedMessage, _) = nodeCompleted()
        probe.send(wea, nodeCompletedMessage)

        verifyStatus(statusListener){ _.graphState shouldBe 'Completed }
      }
    }
    "received NodeFailed" should {
      "tell graph about it" in {
        val (probe, wea, graph, _, _, statusListener) = readyNodesFixture()
        become(wea, graph)
        val (completedId, nodeFailedMessage, cause) = nodeFailed()
        probe.send(wea, nodeFailedMessage)
        eventually {
          verify(graph).nodeFailed(completedId, cause)
        }
        verifyStatusSent(statusListener)
      }
      "launch ready nodes" when {
        "ready nodes exist" in {
          val (probe, wea, graph, readyNodes, executors, statusListener) = readyNodesFixture()
          become(wea, graph)
          val (_, nodeFailedMessage, _) = nodeFailed()
          probe.send(wea, nodeFailedMessage)
          verifyNodesStarted(graph, readyNodes, executors)
          verifyStatusSent(statusListener)
        }
      }
      "finish execution and persist its state if graph Completed/Failed" in {
        val (probe, wea, graph, _, _, statusListener) = readyNodesFixture()
        val failed = failedGraph()
        when(graph.nodeFailed(any(), any())).thenReturn(failed)
        become(wea, graph)
        val (_, nodeFailedMessage, _) = nodeFailed()
        probe.send(wea, nodeFailedMessage)

        verifyStatus(statusListener){ _.graphState shouldBe 'Failed }

        probe.send(wea, Connect(Workflow.Id.randomId)) // Id doesn't matter
        eventually {
          verifyStatus(statusListener){ _.graphState shouldBe 'Failed }
        }
      }
    }
  }

  def verifyStatusSent(statusListener: TestProbe): Unit = {
    verifyStatus(statusListener)(_ => ())
  }

  def verifyStatus(statusListener: TestProbe)(f: (ExecutionStatus) => Unit): Unit = {
    eventually {
      f(statusListener.expectMsgClass(classOf[ExecutionStatus]))
    }
  }

  def become(
      wea: TestActorRef[WorkflowExecutorActor],
      graph: StatefulGraph): Unit = {
    wea.underlyingActor.context.become(wea.underlyingActor.launched(graph))
  }

  val executionContext = mock[ExecutionContext]

  def verifyNodesStarted(
      graph: StatefulGraph,
      readyNodes: Seq[ReadyNode],
      executors: Seq[TestProbe]): Unit = {
    eventually {
      readyNodes.foreach { rn => verify(graph).nodeStarted(rn.node.id) }
      executors.foreach {
        _.expectMsg(Start())
      }
    }
  }

  def mockResults(size: Int): NodeExecutionResults = {
    val ids = (1 to size).map(_ => Entity.Id.randomId).toSeq
    val reports = ids.map { id => id -> mock[ReportContent]}.toMap
    val operables = ids.map { id => id -> mock[DOperable]}.toMap
    NodeExecutionResults(reports, operables)
  }

  def mockReadyNode(): ReadyNode = {
    val node = mock[Node]
    when(node.id).thenReturn(Node.Id.randomId)
    val dOperation = mock[DOperation]
    when(dOperation.name).thenReturn("mockedName")
    when(node.operation).thenReturn(dOperation)
    ReadyNode(node, Seq())
  }

  def nodeCompleted(): (Id, NodeCompleted, NodeExecutionResults) = {
    val completedId = Node.Id.randomId
    val results = mockResults(2)
    (completedId, NodeCompleted(completedId, results), results)
  }

  def nodeFailed(): (Id, NodeFailed, Exception) = {
    val failedId = Node.Id.randomId
    val cause = new IllegalStateException("A node failed because a test told him to do so!")
    (failedId, NodeFailed(failedId, cause), cause)
  }

  def graphWithNodes(readyNodes: Seq[ReadyNode]): StatefulGraph = {
    val graph = mock[StatefulGraph]
    when(graph.state).thenReturn(graphstate.Running)
    when(graph.states).thenReturn(Map[Node.Id, NodeState]())
    when(graph.reset).thenReturn(graph)

    when(graph.readyNodes).thenReturn(readyNodes)
    when(graph.nodeStarted(any())).thenReturn(graph)
    graph
  }

  def readyGraph(): (StatefulGraph, Seq[ReadyNode]) = {
    val readyNodes = Seq(mockReadyNode(), mockReadyNode())
    (graphWithNodes(readyNodes), readyNodes)
  }

  def noReadyGraph(): StatefulGraph = graphWithNodes(Seq.empty)

  def graphWithOkInference(
      graph: StatefulGraph,
      readyNodes: Seq[ReadyNode]): (StatefulGraph, Seq[ReadyNode]) = {

    when(graph.enqueue).thenReturn(graph)
    when(graph.inferAndApplyKnowledge(any())).thenReturn(graph)

    (graph, readyNodes)
  }

  def readyGraphWithOkInference(): (StatefulGraph, Seq[ReadyNode]) = {
    val (graph, readyNodes) = readyGraph()
    graphWithOkInference(graph, readyNodes)
  }


  def noReadyGraphWithOkInference(): StatefulGraph = {
    val graph = noReadyGraph()

    when(graph.enqueue).thenReturn(graph)
    when(graph.inferAndApplyKnowledge(any())).thenReturn(graph)

    graph
  }

  def failedGraph(): StatefulGraph = {
    val graph = mock[StatefulGraph]
    when(graph.states).thenReturn(Map[Node.Id, NodeState]())
    when(graph.state).thenReturn(graphstate.Failed(
      FailureDescription(
        DeepSenseFailure.Id.randomId,
        FailureCode.UnexpectedError,
        "Mock title"
      )))
    when(graph.readyNodes).thenReturn(Seq())
    graph
  }

  def completedGraph(): StatefulGraph = {
    val graph = mock[StatefulGraph]
    when(graph.state).thenReturn(graphstate.Completed)
    when(graph.states).thenReturn(Map[Node.Id, NodeState]())
    when(graph.readyNodes).thenReturn(Seq())
    graph
  }

  def readyGraphWithThrowingInference(): StatefulGraph = {
    val (graph, _) = readyGraph()
    when(graph.enqueue).thenReturn(graph)
    val failed = failedGraph()
    when(graph.inferAndApplyKnowledge(any(classOf[InferContext])))
      .thenReturn(failed)

    graph
  }

  def nodeExecutorFactory(): GraphNodeExecutorFactory = {
    val factory = mock[GraphNodeExecutorFactory]
    when(factory.createGraphNodeExecutor(any(), any(), any(), any())).thenReturn(TestProbe().ref)
    factory
  }

  def fixture(graph: StatefulGraph, readyNodes: Seq[ReadyNode]):
  (TestProbe,
    TestActorRef[WorkflowExecutorActor],
    StatefulGraph,
    Seq[ReadyNode],
    Seq[TestProbe],
    TestProbe) = {
    val testProbe = TestProbe()
    val statusListener = TestProbe()
    when(graph.nodeFinished(any(), any())).thenReturn(graph)
    when(graph.nodeFailed(any(), any())).thenReturn(graph)
    val executors = Seq(TestProbe(), TestProbe())

    val factory = mock[GraphNodeExecutorFactory]
    when(factory.createGraphNodeExecutor(any(), any(), any(), any()))
      .thenAnswer(new Answer[ActorRef] {
        var i = 0
        override def answer(invocation: InvocationOnMock): ActorRef = {
          val executor = executors(i)
          i = i + 1
          executor.ref
        }
      })

    val testedActor = TestActorRef(new WorkflowExecutorActor(
      executionContext, factory, Some(statusListener.ref)))

    (testProbe, testedActor, graph, readyNodes, executors, statusListener)
  }

  def readyNodesFixture():
      (TestProbe,
        TestActorRef[WorkflowExecutorActor],
        StatefulGraph,
        Seq[ReadyNode],
        Seq[TestProbe],
        TestProbe) = {

    val (graph, readyNodes) = readyGraphWithOkInference()
    fixture(graph, readyNodes)
  }

  def noReadyNodesFixture():
  (TestProbe,
    TestActorRef[WorkflowExecutorActor],
    StatefulGraph,
    Seq[ReadyNode],
    Seq[TestProbe],
    TestProbe) = {
    fixture(noReadyGraphWithOkInference(), Seq.empty)
  }
}
