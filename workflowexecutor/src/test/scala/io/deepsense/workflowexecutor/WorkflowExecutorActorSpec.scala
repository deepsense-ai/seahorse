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

import akka.actor.{Actor, ActorRef, ActorSystem}
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
import io.deepsense.commons.models.Entity
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.inout._
import io.deepsense.deeplang.doperations.{ReadDataFrame, WriteDataFrame}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.params.{FileFormat, StorageType}
import io.deepsense.deeplang.{CommonExecutionContext, DOperable, DOperation, ExecutionContext}
import io.deepsense.graph.DeeplangGraph.DeeplangNode
import io.deepsense.graph.Node.Id
import io.deepsense.graph._
import io.deepsense.graph.nodestate.{Aborted, NodeStatus, Queued, Running}
import io.deepsense.models.workflows.{EntitiesMap, Workflow, _}
import io.deepsense.reportlib.model.ReportContent
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages._
import io.deepsense.workflowexecutor.WorkflowManagerClientActorProtocol.{GetWorkflow, SaveState, SaveWorkflow}
import io.deepsense.workflowexecutor.WorkflowNodeExecutorActor.Messages.Start
import io.deepsense.workflowexecutor.communication.message.workflow.ExecutionStatus
import io.deepsense.workflowexecutor.executor.Executor
import io.deepsense.workflowexecutor.partialexecution._

