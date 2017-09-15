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

import scala.concurrent.Promise

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
import io.deepsense.deeplang.{DOperable, DOperation, ExecutionContext}
import io.deepsense.graph.Node.Id
import io.deepsense.graph.nodestate.NodeState
import io.deepsense.graph.{Node, ReadyNode, StatefulGraph, graphstate}
import io.deepsense.models.entities.Entity
import io.deepsense.reportlib.model.ReportContent
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages._
import io.deepsense.workflowexecutor.WorkflowNodeExecutorActor.Messages.Start

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
    "received Launch" should {
      "enqueue graph" in {
        val (probe, wea, graph, _, _) = readyNodesFixture()
        probe.send(wea, Launch(graph, mock[Promise[GraphFinished]]))
        eventually {
          verify(graph).enqueue
        }
      }
      "infer and apply graph knowledge and pass it to graph" in {
        val (probe, wea, graph, _, _) = readyNodesFixture()
        probe.send(wea, Launch(graph, mock[Promise[GraphFinished]]))
        eventually {
          verify(graph).inferAndApplyKnowledge(executionContext)
        }
      }
      "launch ready nodes if inference ok" in {
        val (probe, wea, graph, readyNodes, executors) = readyNodesFixture()
        probe.send(wea, Launch(graph, mock[Promise[GraphFinished]]))
        verifyNodesStarted(graph, readyNodes, executors)
      }
      "infer graph knowledge and fail graph on exception" in {
        val probe = TestProbe()
        val graph = readyGraphWithThrowingInference()
        val wea = TestActorRef(new WorkflowExecutorActor(
          executionContext, nodeExecutorFactory(), mock[SystemShutdowner]))
        val resultPromise: Promise[GraphFinished] = Promise()

        probe.send(wea, Launch(graph, resultPromise))
        eventually {
          verify(graph).inferAndApplyKnowledge(executionContext)

          resultPromise shouldBe 'Completed
          whenReady(resultPromise.future) { result =>
            result.graph.state shouldBe 'Failed
          }
        }
      }
    }
    "received NodeCompleted" should {
      "tell graph about it" in {
        val (probe, wea, graph, _, _) = readyNodesFixture()
        val resultPromise = mock[Promise[GraphFinished]]
        become(wea, graph, resultPromise)
        val (completedId, nodeCompletedMessage, results) = nodeCompleted()
        probe.send(wea, nodeCompletedMessage)
        eventually {
          verify(graph).nodeFinished(completedId, results.doperables.keys.toSeq)
        }
      }
      "launch ready nodes" when {
        "ready nodes exist" in {
          val (probe, wea, graph, readyNodes, executors) = readyNodesFixture()
          become(wea, graph, mock[Promise[GraphFinished]])
          val (_, nodeCompletedMessage, _) = nodeCompleted()
          probe.send(wea, nodeCompletedMessage)
          verifyNodesStarted(graph, readyNodes, executors)
        }
      }
      "finish execution if graph Completed/Failed" in {
        val (probe, wea, graph, _, _) = readyNodesFixture()
        val completed = completedGraph()
        when(graph.nodeFinished(any(), any())).thenReturn(completed)
        val resultPromise: Promise[GraphFinished] = Promise()
        become(wea, graph, resultPromise)
        val (_, nodeCompletedMessage, _) = nodeCompleted()
        probe.send(wea, nodeCompletedMessage)

        eventually {
          resultPromise shouldBe 'Completed
          whenReady(resultPromise.future) { result =>
            result.graph.state shouldBe 'Completed
          }
        }
      }
    }
    "received NodeFailed" should {
      "tell graph about it" in {
        val (probe, wea, graph, _, _) = readyNodesFixture()
        become(wea, graph, mock[Promise[GraphFinished]])
        val (completedId, nodeFailedMessage, cause) = nodeFailed()
        probe.send(wea, nodeFailedMessage)
        eventually {
          verify(graph).nodeFailed(completedId, cause)
        }
      }
      "launch ready nodes" when {
        "ready nodes exist" in {
          val (probe, wea, graph, readyNodes, executors) = readyNodesFixture()
          become(wea, graph, mock[Promise[GraphFinished]])
          val (_, nodeFailedMessage, _) = nodeFailed()
          probe.send(wea, nodeFailedMessage)
          verifyNodesStarted(graph, readyNodes, executors)
        }
      }
      "finish execution if graph Completed/Failed" in {
        val (probe, wea, graph, _, _) = readyNodesFixture()
        val failed = failedGraph()
        when(graph.nodeFailed(any(), any())).thenReturn(failed)
        val resultPromise: Promise[GraphFinished] = Promise()
        become(wea, graph, resultPromise)
        val (_, nodeFailedMessage, _) = nodeFailed()
        probe.send(wea, nodeFailedMessage)

        eventually {
          resultPromise shouldBe 'Completed
          whenReady(resultPromise.future) { result =>
            result.graph.state shouldBe 'Failed
          }
        }
      }
    }
  }

  def become(
      wea: TestActorRef[WorkflowExecutorActor],
      graph: StatefulGraph,
      p: Promise[GraphFinished]): Unit = {
    wea.underlyingActor.context.become(wea.underlyingActor.launched(graph, p))
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

  def readyGraph(): (StatefulGraph, Seq[ReadyNode]) = {
    val graph = mock[StatefulGraph]
    when(graph.state).thenReturn(graphstate.Running)
    when(graph.states).thenReturn(Map[Node.Id, NodeState]())

    val readyNodes = Seq(mockReadyNode(), mockReadyNode())
    when(graph.readyNodes).thenReturn(readyNodes)
    when(graph.nodeStarted(any())).thenReturn(graph)
    (graph, readyNodes)
  }

  def readyGraphWithOkInference(): (StatefulGraph, Seq[ReadyNode]) = {
    val (graph, readyNodes) = readyGraph()

    when(graph.enqueue).thenReturn(graph)
    when(graph.inferAndApplyKnowledge(any())).thenReturn(graph)

    (graph, readyNodes)
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
    when(graph.inferAndApplyKnowledge(any(classOf[ExecutionContext])))
      .thenReturn(failed)

    graph
  }

  def nodeExecutorFactory(): GraphNodeExecutorFactory = {
    val factory = mock[GraphNodeExecutorFactory]
    when(factory.createGraphNodeExecutor(any(), any(), any(), any())).thenReturn(TestProbe().ref)
    factory
  }

  def readyNodesFixture():
      (TestProbe,
        TestActorRef[WorkflowExecutorActor],
        StatefulGraph,
        Seq[ReadyNode],
        Seq[TestProbe]) = {
    val testProbe = TestProbe()
    val (graph, readyNodes) = readyGraphWithOkInference()
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
      executionContext, factory, mock[SystemShutdowner]))

    (testProbe, testedActor, graph, readyNodes, executors)
  }
}
