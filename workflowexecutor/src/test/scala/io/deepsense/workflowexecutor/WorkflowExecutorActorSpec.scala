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
import io.deepsense.graph._
import io.deepsense.graph.nodestate.NodeState
import io.deepsense.models.entities.Entity
import io.deepsense.models.workflows.Workflow
import io.deepsense.reportlib.model.ReportContent
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages._
import io.deepsense.workflowexecutor.WorkflowNodeExecutorActor.Messages.Start
import io.deepsense.workflowexecutor.communication.{Connect, ExecutionStatus}
import io.deepsense.workflowexecutor.partialexecution.{Execution, PartialExecution}

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
          val publisher = TestProbe()
          val statusListener = TestProbe()
          val wea = TestActorRef(new WorkflowExecutorActor(
            mock[ExecutionContext],
            mock[GraphNodeExecutorFactory],
            createExecutionFactory(emptyExecution),
            Some(statusListener.ref),
            Some(system.actorSelection(publisher.ref.path))))
          probe.send(wea, Connect(Workflow.Id.randomId))
          verifyStatusSent(Seq(statusListener, publisher))
        }
      }
      "a graph was sent" should {
        "send graph's status" in {
          val (probe, wea, execution, _, _, statusListeners) = readyNodesFixture()
          become(wea, execution)
          probe.send(wea, Connect(Workflow.Id.randomId))
          verifyStatusSent(statusListeners)
        }
      }
    }
    "received Launch" should {
      "enqueue graph" in {
        val (probe, wea, execution, _, _, _) = readyNodesFixture()
        sendLaunch(probe, wea)
        eventually {
          verify(execution).enqueue(any())
        }
      }
      "infer and apply graph knowledge and pass it to graph" in {
        val (probe, wea, execution, _, _, _) = readyNodesFixture()
        sendLaunch(probe, wea)
        eventually {
          verify(execution).inferAndApplyKnowledge(executionContext.inferContext)
        }
      }
      "launch ready nodes and send status if inference ok" in {
        val (probe, wea, execution, readyNodes, executors, statusListeners) = readyNodesFixture()
        sendLaunch(probe, wea)
        verifyNodesStarted(execution, readyNodes, executors)
        verifyStatusSent(statusListeners)
      }

      "infer graph knowledge, fail graph and send status on exception" in {
        val probe = TestProbe()
        val statusListener = TestProbe()
        val publisher = TestProbe()
        val execution = readyExecutionWithThrowingInference()
        val wea = TestActorRef(new WorkflowExecutorActor(
          executionContext,
          nodeExecutorFactory(),
          createExecutionFactory(execution),
          Some(statusListener.ref),
          Some(system.actorSelection(publisher.ref.path))))
        sendLaunch(probe, wea)
        eventually {
          verify(execution).inferAndApplyKnowledge(executionContext.inferContext)
        }

        verifyStatus(Seq(statusListener, publisher)){ _.graphState shouldBe 'Failed }
      }
    }
    "received NodeCompleted" should {
      "tell graph about it" in {
        val (probe, wea, execution, _, _, _) = readyNodesFixture()
        become(wea, execution)
        val (completedId, nodeCompletedMessage, results) = nodeCompleted()
        probe.send(wea, nodeCompletedMessage)
        eventually {
          verify(execution).nodeFinished(completedId, results.doperables.keys.toSeq)
        }
      }
      "launch ready nodes and send status" when {
        "ready nodes exist" in {
          val (probe, wea, graph, readyNodes, executors, statusListeners) = readyNodesFixture()
          become(wea, graph)
          val (_, nodeCompletedMessage, _) = nodeCompleted()
          probe.send(wea, nodeCompletedMessage)
          verifyNodesStarted(graph, readyNodes, executors)
          verifyStatusSent(statusListeners)
        }
      }
      "send graph status" when {
        "there is no ready nodes" in {
          val (probe, wea, graph, _, _, statusListeners) = noReadyNodesFixture()
          become(wea, graph)
          val (_, nodeCompletedMessage, _) = nodeCompleted()
          probe.send(wea, nodeCompletedMessage)
          verifyStatusSent(statusListeners)
        }
      }
      "finish execution, if graph Completed/Failed" in {
        val (probe, wea, execution, _, _, statusListeners) = readyNodesFixture()
        val completed = completedExecution()
        when(execution.nodeFinished(any(), any())).thenReturn(completed)
        become(wea, execution)
        val (_, nodeCompletedMessage, _) = nodeCompleted()
        probe.send(wea, nodeCompletedMessage)

        verifyStatus(statusListeners){ _.graphState shouldBe 'Completed }
      }
    }
    "received NodeFailed" should {
      "tell graph about it" in {
        val (probe, wea, execution, _, _, statusListeners) = readyNodesFixture()
        become(wea, execution)
        val (completedId, nodeFailedMessage, cause) = nodeFailed()
        probe.send(wea, nodeFailedMessage)
        eventually {
          verify(execution).nodeFailed(completedId, cause)
        }
        verifyStatusSent(statusListeners)
      }
      "launch ready nodes" when {
        "ready nodes exist" in {
          val (probe, wea, graph, readyNodes, executors, statusListeners) = readyNodesFixture()
          become(wea, graph)
          val (_, nodeFailedMessage, _) = nodeFailed()
          probe.send(wea, nodeFailedMessage)
          verifyNodesStarted(graph, readyNodes, executors)
          verifyStatusSent(statusListeners)
        }
      }
      "finish execution and persist its state if graph Completed/Failed" in {
        val (probe, wea, graph, _, _, statusListeners) = readyNodesFixture()
        val failed = failedExecution()
        when(graph.nodeFailed(any(), any())).thenReturn(failed)
        become(wea, graph)
        val (_, nodeFailedMessage, _) = nodeFailed()
        probe.send(wea, nodeFailedMessage)

        verifyStatus(statusListeners){ _.graphState shouldBe 'Failed }

        probe.send(wea, Connect(Workflow.Id.randomId)) // Id doesn't matter
        eventually {
          verifyStatus(statusListeners){ _.graphState shouldBe 'Failed }
        }
      }
    }
  }

  def sendLaunch(probe: TestProbe, wea: TestActorRef[WorkflowExecutorActor]): Unit = {
    probe.send(wea, Launch(DirectedGraph()))
  }

  def emptyExecution: Execution = PartialExecution(DirectedGraph())

  def createExecutionFactory(execution: Execution): ExecutionFactory = {
    val factory = mock[ExecutionFactory]
    when(factory.create(any())).thenReturn(execution)
    when(factory.empty).thenReturn(emptyExecution)
    factory
  }

  def verifyStatusSent(receivers: Seq[TestProbe]): Unit = {
    verifyStatus(receivers)(_ => ())
  }

  def verifyStatus(receivers: Seq[TestProbe])(f: (ExecutionStatus) => Unit): Unit = {
    eventually {
      receivers.foreach { receiver =>
        f(receiver.expectMsgClass(classOf[ExecutionStatus]))
      }
    }
  }

  def become(
      wea: TestActorRef[WorkflowExecutorActor],
      execution: Execution): Unit = {
    wea.underlyingActor.context
      .become(wea.underlyingActor.launched(execution))
  }

  val executionContext = mock[ExecutionContext]

  def verifyNodesStarted(
      execution: Execution,
      readyNodes: Seq[ReadyNode],
      executors: Seq[TestProbe]): Unit = {
    eventually {
      readyNodes.foreach { rn => verify(execution).nodeStarted(rn.node.id) }
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

  def runningExecutionWithReadyNodes(readyNodes: Seq[ReadyNode]): Execution = {
    val graph = mock[Execution]
    when(graph.state).thenReturn(graphstate.Running)
    when(graph.states).thenReturn(Map[Node.Id, NodeState]())

    when(graph.readyNodes).thenReturn(readyNodes)
    when(graph.nodeStarted(any())).thenReturn(graph)
    graph
  }

  def readyExecution(): (Execution, Seq[ReadyNode]) = {
    val readyNodes = Seq(mockReadyNode(), mockReadyNode())
    (runningExecutionWithReadyNodes(readyNodes), readyNodes)
  }

  def noReadyExecution(): Execution = runningExecutionWithReadyNodes(Seq.empty)

  def executionWithOkInference(
      execution: Execution,
      readyNodes: Seq[ReadyNode]): (Execution, Seq[ReadyNode]) = {

    when(execution.enqueue(any())).thenReturn(execution)
    when(execution.inferAndApplyKnowledge(any())).thenReturn(execution)

    (execution, readyNodes)
  }

  def readyExecutionWithOkInference(): (Execution, Seq[ReadyNode]) = {
    val (graph, readyNodes) = readyExecution()
    executionWithOkInference(graph, readyNodes)
  }


  def noReadyExecutionWithOkInference(): Execution = {
    val graph = noReadyExecution()

    when(graph.enqueue(any())).thenReturn(graph)
    when(graph.inferAndApplyKnowledge(any())).thenReturn(graph)

    graph
  }

  def failedExecution(): Execution = {
    val graph = mock[Execution]
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

  def completedExecution(): Execution = {
    val graph = mock[Execution]
    when(graph.state).thenReturn(graphstate.Completed)
    when(graph.states).thenReturn(Map[Node.Id, NodeState]())
    when(graph.readyNodes).thenReturn(Seq())
    graph
  }

  def readyExecutionWithThrowingInference(): Execution = {
    val (graph, _) = readyExecution()
    when(graph.enqueue(any())).thenReturn(graph)
    val failed = failedExecution()
    when(graph.inferAndApplyKnowledge(any(classOf[InferContext])))
      .thenReturn(failed)

    graph
  }

  def nodeExecutorFactory(): GraphNodeExecutorFactory = {
    val factory = mock[GraphNodeExecutorFactory]
    when(factory.createGraphNodeExecutor(any(), any(), any(), any())).thenReturn(TestProbe().ref)
    factory
  }

  def fixture(execution: Execution, readyNodes: Seq[ReadyNode]):
  (TestProbe,
    TestActorRef[WorkflowExecutorActor],
    Execution,
    Seq[ReadyNode],
    Seq[TestProbe],
    Seq[TestProbe]) = {
    val testProbe = TestProbe()
    val statusListener = TestProbe()
    val publisher = TestProbe()
    when(execution.nodeFinished(any(), any())).thenReturn(execution)
    when(execution.nodeFailed(any(), any())).thenReturn(execution)
    val executors = Seq(TestProbe(), TestProbe())

    val nodeExecutorFactory = mock[GraphNodeExecutorFactory]
    when(nodeExecutorFactory.createGraphNodeExecutor(any(), any(), any(), any()))
      .thenAnswer(new Answer[ActorRef] {
        var i = 0
        override def answer(invocation: InvocationOnMock): ActorRef = {
          val executor = executors(i)
          i = i + 1
          executor.ref
        }
      })

    val executionFactory = createExecutionFactory(execution)

    val testedActor = TestActorRef(new WorkflowExecutorActor(
      executionContext,
      nodeExecutorFactory,
      executionFactory,
      Some(statusListener.ref),
      Some(system.actorSelection(publisher.ref.path))))

    (testProbe, testedActor, execution, readyNodes, executors, Seq(statusListener, publisher))
  }

  def readyNodesFixture():
      (TestProbe,
        TestActorRef[WorkflowExecutorActor],
        Execution,
        Seq[ReadyNode],
        Seq[TestProbe],
        Seq[TestProbe]) = {

    val (execution, readyNodes) = readyExecutionWithOkInference()
    fixture(execution, readyNodes)
  }

  def noReadyNodesFixture():
  (TestProbe,
    TestActorRef[WorkflowExecutorActor],
    Execution,
    Seq[ReadyNode],
    Seq[TestProbe],
    Seq[TestProbe]) = {
    fixture(noReadyExecutionWithOkInference(), Seq.empty)
  }
}