class WorkflowExecutorActorSpec
  extends TestKit(ActorSystem("WorkflowExecutorActorSpec"))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with MockitoSugar
  with Eventually
  with ScalaFutures
  with ScaledTimeSpans
  with Logging {

  "SessionWorkflowExecutorActor" when {
    "received Init" should {
        "send workflow with results" in {
          val workflow = workflowWithResults(Workflow.Id.randomId)
          val (probe, wea, _, statusListeners, _, _) =
            fixture(workflow, useWorkflowManagerProbe = false)

          probe.send(wea, WorkflowExecutorActor.Messages.Init())

          eventually {
            statusListeners.foreach { receiver =>
              val results = receiver.expectMsgClass(classOf[WorkflowWithResults])
              results.executionReport shouldBe workflow.executionReport
              results.graph shouldBe workflow.graph
              results.id shouldBe workflow.id
              results.metadata shouldBe workflow.metadata
              results.thirdPartyData shouldBe workflow.thirdPartyData

              val inferredState = receiver.expectMsgClass(classOf[InferredState])
              inferredState.id shouldBe workflow.id
              inferredState.states shouldBe workflow.executionReport
            }
          }
        }
    }
    "received Abort" should {
      "abort all nodes and wait for execution end" in {
        val workflow = workflowWithResults(Workflow.Id.randomId)
        val (probe, wea, _, statusListeners, _, testWMClientProbe) = launchedStateFixture(workflow)
        val abortedStatuses: Map[Node.Id, NodeStatus] =
          workflow.graph.nodes.map(_.id -> Aborted()).toMap

        probe.send(wea, Abort())

        eventually {
          statusListeners.foreach { receiver =>
            val status = receiver.expectMsgClass(classOf[ExecutionStatus])
            status.executionReport.error shouldBe None
            status.executionReport.nodesStatuses shouldBe abortedStatuses
            status.executionReport.resultEntities shouldBe EntitiesMap()

            val execution = wea.underlyingActor.execution
            execution match {
              case e: AbortedExecution => ()
              case e => fail(s"Expected AbortedExecution, got: ${e.toString}")
            }
          }
        }
        eventually {
          val saveState = testWMClientProbe.expectMsgClass(classOf[SaveState])
          saveState.workflowId shouldBe workflow.id
          saveState.state.error shouldBe None
          saveState.state.nodesStatuses shouldBe abortedStatuses
          saveState.state.resultEntities shouldBe EntitiesMap()
        }
      }
    }
    "received Launch" should {
      "send status update" in {
        val workflowId = Workflow.Id.randomId
        val workflow = workflowWithResults(workflowId)
        val (probe, wea, _, statusListeners, _, testWMClientProbe) =
          initializedStateFixture(workflow)
        val nodesIds = workflow.graph.nodes.map(_.id)

        probe.send(wea, Launch(nodesIds))

        eventually {
          statusListeners.foreach { receiver =>
            val status = receiver.expectMsgClass(classOf[ExecutionStatus])
            status.executionReport.error shouldBe None
            status.executionReport.nodesStatuses.size shouldBe 2
            status.executionReport.nodesStatuses(node2.id) shouldBe a[Queued]
            status.executionReport.nodesStatuses(node1.id) shouldBe a[Running]
          }
        }
        eventually {
          val saveState = testWMClientProbe.expectMsgClass(classOf[SaveState])
          saveState.workflowId shouldBe workflow.id
          saveState.state.error shouldBe None
          saveState.state.nodesStatuses.size shouldBe 2
          saveState.state.nodesStatuses(node2.id) shouldBe a[Queued]
          saveState.state.nodesStatuses(node1.id) shouldBe a[Running]
        }
      }
      "infer graph knowledge, fail graph and send status on exception" in {
        val workflow = workflowInvalidInference(Workflow.Id.randomId)
        val (probe, wea, _, statusListeners, publisher, testWMClientProbe) =
          initializedStateFixture(workflow)

        sendLaunch(probe, wea, workflow.graph.nodes.map(_.id))

        verifyStatus(statusListeners) { executionStatus =>
          executionStatus.executionReport.error.isDefined shouldBe true
        }
        eventually {
          val saveState = testWMClientProbe.expectMsgClass(classOf[SaveState])
          saveState.workflowId shouldBe workflow.id
          saveState.state.error.isDefined shouldBe true
        }
      }
      "after relaunch handleonly changed nodes" in {
        val workflow = workflowInvalidInference(Workflow.Id.randomId)
        val (probe, wea, _, statusListeners, publisher, testWMClientProbe) =
          finishedStateFixture(workflow)

        sendLaunch(TestProbe(), wea, Set(node2.id))

        eventually {
          statusListeners.foreach { receiver =>
            val executionStatus = receiver.expectMsgType[ExecutionStatus]
            logger.info("status: " + executionStatus)
            executionStatus.executionReport.nodesStatuses.size shouldBe 1
            executionStatus.executionReport.nodesStatuses(node2.id) shouldBe a[nodestate.Running]
          }
          wea.underlyingActor.execution.states(node1.id).nodeState.nodeStatus shouldBe
            a[nodestate.Completed]
        }
        eventually {
          val saveState = testWMClientProbe.expectMsgClass(classOf[SaveState])
          saveState.workflowId shouldBe workflow.id
          saveState.state.nodesStatuses.size shouldBe 1
          saveState.state.nodesStatuses(node2.id) shouldBe a[nodestate.Running]
        }
      }
    }
    "received NodeCompleted" should {
      "tell graph about it" in {
        val statefulWorkflow: StatefulWorkflow = mock[StatefulWorkflow]
        val workflow = workflowWithResults(Workflow.Id.randomId)
        val states: Map[Id, NodeStateWithResults] =
          Map(node1.id -> nodeState(nodestate.Draft()), node2.id -> nodeState(nodestate.Draft()))
        when(statefulWorkflow.changesExecutionReport(any())).thenReturn(
          ExecutionReport(states.mapValues(_.nodeState)))
        when(statefulWorkflow.currentExecution).thenReturn(
          Execution(
            StatefulGraph(
              workflow.graph,
              states,
              None)))
        val (probe, wea, _, statusListeners, _, testWMClientProbe) = launchedStateFixture(workflow)
        wea.underlyingActor.statefulWorkflow = statefulWorkflow

        val (completedId, nodeCompletedMessage, _) = nodeCompleted()

        probe.send(wea, nodeCompletedMessage)

        eventually {
          statusListeners.foreach { receiver =>
            val stat = receiver.expectMsgClass(classOf[ExecutionStatus])
          }
          verify(statefulWorkflow).nodeFinished(
            completedId,
            nodeCompletedMessage.results.entitiesId,
            nodeCompletedMessage.results.reports,
            nodeCompletedMessage.results.doperables)
        }
        eventually {
          val saveState = testWMClientProbe.expectMsgClass(classOf[SaveState])
          saveState.workflowId shouldBe workflow.id
        }
      }
      "launch ready nodes" when {
        "ready nodes exist" in {
          val statefulWorkflow: StatefulWorkflow = mock[StatefulWorkflow]
          val workflow = workflowWithResults(Workflow.Id.randomId)
          val states: Map[Id, NodeStateWithResults] =
            Map(node1.id -> nodeState(nodestate.Draft()), node2.id -> nodeState(nodestate.Draft()))
          when(statefulWorkflow.changesExecutionReport(any())).thenReturn(
            ExecutionReport(states.mapValues(_.nodeState)))
          when(statefulWorkflow.currentExecution).thenReturn(
            RunningExecution(
              StatefulGraph(
                workflow.graph,
                states,
                None),
              StatefulGraph(
                DeeplangGraph(Set(node1), Set()),
                Map(node1.id -> NodeStateWithResults(
                  NodeState(nodestate.Running(DateTimeConverter.now), Some(EntitiesMap())),
                  Map())),
                None),
              Set(node1.id)))
          when(statefulWorkflow.startReadyNodes()).thenReturn(Seq.empty)
          val (probe, wea, _, statusListeners, _, testWMClientProbe) =
            launchedStateFixture(workflow)
          wea.underlyingActor.statefulWorkflow = statefulWorkflow

          val (completedId, nodeCompletedMessage, _) = nodeCompleted()

          probe.send(wea, nodeCompletedMessage)

          eventually {
            verify(statefulWorkflow).startReadyNodes()
          }
          eventually {
            val saveState = testWMClientProbe.expectMsgClass(classOf[SaveState])
            saveState.workflowId shouldBe workflow.id
          }
        }
      }
    }
    "received NodeFailed" should {
      "tell graph about it" in {
        val statefulWorkflow: StatefulWorkflow = mock[StatefulWorkflow]
        val workflow = workflowWithResults(Workflow.Id.randomId)
        val states: Map[Id, NodeStateWithResults] =
          Map(node1.id -> nodeState(nodestate.Draft()), node2.id -> nodeState(nodestate.Draft()))
        when(statefulWorkflow.changesExecutionReport(any())).thenReturn(
          ExecutionReport(states.mapValues(_.nodeState)))
        when(statefulWorkflow.currentExecution).thenReturn(
          Execution(
            StatefulGraph(
              workflow.graph,
              states,
              None)))
        val (probe, wea, _, statusListeners, _, testWMClientProbe) = launchedStateFixture(workflow)
        wea.underlyingActor.statefulWorkflow = statefulWorkflow

        val (completedId, nodeFailedMessage, cause) = nodeFailed()
        probe.send(wea, nodeFailedMessage)

        eventually {
          statusListeners.foreach { receiver =>
            receiver.expectMsgClass(classOf[ExecutionStatus])
          }
          verify(statefulWorkflow).nodeFailed(completedId, cause)
        }
        eventually {
          val saveState = testWMClientProbe.expectMsgClass(classOf[SaveState])
          saveState.workflowId shouldBe workflow.id
        }
      }
    }
    "receive StructUpdate" should {
      "update state, send update to WM and to editor" in {
        val statefulWorkflow: StatefulWorkflow = mock[StatefulWorkflow]
        val wmClient = TestProbe()
        val publisher = TestProbe()
        val probe = TestProbe()
        val wea: TestActorRef[WorkflowExecutorActor] = TestActorRef(
          SessionWorkflowExecutorActor.props(
            mock[CommonExecutionContext],
            wmClient.ref,
            system.actorSelection(publisher.ref.path),
            3), Workflow.Id.randomId.toString)
        wea.underlyingActor.statefulWorkflow = statefulWorkflow
        wea.underlyingActor.context.become(wea.underlyingActor.ready())
        val workflow: Workflow = mock[Workflow]
        val workflowWithResults: WorkflowWithResults = mock[WorkflowWithResults]
        val inferredState = mock[InferredState]
        when(statefulWorkflow.updateStructure(workflow)).thenReturn(inferredState)
        when(statefulWorkflow.workflowWithResults).thenReturn(workflowWithResults)

        probe.send(wea, UpdateStruct(workflow))

        eventually {
          verify(statefulWorkflow).updateStructure(workflow)
          verify(statefulWorkflow).workflowWithResults

          val saveWorkflow = wmClient.expectMsgClass(classOf[SaveWorkflow])
          saveWorkflow shouldBe SaveWorkflow(workflowWithResults)

          val inferred = publisher.expectMsgClass(classOf[InferredState])
          inferred shouldBe inferredState
        }
      }
    }
  }

  private def nodeState(status: NodeStatus): NodeStateWithResults = {
    NodeStateWithResults(NodeState(status, Some(EntitiesMap())), Map())
  }

  def createNodeCompletedMessage(nodeId: Id): (NodeCompleted, EntitiesMap) = {
    val eId: Entity.Id = Entity.Id.randomId
    val reports: Map[Entity.Id, ReportContent] = Map(eId -> ReportContent("t", List()))
    val dOperables: Map[Entity.Id, DOperable] = Map(eId -> mock[DOperable])
    val nodeCompletedMessage = NodeCompleted(
      nodeId,
      NodeExecutionResults(Seq(eId), reports, dOperables))
    (nodeCompletedMessage, EntitiesMap(dOperables, reports))
  }

  def sendLaunch(
      probe: TestProbe,
      wea: TestActorRef[WorkflowExecutorActor],
      nodes: Set[Node.Id]): Unit = {
    probe.send(wea, Launch(nodes))
  }

  def emptyExecution: IdleExecution = Execution.empty

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
      workflow: WorkflowWithResults): Unit = {
    val statefulWorkflow = StatefulWorkflow(
      wea.underlyingActor.executionContext,
      workflow,
      Execution.defaultExecutionFactory)
    statefulWorkflow.launch(workflow.graph.nodes.map(_.id))
    wea.underlyingActor.statefulWorkflow = statefulWorkflow
    wea.underlyingActor.context.become(wea.underlyingActor.launched())
  }

  def becomeFinished(
      wea: TestActorRef[WorkflowExecutorActor],
      workflow: WorkflowWithResults): Unit = {
    val statefulWorkflow = new StatefulWorkflow(
      wea.underlyingActor.executionContext,
      workflow.id,
      workflow.metadata,
      workflow.thirdPartyData,
      Execution(StatefulGraph(
        workflow.graph,
        workflow.graph.nodes.map(_.id -> completedState()).toMap,
        None))
    )
    wea.underlyingActor.statefulWorkflow = statefulWorkflow
    wea.underlyingActor.context.become(wea.underlyingActor.ready())
  }

  val executionContext = {
    val context = mock[CommonExecutionContext]
    when(context.createExecutionContext(any(), any())).thenReturn(mock[ExecutionContext])
    context
  }

  def verifyNodesStarted(
      wea: WorkflowExecutorActor,
      readyNodes: Seq[ReadyNode],
      executors: Seq[TestProbe]): Unit = {
    eventually {
      val execution = wea.execution
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
    NodeExecutionResults(ids, reports, operables)
  }

  def mockReadyNode(): ReadyNode = {
    val node = mock[DeeplangNode]
    when(node.id).thenReturn(Node.Id.randomId)
    val dOperation = mock[DOperation]
    when(dOperation.name).thenReturn("mockedName")
    when(node.value).thenReturn(dOperation)
    ReadyNode(node, Seq())
  }

  def nodeCompleted(): (Id, NodeCompleted, NodeExecutionResults) = {
    val completedId = node1.id
    val results = mockResults(2)
    (completedId, NodeCompleted(completedId, results), results)
  }

  def nodeFailed(): (Node.Id, NodeFailed, Exception) = {
    val failedId = node1.id
    val cause = new IllegalStateException("A node failed because a test told him to do so!")
    (failedId, NodeFailed(failedId, cause), cause)
  }

  def runningExecutionWithReadyNodes(readyNodes: Seq[ReadyNode]): RunningExecution = {
    val execution = mock[RunningExecution]
    when(execution.states).thenReturn(Map[Node.Id, NodeStateWithResults]())
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
    when(execution.states).thenReturn(Map[Node.Id, NodeStateWithResults]())
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
    when(execution.states).thenReturn(Map[Node.Id, NodeStateWithResults]())
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

  def fixture(workflow: WorkflowWithResults, useWorkflowManagerProbe: Boolean = true):
    (TestProbe,
      TestActorRef[WorkflowExecutorActor],
      Seq[TestProbe],
      Seq[TestProbe],
      TestProbe,
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

    val testWMClientProbe = TestProbe()
    val wmClient =
      if (useWorkflowManagerProbe) {
        testWMClientProbe.ref
      } else {
        wmClientActor(Map[Workflow.Id, WorkflowWithResults](workflow.id -> workflow))
      }
    val commonExecutionContext = mock[CommonExecutionContext]
    val inferContext =
      InferContext(null, null, Executor.createDOperableCatalog(), fullInference = true)
    when(commonExecutionContext.inferContext).thenReturn(inferContext)
    val wea: TestActorRef[WorkflowExecutorActor] = TestActorRef(
      new SessionWorkflowExecutorActor(
        commonExecutionContext,
        nodeExecutorFactory,
        wmClient,
        system.actorSelection(publisher.ref.path),
        5), workflow.id.toString)

    val statusListeners = Seq(publisher)
    (probe, wea, executors, statusListeners, subscriber, testWMClientProbe)
  }

  def blankStateFixture(workflow: WorkflowWithResults):
    (TestProbe,
      TestActorRef[WorkflowExecutorActor],
      Seq[TestProbe],
      Seq[TestProbe],
      TestProbe,
      TestProbe) = {
    fixture(workflow)
  }

  def initializedStateFixture(workflow: WorkflowWithResults):
    (TestProbe,
      TestActorRef[WorkflowExecutorActor],
      Seq[TestProbe],
      Seq[TestProbe],
      TestProbe,
      TestProbe) = {
    val (probe, wea, executors, statusListeners, subscriber, testWMClientProbe) = fixture(workflow)
    wea.underlyingActor.initWithWorkflow(workflow)
    (probe, wea, executors, statusListeners, subscriber, testWMClientProbe)
  }

  def launchedStateFixture(workflow: WorkflowWithResults):
    (TestProbe,
      TestActorRef[WorkflowExecutorActor],
      Seq[TestProbe],
      Seq[TestProbe],
      TestProbe,
      TestProbe) = {
    val (probe, wea, executors, statusListeners, subscriber, testWMClientProbe) = fixture(workflow)
    becomeLaunched(wea, workflow)
    (probe, wea, executors, statusListeners, subscriber, testWMClientProbe)
  }

  def finishedStateFixture(workflow: WorkflowWithResults):
    (TestProbe,
      TestActorRef[WorkflowExecutorActor],
      Seq[TestProbe],
      Seq[TestProbe],
      TestProbe,
      TestProbe) = {
    val (probe, wea, executors, statusListeners, subscriber, testWMClientProbe) = fixture(workflow)
    becomeFinished(wea, workflow)
    (probe, wea, executors, statusListeners, subscriber, testWMClientProbe)
  }

  private def linearGraph(nodesIds: IndexedSeq[Id], runningNode: Option[Id]): StatefulGraph = {
    // A -> B -> ... -> N
    val nodes = nodesIds.map(Node(_, mockDOperation()))
    val edges = (0 until nodesIds.length - 1).map(i => Edge(nodes(i), 0, nodes(i + 1), 0))
    val time = DateTimeConverter.now.minusHours(1)
    val runningNodeIdx: Int = runningNode.map(nodesIds.indexOf(_)).getOrElse(-1)
    val completedNodes = nodesIds.take(runningNodeIdx)
      .map(_ -> completedState())
    val queuedNodes = nodesIds.drop(runningNodeIdx + 1).map(_ -> nodeState(Queued()))
    val statefulGraph = StatefulGraph(
      DeeplangGraph(nodes.toSet, edges.toSet),
      (completedNodes ++ Seq(nodesIds.head -> nodeState(Running(time))) ++ queuedNodes).toMap,
      None)
    statefulGraph
  }

  private def simpleRunningExecution(
    nodesIds: IndexedSeq[Node.Id],
    runningNode: Option[Node.Id]): (DeeplangGraph, RunningExecution) = {
    val statefulGraph: StatefulGraph = linearGraph(nodesIds, runningNode)
    (statefulGraph.directedGraph, RunningExecution(statefulGraph, statefulGraph, nodesIds.toSet))
  }

  private def mockDOperation(): DOperation = {
    val operation = mock[DOperation]
    when(operation.inArity).thenReturn(1)
    when(operation.outArity).thenReturn(1)
    operation
  }

  private def completedState(): NodeStateWithResults = {
    val entityId: Entity.Id = Entity.Id.randomId
    val now = DateTimeConverter.now
    val dOperables: Map[Entity.Id, DataFrame] = Map(entityId -> new DataFrame())
    NodeStateWithResults(
      NodeState(
        nodestate.Completed(now.minusHours(1), now, Seq(entityId)),
        Some(EntitiesMap(dOperables, Map(entityId -> ReportContent("test"))))),
      dOperables)
  }

  def wmClientActor(
      workflows: Map[Workflow.Id, WorkflowWithResults]): ActorRef = {
    TestActorRef(new TestWMClientActor(workflows))
  }

  val inputFile = InputStorageTypeChoice.File()
    .setFileFormat(InputFileFormatChoice.Json())
    .setSourceFile("/whatever")
  val outputFile = OutputStorageTypeChoice.File()
    .setFileFormat(OutputFileFormatChoice.Json())
    .setOutputFile("/output")
  val node1 = Node(
    Node.Id.randomId,
    ReadDataFrame().setStorageType(inputFile))
  val node2 = Node(
    Node.Id.randomId,
    WriteDataFrame().setStorageType(outputFile))
  val invalidNode = Node(Node.Id.randomId, new WriteDataFrame())
  def workflowWithResults(id: Workflow.Id): WorkflowWithResults = WorkflowWithResults(
    id,
    WorkflowMetadata(WorkflowType.Batch, "1.0.0"),
    DeeplangGraph(
      Set(node1, node2),
      Set(Edge(node1, 0, node2, 0))),
    ThirdPartyData(),
    ExecutionReport(Map(node1.id -> NodeState.draft, node2.id -> NodeState.draft))
  )
  def workflowInvalidInference(workflowId: Workflow.Id): WorkflowWithResults =
    workflowWithResults(workflowId).copy(
      id = workflowId,
      graph = workflowWithResults(workflowId).graph.copy(nodes = Set(node1, node2, invalidNode)),
      executionReport = ExecutionReport(
        Map(
          node1.id -> NodeState.draft,
          node2.id -> NodeState.draft,
          invalidNode.id -> NodeState.draft)))

  class TestWMClientActor(workflows: Map[Workflow.Id, WorkflowWithResults])
      extends Actor
      with Logging {
    override def receive: Receive = {
      case GetWorkflow(id) =>
        logger.info(workflows.get(id).toString)
        sender() ! workflows.get(id)
    }
  }
}
