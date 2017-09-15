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

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.{DOperable, DOperation, ExecutionContext}
import io.deepsense.graph.Node.Id
import io.deepsense.graph._
import io.deepsense.graph.nodestate.{Aborted, Completed, NodeState, Queued, Running}
import io.deepsense.models.entities.Entity
import io.deepsense.models.workflows.{EntitiesMap, Workflow}
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
          verifyStatusSent(Seq(publisher))
        }
      }
      "a graph was sent" should {
        "send whole graph's status" in {
          val (probe, wea, _, _, _, statusListeners, _) = readyNodesFixture()
          val ids: IndexedSeq[Id] = IndexedSeq(Node.Id.randomId, Node.Id.randomId, Node.Id.randomId)
          val (_, execution) = simpleExecution(ids, Some(ids.head))
          become(wea, execution)
          probe.send(wea, Connect(Workflow.Id.randomId))
          eventually {
            statusListeners.foreach { receiver =>
              val status = receiver.expectMsgClass(classOf[ExecutionStatus])
              status.executionFailure shouldBe None
              status.nodes shouldBe execution.states
              status.resultEntities shouldBe EntitiesMap()
            }
          }
        }
      }
    }
    "received Launch" should {
      "send status update" in {
        val probe = TestProbe()
        val publisher = TestProbe()
        val wea = TestActorRef(new WorkflowExecutorActor(
          executionContext,
          nodeExecutorFactory(),
          new ExecutionWithoutInferenceFactory(),
          None,
          Some(system.actorSelection(publisher.ref.path))))

        val nodesIds = IndexedSeq(Node.Id.randomId, Node.Id.randomId, Node.Id.randomId)
        val (graph, _) = simpleExecution(nodesIds, None)
        probe.send(wea, Launch(graph, nodesIds))
        eventually {
          val status = publisher.expectMsgClass(classOf[ExecutionStatus])
          status.executionFailure shouldBe None
          status.nodes.size shouldBe 3
          nodesIds.foreach(status.nodes(_) shouldBe a[Queued.type])
        }
      }
      "enqueue graph" in {
        val (probe, wea, execution, _, _, _, _) = readyNodesFixture()
        sendLaunch(probe, wea)
        eventually {
          verify(execution).enqueue(any())
        }
      }
      "infer and apply graph knowledge and pass it to graph" in {
        val (probe, wea, execution, _, _, _, _) = readyNodesFixture()
        sendLaunch(probe, wea)
        eventually {
          verify(execution).inferAndApplyKnowledge(executionContext.inferContext)
        }
      }
      "launch ready nodes and send status if inference ok" in {
        val (probe, wea, execution, readyNodes, executors, statusListeners, _) = readyNodesFixture()
        sendLaunch(probe, wea)
        verifyNodesStarted(execution, readyNodes, executors)
        verifyStatusSent(statusListeners)
      }

      "infer graph knowledge, fail graph and send status on exception" in {
        val probe = TestProbe()
        val publisher = TestProbe()
        val subscriber = TestProbe()
        val execution = readyExecutionWithThrowingInference()
        val wea = TestActorRef(new WorkflowExecutorActor(
          executionContext,
          nodeExecutorFactory(),
          createExecutionFactory(execution),
          Some(subscriber.ref),
          Some(system.actorSelection(publisher.ref.path))))
        sendLaunch(probe, wea)
        eventually {
          verify(execution).inferAndApplyKnowledge(executionContext.inferContext)
        }

        verifyStatus(Seq(subscriber, publisher)){ _.executionFailure.isDefined shouldBe true }
      }
    }
    "received NodeCompleted" should {
      "tell graph about it" in {
        val (probe, wea, execution, _, _, _, _) = readyNodesFixture()
        become(wea, execution)
        val (completedId, nodeCompletedMessage, results) = nodeCompleted()
        probe.send(wea, nodeCompletedMessage)
        eventually {
          verify(execution).nodeFinished(completedId, results.doperables.keys.toSeq)
        }
      }
      "launch ready nodes and send status" when {
        "ready nodes exist" in {
          val (probe, wea, execution, readyNodes, executors, statusListeners, _) =
            readyNodesFixture()
          become(wea, execution)
          val (_, nodeCompletedMessage, _) = nodeCompleted()
          probe.send(wea, nodeCompletedMessage)
          verifyNodesStarted(execution, readyNodes, executors)
          verifyStatusSent(statusListeners)
        }
      }
      "launch ready nodes and send only changed statuses" when {
        "ready nodes exist" in {
          val (probe, wea, _, _, _, statusListeners, _) = readyNodesFixture()
          val nodesIds = IndexedSeq(Node.Id.randomId, Node.Id.randomId, Node.Id.randomId)
          val (_, execution) = simpleExecution(nodesIds, Some(nodesIds.head))
          become(wea, execution)
          val (nodeCompletedMessage, entitiesMap) = createNodeCompletedMessage(nodesIds.head)
          probe.send(wea, nodeCompletedMessage)
          eventually {
            statusListeners.foreach { receiver =>
              val status = receiver.expectMsgClass(classOf[ExecutionStatus])
              status.executionFailure shouldBe None
              status.nodes.size shouldBe 2
              status.nodes(nodesIds.head) shouldBe a[Completed]
              status.nodes(nodesIds(1)) shouldBe a[Running]
              status.resultEntities shouldBe entitiesMap
            }
          }
        }
      }
      "send graph status" when {
        "there is no ready nodes" in {
          val (probe, wea, execution, _, _, statusListeners, _) = noReadyNodesFixture()
          become(wea, execution)
          val (_, nodeCompletedMessage, _) = nodeCompleted()
          probe.send(wea, nodeCompletedMessage)
          verifyStatusSent(statusListeners)
        }
      }
      "finish execution, if graph Completed/Failed" in {
        val (probe, wea, _, _, _, statusListeners, endStateSubscriber) = readyNodesFixture()
        val nodesIds = IndexedSeq(Node.Id.randomId)
        val (_, execution) = simpleExecution(nodesIds, Some(nodesIds.head))
        become(wea, execution)
        val (nodeCompletedMessage, entitiesMap) = createNodeCompletedMessage(nodesIds.head)
        probe.send(wea, nodeCompletedMessage)
        eventually {
          (statusListeners :+ endStateSubscriber).foreach { receiver =>
            val status = receiver.expectMsgClass(classOf[ExecutionStatus])
            status.executionFailure shouldBe None
            status.nodes.size shouldBe 1
            status.nodes(nodesIds.head) shouldBe a[Completed]
            status.resultEntities shouldBe entitiesMap

          }
        }
      }
    }
    "received NodeFailed" should {
      "tell graph about it" in {
        val (probe, wea, execution, _, _, statusListeners, _) = readyNodesFixture()
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
          val (probe, wea, execution, readyNodes, executors, statusListeners, _) =
            readyNodesFixture()
          become(wea, execution)
          val (_, nodeFailedMessage, _) = nodeFailed()
          probe.send(wea, nodeFailedMessage)
          verifyNodesStarted(execution, readyNodes, executors)
          verifyStatusSent(statusListeners)
        }
      }
      "send status" in {
        val (probe, wea, _, _, _, statusListeners, _) = readyNodesFixture()
        val nodesIds = IndexedSeq(Node.Id.randomId, Node.Id.randomId)
        val (_, execution) = simpleExecution(nodesIds, Some(nodesIds.head))
        become(wea, execution)
        val nodeFailedMsg =
          NodeFailed(nodesIds.head, new RuntimeException("Huston we have a problem"))
        probe.send(wea, nodeFailedMsg)
        eventually {
          statusListeners.foreach { receiver =>
            val status = receiver.expectMsgClass(classOf[ExecutionStatus])
            status.executionFailure shouldBe None
            status.nodes.size shouldBe 2
            status.nodes(nodesIds.head) shouldBe a[nodestate.Failed]
            status.nodes(nodesIds(1)) shouldBe a[Aborted.type]
          }
        }
      }
      "finish execution and persist its state if graph Completed/Failed" in {
        val (probe, wea, graph, _, _, statusListeners, endStateSubscriber) = readyNodesFixture()
        val failed = failedExecution()
        when(graph.nodeFailed(any(), any())).thenReturn(failed)
        become(wea, graph)
        val (_, nodeFailedMessage, _) = nodeFailed()
        probe.send(wea, nodeFailedMessage)

        verifyStatus(statusListeners){ _.executionFailure.isDefined shouldBe true }
        verifyStatus(Seq(endStateSubscriber)){ _.executionFailure.isDefined shouldBe true }

        probe.send(wea, Connect(Workflow.Id.randomId)) // Id doesn't matter
        eventually {
          verifyStatus(statusListeners){ _.executionFailure.isDefined shouldBe true }
        }
      }
    }
  }

  def createNodeCompletedMessage(nodeId: Id): (NodeCompleted, EntitiesMap) = {
    val eId: Entity.Id = Entity.Id.randomId
    val reports: Map[Entity.Id, ReportContent] = Map(eId -> ReportContent("t", List()))
    val dOperables: Map[Entity.Id, DOperable] = Map(eId -> mock[DOperable])
    val nodeCompletedMessage = NodeCompleted(
      nodeId,
      NodeExecutionResults(reports, dOperables))
    (nodeCompletedMessage, EntitiesMap(dOperables, reports))
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
    val execution = mock[Execution]
    when(execution.states).thenReturn(Map[Node.Id, NodeState]())

    when(execution.readyNodes).thenReturn(readyNodes)
    when(execution.nodeStarted(any())).thenReturn(execution)
    when(execution.error).thenReturn(None)
    execution
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
    when(execution.error).thenReturn(None)
    when(execution.isRunning).thenReturn(true)
    (execution, readyNodes)
  }

  def readyExecutionWithOkInference(): (Execution, Seq[ReadyNode]) = {
    val (execution, readyNodes) = readyExecution()
    executionWithOkInference(execution, readyNodes)
  }


  def noReadyExecutionWithOkInference(): Execution = {
    val graph = noReadyExecution()

    when(graph.enqueue(any())).thenReturn(graph)
    when(graph.inferAndApplyKnowledge(any())).thenReturn(graph)

    graph
  }

  def failedExecution(): Execution = {
    val execution = mock[Execution]
    when(execution.states).thenReturn(Map[Node.Id, NodeState]())
    when(execution.error).thenReturn(Some(
      FailureDescription(
        DeepSenseFailure.Id.randomId,
        FailureCode.UnexpectedError,
        "Mock title"
      )
    ))
    when(execution.readyNodes).thenReturn(Seq())
    execution
  }

  def completedExecution(): Execution = {
    val execution = mock[Execution]
    when(execution.states).thenReturn(Map[Node.Id, NodeState]())
    when(execution.readyNodes).thenReturn(Seq())
    when(execution.isRunning).thenReturn(false)
    when(execution.error).thenReturn(None)
    execution
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
    Seq[TestProbe],
    TestProbe) = {
    val testProbe = TestProbe()
    val publisher = TestProbe()
    when(execution.nodeFinished(any(), any())).thenReturn(execution)
    when(execution.nodeFailed(any(), any())).thenReturn(execution)
    val subscriber = TestProbe()
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
      Some(subscriber.ref),
      Some(system.actorSelection(publisher.ref.path))))

    (testProbe, testedActor, execution, readyNodes, executors, Seq(publisher), subscriber)
  }

  def readyNodesFixture():
      (TestProbe,
        TestActorRef[WorkflowExecutorActor],
        Execution,
        Seq[ReadyNode],
        Seq[TestProbe],
        Seq[TestProbe],
        TestProbe) = {

    val (execution, readyNodes) = readyExecutionWithOkInference()
    fixture(execution, readyNodes)
  }

  def noReadyNodesFixture():
  (TestProbe,
    TestActorRef[WorkflowExecutorActor],
    Execution,
    Seq[ReadyNode],
    Seq[TestProbe],
    Seq[TestProbe],
    TestProbe) = {
    fixture(noReadyExecutionWithOkInference(), Seq.empty)
  }

  private def simpleExecution(
      nodesIds: IndexedSeq[Node.Id],
      runningNode: Option[Node.Id]): (DirectedGraph, Execution) = {
    // A -> B -> ... -> N
    val nodes = nodesIds.map(Node(_, mockDOperation()))
    val edges = (0 until nodesIds.length - 1).map(i => Edge(nodes(i), 0, nodes(i + 1), 0))
    val time = DateTimeConverter.now.minusHours(1)
    val runningNodeIdx: Int = runningNode.map(nodesIds.indexOf(_)).getOrElse(-1)
    val completedNodes = nodesIds.take(runningNodeIdx)
      .map(_ -> Completed(time, time.plusMinutes(1), Seq()))
    val queuedNodes = nodesIds.drop(runningNodeIdx + 1).map(_ -> Queued)
    val statefulGraph = StatefulGraph(
      DirectedGraph(nodes.toSet, edges.toSet),
      (completedNodes ++ Seq(nodesIds.head -> Running(time)) ++ queuedNodes).toMap,
      None)
    (statefulGraph.directedGraph, new ExecutionWithoutInference(statefulGraph))
  }

  private def mockDOperation(): DOperation = {
    val operation = mock[DOperation]
    when(operation.inArity).thenReturn(1)
    when(operation.outArity).thenReturn(1)
    operation
  }

  private class ExecutionWithoutInferenceFactory extends ExecutionFactory {
    override def create(directedGraph: DirectedGraph): Execution =
      new ExecutionWithoutInference(StatefulGraph(directedGraph.nodes, directedGraph.edges))
    override def empty: Execution = ???
  }

  private class ExecutionWithoutInference(statefulGraph: StatefulGraph)
      extends PartialExecution(statefulGraph) {
    override def inferAndApplyKnowledge(inferContext: InferContext): Execution = this
  }
}
