/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */
package io.deepsense.experimentmanager.execution

import java.util.UUID

import scala.concurrent.duration._

import akka.actor.Props
import akka.testkit.{TestActorRef, TestProbe}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.{Eventually, ScaledTimeSpans}
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}

import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.DOperation
import io.deepsense.experimentmanager.execution.RunningExperimentsActor._
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphexecutor.{SimpleGraphExecutionIntegSuiteEntities, GraphExecutorClient}
import io.deepsense.models.experiments.Experiment

class RunningExperimentsActorSpec
  extends StandardSpec
  with UnitTestSupport
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with Eventually
  with ScaledTimeSpans {

  val experiment = Experiment(
    UUID.randomUUID(),
    "A",
    "Experiment",
    Graph())

  val updatedGraph = Graph()
  val graphExecutorClient = createMockGraphExecutorClient(updatedGraph)
  val launched = experiment.copy(graph = experiment.graph.enqueueNodes)
  val mockClientFactory = mock[GraphExecutorClientFactory]
  when(mockClientFactory.create()).thenReturn(graphExecutorClient)

  var actorRef: TestActorRef[RunningExperimentsActor] = createTestedActor
  var actor = actorRef.underlyingActor
  var probe = TestProbe()

  before {
    actorRef = createTestedActor
    actor = actorRef.underlyingActor
    probe = TestProbe()
  }

  def withLaunchedExperiments(experiments: Set[Experiment])(testCode: => Any): Unit = {
    experiments.foreach(e => probe.send(actorRef, Launch(e)))
    probe.receiveN(experiments.size)
    testCode
  }

  "RunningExperimentsActor" should {
    "launch experiment" when {
      "received Launch" in {
        probe.send(actorRef, Launch(experiment))
        probe.expectMsg(Status(Some(launched)))
        eventually {
          actor.experiments should contain key experiment.id
          verify(graphExecutorClient).sendExperiment(experiment)
        }
      }
    }
    "answer with Status(Some(...))" when {
      "received GetStatus and the experiment was queued" in {
        probe.send(actorRef, Launch(experiment))
        probe.expectMsgAnyClassOf(classOf[Status])
        probe.send(actorRef, GetStatus(experiment.id))
        probe.expectMsg(Status(Some(launched)))
      }
    }
    "answer with Status(None)" when {
      "received GetStatus but the experiment was not queued" in {
        probe.send(actorRef, GetStatus(experiment.id))
        probe.expectMsg(Status(None))
      }
    }
    "abort experiment" when {
      "received Abort" in {
        withLaunchedExperiments(Set(experiment)) {
          val abortedExperiment = experiment.copy(graph = experiment.graph.abortNodes)
          probe.send(actorRef, Abort(experiment.id))
          probe.expectMsg(Status(Some(abortedExperiment)))
          probe.send(actorRef, GetStatus(experiment.id))
          probe.expectMsg(Status(Some(abortedExperiment)))
          eventually {
            verify(graphExecutorClient).terminateExecution()
          }
        }
      }
    }
    "list experiments" when {
      val experiment1 = experiment.copy(id = UUID.randomUUID(), description = "1")
      val experiment2 = experiment.copy(id = UUID.randomUUID(), description = "2")
      val experiment3 = experiment.copy(id = UUID.randomUUID(), description = "3")
      val experiment4 = experiment.copy(
        id = UUID.randomUUID(),
        tenantId = experiment.tenantId + "other",
        description = "4")
      val experiments = Set(experiment1, experiment2, experiment3, experiment4)
      val expectedExperimentsOfTenant1 =
        Map(experiment1.tenantId -> Set(experiment1, experiment2, experiment3))
      val expectedExperimentsOfTenant2 =
        Map(experiment4.tenantId -> Set(experiment4))

      "received ListExperiments with tenantId and tenant has experiments" in {
        withLaunchedExperiments(experiments) {
          probe.send(actorRef, ListExperiments(Some(experiment1.tenantId)))
          val receivedExperiments = probe.expectMsgAnyClassOf(classOf[Experiments])
          receivedExperiments.experimentsByTenantId should have size 1
          receivedExperiments.experimentsByTenantId(experiment1.tenantId) should
            contain theSameElementsAs expectedExperimentsOfTenant1(experiment1.tenantId)
        }
      }
      "received ListExperiments without tenantId" in {
        withLaunchedExperiments(experiments) {
          probe.send(actorRef, ListExperiments(None))
          val receivedExperiments = probe.expectMsgAnyClassOf(classOf[Experiments])
          receivedExperiments.experimentsByTenantId should have size 2
          receivedExperiments.experimentsByTenantId(experiment1.tenantId) should
            contain theSameElementsAs expectedExperimentsOfTenant1(experiment1.tenantId)
          receivedExperiments.experimentsByTenantId(experiment4.tenantId) should
            contain theSameElementsAs expectedExperimentsOfTenant2(experiment4.tenantId)
        }
      }
    }
    "answer with empty map" when {
      "received ListExperiments with tenantId and tenant has no experiments" in {
        probe.send(actorRef, ListExperiments(Some("tenantWithNoExperiments")))
        probe.expectMsgAnyClassOf(classOf[Experiments])
          .experimentsByTenantId shouldBe Map.empty
      }
    }
    "update experiments' statuses when they are running" in {
      val mockOperation = mock[DOperation]
      when(mockOperation.inArity).thenReturn(1)
      when(mockOperation.outArity).thenReturn(1)
      when(mockOperation.id).thenReturn(DOperation.Id.randomId)
      val mockNode = Node(UUID.randomUUID(), mockOperation)
      val experimentWithNode = Experiment(
        UUID.randomUUID(),
        "A",
        "Experiment",
        Graph(Set(mockNode)))
      val expectedExperiment = experimentWithNode.copy(graph = updatedGraph)
      withLaunchedExperiments(Set(experimentWithNode)) {
        eventually (timeout(6.seconds), interval(1.second)) {
          probe.send(actorRef, GetStatus(experimentWithNode.id))
          val status = probe.expectMsgClass(classOf[Status])
          status.experiment shouldBe Some(expectedExperiment)
        }
      }
    }
  }

  private def createMockGraphExecutorClient(graph: Graph): GraphExecutorClient = {
    val gec = mock[GraphExecutorClient]
    when(gec.waitForSpawn(any())).thenReturn(true)
    when(gec.sendExperiment(any())).thenReturn(true)
    when(gec.getExecutionState()).thenReturn(graph)
    when(gec.terminateExecution()).thenReturn(true)
    gec
  }

  private def createTestedActor: TestActorRef[RunningExperimentsActor] = {
    TestActorRef(Props(new RunningExperimentsActor(
      SimpleGraphExecutionIntegSuiteEntities.Name,
      5000L,
      3L,
      mockClientFactory)))
  }
}
