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
import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest._
import org.scalatest.concurrent.{Eventually, ScalaFutures, ScaledTimeSpans}
import org.scalatest.mock.MockitoSugar

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.{CommonExecutionContext, DOperable, DOperation, ExecutionContext}
import io.deepsense.graph.Node.Id
import io.deepsense.graph._
import io.deepsense.graph.nodestate.{Aborted, Completed, NodeState, Queued, Running}
import io.deepsense.models.entities.Entity
import io.deepsense.models.workflows.{EntitiesMap, Workflow}
import io.deepsense.reportlib.model.ReportContent
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages._
import io.deepsense.workflowexecutor.WorkflowNodeExecutorActor.Messages.Start
import io.deepsense.workflowexecutor.communication.{ExecutionStatus, StatusRequest}
import io.deepsense.workflowexecutor.partialexecution.{AbortedExecution, Execution, IdleExecution, RunningExecution}

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
    "received StatusRequest" when {
      "no graph was sent" should {
        "send empty status" in {
          val probe = TestProbe()
          val publisher = TestProbe()
          val statusListener = TestProbe()
          val wea = TestActorRef(new WorkflowExecutorActor(
            mock[CommonExecutionContext],
            mock[GraphNodeExecutorFactory],
            createExecutionFactory(emptyExecution),
            Some(statusListener.ref),
            Some(system.actorSelection(publisher.ref.path))),
            Id.randomId.toString)
          probe.send(wea, StatusRequest(Workflow.Id.randomId))
          verifyStatusSent(Seq(publisher))
        }
      }
      "a graph was sent" should {
        "send whole graph's status" in {
          val ids: IndexedSeq[Id] = IndexedSeq(Node.Id.randomId, Node.Id.randomId, Node.Id.randomId)
          val (_, execution) = simpleRunningExecution(ids, Some(ids.head))
          val (probe, wea, _, _, statusListeners, _) = launchedStateFixture(execution)
          probe.send(wea, StatusRequest(Workflow.Id.randomId))
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
    "received Abort" should {
      "abort all nodes and wait for execution end" in {
        val (probe, wea, execution, _, statusListeners, _) =
          launchedStateFixture(mock[RunningExecution])
        val abortedExecution = mock[AbortedExecution]
        val nodeId = Node.Id.randomId
        when(execution.states).thenReturn(Map(
          nodeId -> nodestate.Queued
        ))

        when(execution.abort).thenReturn(abortedExecution)
        val abortedStates: Map[Id, NodeState] = Map(
          nodeId -> nodestate.Aborted
        )
        when(abortedExecution.states).thenReturn(abortedStates)
        when(abortedExecution.error).thenReturn(None)

        probe.send(wea, Abort())
        eventually {
          verify(execution).abort
        }

        eventually {
          statusListeners.foreach { receiver =>
            val status = receiver.expectMsgClass(classOf[ExecutionStatus])
            status.executionFailure shouldBe None
            status.nodes shouldBe abortedStates
            status.resultEntities shouldBe EntitiesMap()
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
          Some(system.actorSelection(publisher.ref.path))),
          Id.randomId.toString)

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
        val idleExecutionOkInference = mock[IdleExecution]
        val (runningExecution, readyNodes) = readyExecution()
        executionWithOkInference(idleExecutionOkInference, runningExecution, readyNodes)
        val (probe, wea, execution, _, _, _) = blankStateFixture(idleExecutionOkInference)
        sendLaunch(probe, wea)
        eventually {
          verify(execution).enqueue
        }
      }
      "infer and apply graph knowledge and pass it to graph" in {
        val idleExecutionOkInference = mock[IdleExecution]
        val (runningExecution, readyNodes) = readyExecution()
        executionWithOkInference(idleExecutionOkInference, runningExecution, readyNodes)
        val (probe, wea, execution, _, _, _) = blankStateFixture(idleExecutionOkInference)
        sendLaunch(probe, wea)
        eventually {
          verify(execution).inferAndApplyKnowledge(executionContext.inferContext)
        }
      }
      "launch ready nodes and send status if inference ok" in {
        val idleExecutionOkInference = mock[IdleExecution]
        val (runningExecution, readyNodes) = readyExecution()
        executionWithOkInference(idleExecutionOkInference, runningExecution, readyNodes)
        val (probe, wea, _, executors, statusListeners, _) =
          blankStateFixture(idleExecutionOkInference)
        sendLaunch(probe, wea)
        verifyNodesStarted(runningExecution, readyNodes, executors)
        verifyStatusSent(statusListeners)
      }
      "infer graph knowledge, fail graph and send status on exception" in {
        val probe = TestProbe()
        val publisher = TestProbe()
        val subscriber = TestProbe()
        val execution = executionWithThrowingInference()
        val wea = TestActorRef(new WorkflowExecutorActor(
          executionContext,
          nodeExecutorFactory(),
          createExecutionFactory(execution),
          Some(subscriber.ref),
          Some(system.actorSelection(publisher.ref.path))),
          Id.randomId.toString)
        sendLaunch(probe, wea)
        eventually {
          verify(execution).inferAndApplyKnowledge(executionContext.inferContext)
        }

        verifyStatus(Seq(subscriber, publisher)){ _.executionFailure.isDefined shouldBe true }
      }
      "after relaunch send statuses for all nodes that changed" in {
        val testProbe = TestProbe()
        val wea = TestActorRef(new WorkflowExecutorActor(
          executionContext,
          nodeExecutorFactory(),
          mock[ExecutionFactory],
          None,
          Some(system.actorSelection(testProbe.ref.path))))

        val finishedExecution: IdleExecution = mock[IdleExecution]
        becomeFinished(wea, finishedExecution)

        val id1 = Id.randomId
        val id2 = Id.randomId
        val id3 = Id.randomId
        val id4 = Id.randomId
        val id5 = Id.randomId

        def completedState(): NodeState = {
          nodestate.Completed(DateTime.now(), DateTime.now(), Seq())
        }

        val oldStates = Map(
          id1 -> completedState(),
          id2 -> nodestate.Draft,
          id3 -> nodestate.Queued
        )

        val differentExecution: IdleExecution = mock[IdleExecution]
        when(finishedExecution.updateStructure(any(), any())).thenReturn(finishedExecution)
        when(finishedExecution.error).thenReturn(None)
        when(finishedExecution.inferAndApplyKnowledge(any())).thenReturn(finishedExecution)
        when(finishedExecution.enqueue).thenReturn(differentExecution)
        when(finishedExecution.states).thenReturn(oldStates)

        val newStates = Map(
          id1 -> completedState(),
          id2 -> nodestate.Draft,
          id4 -> completedState(),
          id5 -> nodestate.Draft
        )

        when(differentExecution.states).thenReturn(newStates)

        val expectedStates = Map(
          id1 -> newStates(id1),
          id4 -> newStates(id4),
          id5 -> newStates(id5)
        )

        sendLaunch(TestProbe(), wea)
        eventually {
          val executionStatus = testProbe.expectMsgType[ExecutionStatus]
          executionStatus.nodes should contain theSameElementsAs expectedStates
        }
      }
      "after relaunch reset dOperableCache and reports only for nodes that changed" in {
        val wea = TestActorRef(new WorkflowExecutorActor(
          executionContext,
          nodeExecutorFactory(),
          mock[ExecutionFactory],
          None,
          None))

        // Simulate that an execution finished: A -> B -> C
        // Now, we relaunch B. Only A's output entity should stay in cache.
        val entityId1 = Entity.Id.randomId
        val entityId2 = Entity.Id.randomId
        val entityReport1: ReportContent = mock[ReportContent]("MockedReport")
        wea.underlyingActor.reports.put(entityId1, entityReport1)
        wea.underlyingActor.reports.put(entityId2, mock[ReportContent])
        val doperable1: DOperable = mock[DOperable]("MockedDOperable")
        wea.underlyingActor.dOperableCache.put(entityId1, doperable1)
        wea.underlyingActor.dOperableCache.put(entityId2, mock[DOperable])

        def nodeCompleted(entity: Entity.Id): NodeState = {
          nodestate.Completed(DateTime.now(), DateTime.now(), Seq(entity))
        }

        val finishedExecution = mock[IdleExecution]
        val states = Map(
          Node.Id.randomId -> nodeCompleted(entityId1),
          Node.Id.randomId -> nodestate.Draft,
          Node.Id.randomId -> nodestate.Draft
        )

        val enqueued = mock[RunningExecution]
        when(enqueued.readyNodes).thenReturn(Seq())
        when(finishedExecution.states).thenReturn(states)
        when(finishedExecution.error).thenReturn(None)
        when(finishedExecution.updateStructure(any(), any())).thenReturn(finishedExecution)
        when(finishedExecution.inferAndApplyKnowledge(any())).thenReturn(finishedExecution)
        when(finishedExecution.enqueue).thenReturn(enqueued)
        when(enqueued.states).thenReturn(states)
        becomeFinished(wea, finishedExecution)

        sendLaunch(TestProbe(), wea)

        eventually {
          wea.underlyingActor.dOperableCache should
            contain theSameElementsAs Map(entityId1 -> doperable1)
          wea.underlyingActor.reports should
            contain theSameElementsAs Map(entityId1 -> entityReport1)
        }
      }
    }
    "received NodeCompleted" should {
      "tell graph about it" in {
        val (probe, wea, execution, _, _, _) = launchedStateFixture(mock[RunningExecution])
        val (completedId, nodeCompletedMessage, results) = nodeCompleted()
        probe.send(wea, nodeCompletedMessage)
        eventually {
          verify(execution).nodeFinished(completedId, results.doperables.keys.toSeq)
        }
      }
      "launch ready nodes and send status" when {
        "ready nodes exist" in {
          val (runningExecution, readyNodes) = readyExecution()
          when(runningExecution.nodeFinished(any(), any())).thenReturn(runningExecution)
          val (probe, wea, execution, executors, statusListeners, _) =
            launchedStateFixture(runningExecution)
          val (_, nodeCompletedMessage, _) = nodeCompleted()
          probe.send(wea, nodeCompletedMessage)
          verifyNodesStarted(execution, readyNodes, executors)
          verifyStatusSent(statusListeners)
        }
      }
      "launch ready nodes and send only changed statuses" when {
        "ready nodes exist" in {
          val nodesIds = IndexedSeq(Node.Id.randomId, Node.Id.randomId, Node.Id.randomId)
          val (_, execution) = simpleRunningExecution(nodesIds, Some(nodesIds.head))
          val (probe, wea, _, _, statusListeners, _) = launchedStateFixture(execution)
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
          val readyExecution = noReadyExecution()
          when(readyExecution.nodeFinished(any(), any())).thenReturn(readyExecution)
          val (probe, wea, _, _, statusListeners, _) =
            launchedStateFixture(readyExecution)
          val (_, nodeCompletedMessage, _) = nodeCompleted()
          probe.send(wea, nodeCompletedMessage)
          verifyStatusSent(statusListeners)
        }
      }
      "finish execution, if graph Completed/Failed" in {
        val nodesIds = IndexedSeq(Node.Id.randomId)
        val (_, execution) = simpleRunningExecution(nodesIds, Some(nodesIds.head))
        val (probe, wea, _, _, statusListeners, endStateSubscriber) =
          launchedStateFixture(execution)
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
        val runningExecution: RunningExecution = mock[RunningExecution]
        val executionResult = failedExecution()
        when(runningExecution.nodeFailed(any(), any())).thenReturn(executionResult)
        val (probe, wea, execution, _, statusListeners, _) =
          launchedStateFixture(runningExecution)
        val (completedId, nodeFailedMessage, cause) = nodeFailed()
        probe.send(wea, nodeFailedMessage)
        eventually {
          verify(runningExecution).nodeFailed(completedId, cause)
        }
        verifyStatusSent(statusListeners)
      }
      "launch ready nodes" when {
        "ready nodes exist" in {
          val (runningExecution, readyNodes) = readyExecution()
          when(runningExecution.nodeFailed(any(), any())).thenReturn(runningExecution)
          val (probe, wea, execution, executors, statusListeners, _) =
            launchedStateFixture(runningExecution)
          val (_, nodeFailedMessage, _) = nodeFailed()
          probe.send(wea, nodeFailedMessage)
          verifyNodesStarted(execution, readyNodes, executors)
          verifyStatusSent(statusListeners)
        }
      }
      "send status" in {
        val nodesIds = IndexedSeq(Node.Id.randomId, Node.Id.randomId)
        val (_, execution) = simpleRunningExecution(nodesIds, Some(nodesIds.head))
        val (probe, wea, _, _, statusListeners, _) = launchedStateFixture(execution)
        becomeLaunched(wea, execution)
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
        val (runningExecution, _) = readyExecution()
        val failed = failedExecution()
        val (probe, wea, execution, _, statusListeners, endStateSubscriber) =
          launchedStateFixture(runningExecution)
        when(execution.nodeFailed(any(), any())).thenReturn(failed)
        val (_, nodeFailedMessage, _) = nodeFailed()
        probe.send(wea, nodeFailedMessage)

        verifyStatus(statusListeners){ _.executionFailure.isDefined shouldBe true }
        verifyStatus(Seq(endStateSubscriber)){ _.executionFailure.isDefined shouldBe true }

        probe.send(wea, StatusRequest(Workflow.Id.randomId)) // Id doesn't matter
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

  def emptyExecution: IdleExecution = Execution.empty

  def createExecutionFactory(execution: IdleExecution): ExecutionFactory = {
    val factory = mock[ExecutionFactory]
    when(factory.create(any(), any())).thenReturn(execution)
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

  def becomeLaunched(
      wea: TestActorRef[WorkflowExecutorActor],
      execution: RunningExecution): Unit = {
    wea.underlyingActor.context
      .become(wea.underlyingActor.launched(execution))
  }

  def becomeFinished(
      wea: TestActorRef[WorkflowExecutorActor],
      execution: IdleExecution): Unit = {
    wea.underlyingActor.context
      .become(wea.underlyingActor.finished(execution))
  }

  val executionContext = {
    val context = mock[CommonExecutionContext]
    when(context.createExecutionContext(any(), any())).thenReturn(mock[ExecutionContext])
    context
  }

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

  def runningExecutionWithReadyNodes(readyNodes: Seq[ReadyNode]): RunningExecution = {
    val execution = mock[RunningExecution]
    when(execution.states).thenReturn(Map[Node.Id, NodeState]())
    when(execution.readyNodes).thenReturn(readyNodes)
    when(execution.nodeStarted(any())).thenReturn(execution)
    when(execution.error).thenReturn(None)
    execution
  }

  def readyExecution(): (RunningExecution, Seq[ReadyNode]) = {
    val readyNodes = Seq(mockReadyNode(), mockReadyNode())
    (runningExecutionWithReadyNodes(readyNodes), readyNodes)
  }

  def noReadyExecution(): RunningExecution =
    runningExecutionWithReadyNodes(Seq.empty)

  def executionWithOkInference(
      idleExecution: IdleExecution,
      enqueueResult: RunningExecution,
      readyNodes: Seq[ReadyNode]): (IdleExecution, Seq[ReadyNode]) = {
    when(idleExecution.enqueue).thenReturn(enqueueResult)
    when(idleExecution.inferAndApplyKnowledge(any())).thenReturn(idleExecution)
    when(idleExecution.error).thenReturn(None)
    when(idleExecution.isRunning).thenReturn(false)
    (idleExecution, readyNodes)
  }

  def failedExecution(): IdleExecution = {
    val execution = mock[IdleExecution]
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

  def executionWithThrowingInference(): IdleExecution = {
    val graph = mock[IdleExecution]
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

  def fixture(
      execution: Execution,
      executionFactory: => ExecutionFactory):
    (TestProbe,
      TestActorRef[WorkflowExecutorActor],
      Execution,
      Seq[TestProbe],
      Seq[TestProbe],
      TestProbe) = {

    val probe = TestProbe()
    val publisher = TestProbe()
    val subscriber = TestProbe()
    val executors = Seq.fill(2)(TestProbe())

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

    val wea = TestActorRef(new WorkflowExecutorActor(
      executionContext,
      nodeExecutorFactory,
      executionFactory,
      Some(subscriber.ref),
      Some(system.actorSelection(publisher.ref.path))),
      Id.randomId.toString)

    val statusListeners = Seq(publisher)
    (probe, wea, execution, executors, statusListeners, subscriber)
  }

  def blankStateFixture(idleExecution: IdleExecution):
    (TestProbe,
      TestActorRef[WorkflowExecutorActor],
      Execution,
      Seq[TestProbe],
      Seq[TestProbe],
      TestProbe) = {
    fixture(idleExecution, createExecutionFactory(idleExecution))
  }

  def launchedStateFixture(runningExecution: RunningExecution):
    (TestProbe,
      TestActorRef[WorkflowExecutorActor],
      Execution,
      Seq[TestProbe],
      Seq[TestProbe],
      TestProbe) = {
    val (probe, wea, execution, executors, statusListeners, subscriber) =
      fixture(runningExecution, mock[ExecutionFactory])
    becomeLaunched(wea, runningExecution)
    (probe, wea, execution, executors, statusListeners, subscriber)
  }

  private def linearGraph(nodesIds: IndexedSeq[Id], runningNode: Option[Id]): StatefulGraph = {
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
    statefulGraph
  }

  private def simpleExecution(
    nodesIds: IndexedSeq[Node.Id],
    runningNode: Option[Node.Id]): (DirectedGraph, Execution) = {
    val statefulGraph: StatefulGraph = linearGraph(nodesIds, runningNode)
    val trueIdleExecution = IdleExecution(statefulGraph, nodesIds.toSet)
    val idleExecution = Mockito.spy(trueIdleExecution)
    when(idleExecution.inferAndApplyKnowledge(any())).thenReturn(idleExecution)
    (statefulGraph.directedGraph, idleExecution)
  }

  private def simpleRunningExecution(
    nodesIds: IndexedSeq[Node.Id],
    runningNode: Option[Node.Id]): (DirectedGraph, RunningExecution) = {
    val statefulGraph: StatefulGraph = linearGraph(nodesIds, runningNode)
    (statefulGraph.directedGraph, RunningExecution(statefulGraph, statefulGraph, nodesIds.toSet))
  }

  private def mockDOperation(): DOperation = {
    val operation = mock[DOperation]
    when(operation.inArity).thenReturn(1)
    when(operation.outArity).thenReturn(1)
    operation
  }

  private class ExecutionWithoutInferenceFactory extends ExecutionFactory {
    override def create(directedGraph: DirectedGraph, nodes: Seq[Node.Id]): IdleExecution = {
      val trueIdleExecution = Execution(directedGraph, nodes)
      val idleExecution = Mockito.spy(trueIdleExecution)
      when(idleExecution.inferAndApplyKnowledge(any())).thenReturn(idleExecution)
      idleExecution
    }
    override def empty: Execution = ???
  }
}
