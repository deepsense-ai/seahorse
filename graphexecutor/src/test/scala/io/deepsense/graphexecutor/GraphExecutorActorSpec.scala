/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import scala.util.Success

import akka.actor.{ActorSystem, Actor, ActorRef, Props}
import akka.testkit.{TestActorRef, TestProbe}
import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.concurrent.{Eventually, ScaledTimeSpans}
import org.scalatest.mock.MockitoSugar

import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.{DOperation, DOperable, ExecutionContext}
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphexecutor.GraphExecutorActor._
import io.deepsense.models.entities.Entity
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.messages._

class GraphExecutorActorSpec
  extends StandardSpec
  with UnitTestSupport
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with MockitoSugar
  with Eventually
  with ScaledTimeSpans {

  trait TestCase {
    val gec = TestProbe()
    val gecPath = gec.ref.path.toStringWithoutAddress
    val executionContext = new ExecutionContext
    val graph = mock[Graph]
    when(graph.enqueueNodes) thenReturn graph

    val experiment = Mockito.spy(Experiment(
      id = Experiment.Id.randomId,
      tenantId = "tenant-id-for-gea-test",
      name = "name-of-experiment-for-gea-test",
      graph = graph))

    class Wrapper(target: ActorRef) extends Actor {
      def receive: Receive = {
        case x => target forward x
      }
    }

    trait TestGraphNodeExecutorFactory extends GraphNodeExecutorFactory {
      var nodeExecutors: Seq[TestProbe] = _
      var expectedNodes: List[Node] = _
      var expectedDOperableCache: Results = _
      var createdExecutorsCount = 0
      var expectedExperiment = experiment

      def createGraphNodeExecutor(
          ec: ExecutionContext,
          node: Node,
          exp: Experiment,
          dOperableCache: Results): Actor = {
        synchronized {
          executionContext.tenantId shouldBe experiment.tenantId
          ec shouldBe executionContext
          exp shouldBe expectedExperiment.markRunning
          node shouldBe expectedNodes(createdExecutorsCount)
          dOperableCache shouldBe expectedDOperableCache
          val returnedExecutor = nodeExecutors(createdExecutorsCount)
          createdExecutorsCount += 1
          new Wrapper(returnedExecutor.ref)
        }
      }
    }

    val geaRef = TestActorRef[GraphExecutorActor with TestGraphNodeExecutorFactory](Props(
      new GraphExecutorActor(executionContext, gecPath) with TestGraphNodeExecutorFactory))
    val gea = geaRef.underlyingActor

    def startCommunication(): Unit = {
      val experimentId = Experiment.Id.randomId
      geaRef ! GraphExecutorActor.Messages.Start(experimentId)
      gec.expectMsg(ExecutorReady(experimentId))
    }

    def launchExperiment(): Unit = {
      val nodes = List(mockNode(), mockNode())
      val nodeExecutors = Seq(TestProbe(), TestProbe())
      when(graph.readyNodes).thenReturn(nodes)
      gea.expectedNodes = nodes
      gea.nodeExecutors = nodeExecutors
      gea.expectedDOperableCache = Map.empty

      geaRef ! Launch(experiment)
      gec.expectMsg(Success(experiment.markRunning))
      gea.nodeExecutors(0).expectMsg(GraphNodeExecutorActor.Messages.Start())
      gea.nodeExecutors(1).expectMsg(GraphNodeExecutorActor.Messages.Start())
      gea.createdExecutorsCount shouldBe 2
    }

    def mockOperation(): DOperation = {
      val operation = mock[DOperation]
      when(operation.inArity) thenReturn 0
      when(operation.outArity) thenReturn 1
      when(operation.id) thenReturn DOperation.Id.randomId
      operation
    }

    def mockNode(): Node = {
      val operation = mockOperation()
      val node = mock[Node]
      when(node.id) thenReturn Node.Id.randomId
      when(node.operation) thenReturn operation
      node
    }

    def mockFinishedNode(): Node = {
      val finishedNode = mockNode()
      when(finishedNode.isRunning) thenReturn false
      when(finishedNode.isCompleted) thenReturn true
      when(finishedNode.isFailed) thenReturn false
      finishedNode
    }
  }

  "GraphExecutorActor" should {
    "send ExecutorReady to actor specified in constructor" when {
      "it receives Start" in new TestCase {
        startCommunication()
      }
    }
    "start GraphNodeExecutorActor for each ready node and send Start to it" when {
      "it receives Launch" in new TestCase {
        startCommunication()
        launchExperiment()
      }
    }
    "send Update of experiment" when {
      "it receives NodeStarted" in new TestCase {
        startCommunication()
        launchExperiment()

        val nodeId = Node.Id.randomId
        val updatedGraph = mock[Graph]
        val updatedExperiment =
          experiment.copy(graph = updatedGraph, state = experiment.state.running)
        when(graph.markAsRunning(nodeId)) thenReturn updatedGraph
        geaRef ! GraphExecutorActor.Messages.NodeStarted(nodeId)
        gec.expectMsg(Update(updatedExperiment))
      }
    }
    "send Update of experiment and start new nodes" when {
      "it receives NodeFinished and there are some nodes left for execution" in new TestCase {
        startCommunication()
        launchExperiment()

        val finishedNode = mockFinishedNode()

        val dOperableCache1 = Map(
          Entity.Id.randomId -> mock[DOperable],
          Entity.Id.randomId -> mock[DOperable])
        gea.expectedDOperableCache = dOperableCache1
        gea.createdExecutorsCount = 0
        val spy = Mockito.spy(gea.experiment)
        when(spy.markRunning) thenReturn spy
        gea.expectedExperiment = spy
        gea.experiment = spy

        val readyNodes = List(mockNode(), mockNode())

        when(spy.withNode(any())) thenReturn spy
        when(spy.readyNodes) thenReturn readyNodes
        gea.expectedNodes = readyNodes

        geaRef ! GraphExecutorActor.Messages.NodeFinished(finishedNode, dOperableCache1)

        gea.nodeExecutors(0).expectMsg(GraphNodeExecutorActor.Messages.Start())
        gea.nodeExecutors(1).expectMsg(GraphNodeExecutorActor.Messages.Start())

        gec.expectMsg(Update(spy))
      }
    }
    "send Update of experiment" when {
      "it receives NodeFinished and there are some running nodes left" in new TestCase {
        startCommunication()
        launchExperiment()

        val finishedNode = mockFinishedNode()

        val stillRunningNode = mockNode()
        when(stillRunningNode.isRunning) thenReturn true
        when(graph.nodes) thenReturn Set(stillRunningNode)
        when(graph.withChangedNode(finishedNode)) thenReturn graph

        geaRef ! GraphExecutorActor.Messages.NodeFinished(finishedNode, Map.empty)
        gec.expectMsg(Update(experiment.markRunning))
      }
    }
    "send Update of experiment and close actor system" when {
      "it receives NodeFinished and there are no nodes left for execution" in new TestCase {
        startCommunication()
        launchExperiment()

        val finishedNode = mockFinishedNode()

        val finishedExperiment = mock[Experiment]

        val runningExperiment = mock[Experiment]
        when(runningExperiment.withNode(any())) thenReturn runningExperiment
        when(runningExperiment.updateState()) thenReturn finishedExperiment
        when(runningExperiment.readyNodes) thenReturn List.empty
        when(runningExperiment.runningNodes) thenReturn Set.empty[Node]
        when(finishedExperiment.state) thenReturn mock[Experiment.State]

        gea.experiment = runningExperiment

        val parent = TestProbe()
        parent.watch(geaRef)

        geaRef ! GraphExecutorActor.Messages.NodeFinished(finishedNode, Map.empty)
        gec.expectMsg(Update(finishedExperiment))
        gec.expectNoMsg()
        parent.expectTerminated(geaRef)
      }
      "it receives Abort" in new TestCase {
        startCommunication()
        launchExperiment()

        val runningExperiment = mock[Experiment]
        gea.experiment = runningExperiment

        val parent = TestProbe()
        parent.watch(geaRef)

        val experimentWithUpdatedState = mock[Experiment]
        when(runningExperiment.updateState()).thenReturn(experimentWithUpdatedState)
        when(experimentWithUpdatedState.state) thenReturn mock[Experiment.State]

        geaRef ! Abort(experiment.id)

        gec.expectMsg(Update(experimentWithUpdatedState))
        parent.expectTerminated(geaRef)
      }
    }
  }
}
