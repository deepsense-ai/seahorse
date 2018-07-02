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

package ai.deepsense.workflowexecutor

import scala.concurrent.duration._

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest._
import org.scalatest.concurrent.{Eventually, ScalaFutures, ScaledTimeSpans}
import org.scalatest.mockito.MockitoSugar
import spray.json.JsObject
import ai.deepsense.commons.datetime.DateTimeConverter
import ai.deepsense.commons.models.Entity
import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang._
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.inout._
import ai.deepsense.deeplang.doperations.readwritedataframe.{FilePath, FileScheme}
import ai.deepsense.deeplang.doperations.{ReadDataFrame, WriteDataFrame}
import ai.deepsense.graph.DeeplangGraph.DeeplangNode
import ai.deepsense.graph.Node.Id
import ai.deepsense.graph._
import ai.deepsense.graph.nodestate.{Aborted, NodeStatus, Queued, Running}
import ai.deepsense.models.workflows.{EntitiesMap, Workflow, _}
import ai.deepsense.reportlib.model.ReportContent
import ai.deepsense.reportlib.model.factory.ReportContentTestFactory
import ai.deepsense.workflowexecutor.WorkflowExecutorActor.Messages._
import ai.deepsense.workflowexecutor.WorkflowManagerClientActorProtocol.{GetWorkflow, SaveState, SaveWorkflow}
import ai.deepsense.workflowexecutor.WorkflowNodeExecutorActor.Messages.Delete
import ai.deepsense.workflowexecutor.partialexecution._
import org.apache.spark.SparkContext

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
              val inferredState = receiver.expectMsgClass(classOf[InferredState])
              inferredState.id shouldBe workflow.id
              inferredState.states shouldBe workflow.executionReport.statesOnly
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
            val status = receiver.expectMsgClass(classOf[ExecutionReport])
            status.error shouldBe None
            status.nodesStatuses shouldBe abortedStatuses
            status.resultEntities shouldBe EntitiesMap()

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
            val status = receiver.expectMsgClass(classOf[ExecutionReport])
            status.error shouldBe None
            status.nodesStatuses.size shouldBe 2
            status.nodesStatuses(node2.id) shouldBe a[Queued]
            status.nodesStatuses(node1.id) shouldBe a[Running]
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
        val (probe, wea, _, statusListeners, _, testWMClientProbe) =
          initializedStateFixture(workflow)

        sendLaunch(probe, wea, workflow.graph.nodes.map(_.id))

        verifyStatus(statusListeners) { executionStatus =>
          executionStatus.error.isDefined shouldBe true
        }
        eventually {
          val saveState = testWMClientProbe.expectMsgClass(classOf[SaveState])
          saveState.workflowId shouldBe workflow.id
          saveState.state.error.isDefined shouldBe true
        }
      }
      "after relaunch handle only changed nodes" in {
        val workflow = workflowInvalidInference(Workflow.Id.randomId)
        val (_, wea, _, statusListeners, _, testWMClientProbe) =
          finishedStateFixture(workflow)

        sendLaunch(TestProbe(), wea, Set(node2.id))

        eventually {
          statusListeners.foreach { receiver =>
            val executionStatus = receiver.expectMsgType[ExecutionReport]
            logger.info("status: " + executionStatus)
            executionStatus.nodesStatuses.size shouldBe 1
            executionStatus.nodesStatuses(node2.id) shouldBe a[nodestate.Running]
          }
          wea.underlyingActor.execution.graph.states(node1.id).nodeState.nodeStatus shouldBe
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
            receiver.expectMsgClass(classOf[ExecutionReport])
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
                  Map(),
                  None)),
                None),
              Set(node1.id)))
          when(statefulWorkflow.startReadyNodes()).thenReturn(Seq.empty)
          val (probe, wea, _, _, _, testWMClientProbe) =
            launchedStateFixture(workflow)
          wea.underlyingActor.statefulWorkflow = statefulWorkflow

          val (_, nodeCompletedMessage, _) = nodeCompleted()

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
            receiver.expectMsgClass(classOf[ExecutionReport])
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

        val wnea = TestProbe()
        val nodeExecutorFactory = mock[GraphNodeExecutorFactory]
        when(nodeExecutorFactory.createGraphNodeExecutor(any(), any(), any(), any()))
          .thenReturn(wnea.ref)

        val wea: TestActorRef[WorkflowExecutorActor] = TestActorRef(
          new SessionWorkflowExecutorActor(
            executionContext,
            nodeExecutorFactory,
            wmClient.ref,
            publisher.ref,
            TestProbe().ref,
            TestProbe().ref,
            3, "", 3.seconds), Workflow.Id.randomId.toString)

        wea.underlyingActor.statefulWorkflow = statefulWorkflow
        wea.underlyingActor.context.become(wea.underlyingActor.ready())

        val workflow: Workflow = mock[Workflow]
        val workflowWithResults: WorkflowWithResults = mock[WorkflowWithResults]
        val inferredState = mock[InferredState]
        when(statefulWorkflow.workflowWithResults).thenReturn(workflowWithResults)
        when(statefulWorkflow.inferState).thenReturn(inferredState)
        when(statefulWorkflow.getNodesRemovedByWorkflow(workflow)).thenReturn(Set[DeeplangNode]())

        probe.send(wea, UpdateStruct(workflow))

        eventually {
          verify(statefulWorkflow).updateStructure(workflow)
          verify(statefulWorkflow).workflowWithResults
          verify(nodeExecutorFactory, never()).createGraphNodeExecutor(any(), any(), any(), any())
          val saveWorkflow = wmClient.expectMsgClass(classOf[SaveWorkflow])
          saveWorkflow shouldBe SaveWorkflow(workflowWithResults)

          val inferred = publisher.expectMsgClass(classOf[InferredState])
          inferred shouldBe inferredState
        }
      }
    }

    "call updateStructure with nodes deleted" should {
      "send delete to WorkflowNodeExecutorActor" in {
        val statefulWorkflow: StatefulWorkflow = mock[StatefulWorkflow]

        val removedNodes = Set(node1, node2)
        val wmClient = TestProbe()
        val publisher = TestProbe()
        val probe = TestProbe()

        val nodeExecutors = Seq.fill(2)(TestProbe())
        val nodeExecutorFactory = mock[GraphNodeExecutorFactory]
        when(nodeExecutorFactory.createGraphNodeExecutor(any(), any(), any(), any()))
          .thenAnswer(new Answer[ActorRef] {
            var i = 0
            override def answer(invocation: InvocationOnMock): ActorRef = {
              val executor = nodeExecutors(i)
              i = i + 1
              executor.ref
            }
          })

        val wea: TestActorRef[WorkflowExecutorActor] = TestActorRef(
          new SessionWorkflowExecutorActor(
            executionContext,
            nodeExecutorFactory,
            wmClient.ref,
            publisher.ref,
            TestProbe().ref,
            TestProbe().ref,
            3, "", 3.seconds), Workflow.Id.randomId.toString)


        wea.underlyingActor.statefulWorkflow = statefulWorkflow
        wea.underlyingActor.context.become(wea.underlyingActor.ready())
        val workflow: Workflow = mock[Workflow]
        val workflowWithResults: WorkflowWithResults = mock[WorkflowWithResults]
        val inferredState = mock[InferredState]
        when(statefulWorkflow.workflowWithResults).thenReturn(workflowWithResults)
        when(statefulWorkflow.inferState).thenReturn(inferredState)
        doReturn(removedNodes).when(statefulWorkflow).getNodesRemovedByWorkflow(workflow)

        probe.send(wea, UpdateStruct(workflow))

        eventually {
          nodeExecutors.foreach(ne => ne.expectMsgClass(classOf[Delete]))
        }
      }
    }
  }

  private def nodeState(status: NodeStatus): NodeStateWithResults = {
    NodeStateWithResults(NodeState(status, Some(EntitiesMap())), Map(), None)
  }

  private def sendLaunch(
      probe: TestProbe,
      wea: TestActorRef[WorkflowExecutorActor],
      nodes: Set[Node.Id]): Unit = {
    probe.send(wea, Launch(nodes))
  }

  private def verifyStatus(receivers: Seq[TestProbe])(f: (ExecutionReport) => Unit): Unit = {
    eventually {
      receivers.foreach { receiver =>
        f(receiver.expectMsgClass(classOf[ExecutionReport]))
      }
    }
  }

  private def becomeLaunched(
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

  private def becomeFinished(
      wea: TestActorRef[WorkflowExecutorActor],
      workflow: WorkflowWithResults): Unit = {
    val statefulWorkflow = new StatefulWorkflow(
      wea.underlyingActor.executionContext,
      workflow.id,
      workflow.metadata,
      WorkflowInfo.forId(workflow.id),
      workflow.thirdPartyData,
      Execution(StatefulGraph(
        workflow.graph,
        workflow.graph.nodes.map(_.id -> completedState()).toMap,
        None)),
      new DefaultStateInferrer(wea.underlyingActor.executionContext, workflow.id)
    )
    wea.underlyingActor.statefulWorkflow = statefulWorkflow
    wea.underlyingActor.context.become(wea.underlyingActor.ready())
  }

  def executionContext = {
    val context = mock[CommonExecutionContext]
    val sparkContext = mock[SparkContext]
    when(context.sparkContext).thenReturn(sparkContext)
    when(sparkContext.uiWebUrl).thenReturn(None)
    when(context.createExecutionContext(any(), any())).thenReturn(mock[ExecutionContext])
    context
  }

  private def nodeCompleted(): (Id, NodeCompleted, NodeExecutionResults) = {
    val completedId = node1.id
    val results = mockResults(2)
    (completedId, NodeCompleted(completedId, results), results)
  }

  private def mockResults(size: Int): NodeExecutionResults = {
    val ids = (1 to size).map(_ => Entity.Id.randomId)
    val reports = ids.map { id => id -> mock[ReportContent]}.toMap
    val operables = ids.map { id => id -> mock[DOperable]}.toMap
    NodeExecutionResults(ids, reports, operables)
  }

  private def nodeFailed(): (Node.Id, NodeFailed, Exception) = {
    val failedId = node1.id
    val cause = new IllegalStateException("A node failed because a test told him to do so!")
    (failedId, NodeFailed(failedId, cause), cause)
  }

  private def fixture(workflow: WorkflowWithResults, useWorkflowManagerProbe: Boolean = true):
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

    val commonExecutionContext = executionContext
    val inferContext = MockedInferContext()
    when(commonExecutionContext.inferContext).thenReturn(inferContext)
    val wea: TestActorRef[WorkflowExecutorActor] = TestActorRef(
      new SessionWorkflowExecutorActor(
        commonExecutionContext,
        nodeExecutorFactory,
        wmClient,
        publisher.ref,
        TestProbe().ref,
        TestProbe().ref,
        5,
        "",
        3.seconds), workflow.id.toString)

    val statusListeners = Seq(publisher)
    (probe, wea, executors, statusListeners, subscriber, testWMClientProbe)
  }

  private def initializedStateFixture(workflow: WorkflowWithResults):
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

  private def launchedStateFixture(workflow: WorkflowWithResults):
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

  private def finishedStateFixture(workflow: WorkflowWithResults):
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

  private def completedState(): NodeStateWithResults = {
    val entityId: Entity.Id = Entity.Id.randomId
    val now = DateTimeConverter.now
    val dOperables: Map[Entity.Id, DataFrame] = Map(entityId -> new DataFrame())
    NodeStateWithResults(
      NodeState(
        nodestate.Completed(now.minusHours(1), now, Seq(entityId)),
        Some(EntitiesMap(dOperables, Map(entityId -> ReportContentTestFactory.someReport)))),
      dOperables,
    None)
  }

  private def wmClientActor(
      workflows: Map[Workflow.Id, WorkflowWithResults]): ActorRef = {
    TestActorRef(new TestWMClientActor(workflows))
  }

  val inputFile = new InputStorageTypeChoice.File()
    .setFileFormat(new InputFileFormatChoice.Json())
    .setSourceFile(someFilePath)
  val outputFile = new OutputStorageTypeChoice.File()
    .setFileFormat(new OutputFileFormatChoice.Json())
    .setOutputFile(someFilePath)
  val node1 = Node(
    Node.Id.randomId,
    ReadDataFrame().setStorageType(inputFile))
  val node2 = Node(
    Node.Id.randomId,
    new WriteDataFrame().setStorageType(outputFile))
  val invalidNode = Node(Node.Id.randomId, new WriteDataFrame())

  private def someFilePath: String = {
    FilePath(FileScheme.File, "/tmp/doesnt_matter").fullPath
  }

  private def workflowWithResults(id: Workflow.Id): WorkflowWithResults = WorkflowWithResults(
    id,
    WorkflowMetadata(WorkflowType.Batch, "1.0.0"),
    DeeplangGraph(
      Set(node1, node2),
      Set(Edge(node1, 0, node2, 0))),
    JsObject(),
    ExecutionReport(Map(node1.id -> NodeState.draft, node2.id -> NodeState.draft)),
    WorkflowInfo.forId(id)
  )

  private def workflowInvalidInference(workflowId: Workflow.Id): WorkflowWithResults =
    workflowWithResults(workflowId).copy(
      id = workflowId,
      graph = workflowWithResults(workflowId).graph.copy(nodes = Set(node1, node2, invalidNode)),
      executionReport = ExecutionReport(
        Map(
          node1.id -> NodeState.draft,
          node2.id -> NodeState.draft,
          invalidNode.id -> NodeState.draft)))

  private class TestWMClientActor(workflows: Map[Workflow.Id, WorkflowWithResults])
      extends Actor
      with Logging {
    override def receive: Receive = {
      case GetWorkflow(id) =>
        logger.info(workflows.get(id).toString)
        sender() ! workflows.get(id)
    }
  }
}
