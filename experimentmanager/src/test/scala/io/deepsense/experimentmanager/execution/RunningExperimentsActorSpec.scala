/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.experimentmanager.execution

import scala.concurrent.duration._

import akka.actor.Props
import akka.testkit.{TestActorRef, TestProbe}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.{Eventually, ScaledTimeSpans}
import org.scalatest.{BeforeAndAfter, WordSpecLike}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.DOperation
import io.deepsense.experimentmanager.execution.RunningExperimentsActor._
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphexecutor.{GraphExecutorClient, SimpleGraphExecutionIntegSuiteEntities}
import io.deepsense.models.experiments.Experiment

class RunningExperimentsActorSpec
  extends StandardSpec
  with UnitTestSupport
  with WordSpecLike
  with BeforeAndAfter
  with Eventually
  with ScaledTimeSpans {

  val tenantIdA = "A"
  val created = DateTimeConverter.now
  val updated = created.plusHours(2)
  val experiment = Experiment(
    Experiment.Id.randomId,
    tenantIdA,
    "Experiment",
    Graph(),
    created,
    updated)
  val updatedGraph = Graph()
  val launched = experiment.markRunning
  val mockClientFactory = mock[GraphExecutorClientFactory]

  var runningExperimentActorRef: TestActorRef[RunningExperimentsActor] = _
  var actor: RunningExperimentsActor = _
  var probe: TestProbe = TestProbe()
  var graphExecutorClient: GraphExecutorClient = _

  before {
    graphExecutorClient = createMockGraphExecutorClient(updatedGraph)
    runningExperimentActorRef = createTestedActor(3L, 15000L)
    actor = runningExperimentActorRef.underlyingActor
    probe = TestProbe()
    when(mockClientFactory.create()).thenReturn(graphExecutorClient)
  }

  private def withLaunchedExperiments(experiments: Set[Experiment])(testCode: => Any): Unit = {
    withLaunchedExperiments(runningExperimentActorRef, experiments)(testCode)
  }

  private def withLaunchedExperiments(
      ar: TestActorRef[RunningExperimentsActor],
      experiments: Set[Experiment])(testCode: => Any): Unit = {
    experiments.foreach(e => probe.send(ar, Launch(e)))
    probe.receiveN(experiments.size)
    testCode
  }

  "RunningExperimentsActor" should {
    "launch experiment" when {
      "received Launch" in {
        probe.send(runningExperimentActorRef, Launch(experiment))
        probe.expectMsg(Launched(launched))
        eventually {
          verify(graphExecutorClient).sendExperiment(launched)
          actor.experiments should contain key experiment.id
        }
      }
    }
    "should mark experiment as failed" when {
      "launch fails" in {
        pending
      }
    }
    "answer with Status(Some(...))" when {
      "received GetStatus and the experiment was queued" in {
        probe.send(runningExperimentActorRef, Launch(experiment))
        probe.expectMsgAnyClassOf(classOf[Launched])
        probe.send(runningExperimentActorRef, GetStatus(experiment.id))
        probe.expectMsg(Status(Some(launched)))
      }
    }
    "answer with Status(None)" when {
      "received GetStatus but the experiment was not queued" in {
        probe.send(runningExperimentActorRef, GetStatus(experiment.id))
        probe.expectMsg(Status(None))
      }
    }
    "abort experiment" when {
      "received Abort" in {
        val testedActor = createTestedActor(10000, 10000)
        withLaunchedExperiments(testedActor,Set(experiment)) {
          val abortedExperiment = experiment.markAborted
          probe.send(testedActor, Abort(experiment.id))
          probe.expectMsg(Status(Some(abortedExperiment)))
          probe.send(testedActor, GetStatus(experiment.id))
          probe.expectMsg(Status(Some(abortedExperiment)))
          eventually {
            verify(graphExecutorClient).terminateExecution()
          }
        }
      }
    }
    "list experiments" when {
      val experiment1 = experiment.copy(id = Experiment.Id.randomId, description = "1")
      val experiment2 = experiment.copy(id = Experiment.Id.randomId, description = "2")
      val experiment3 = experiment.copy(id = Experiment.Id.randomId, description = "3")
      val tenantIdOther = tenantIdA + "other"
      val experiment4 = experiment.copy(
        id = Experiment.Id.randomId,
        tenantId = tenantIdOther,
        description = "4")
      val experiments = Set(experiment1, experiment2, experiment3, experiment4)
      val expectedExperimentsOfTenant1 =
        Map(tenantIdA ->
          Set(experiment1.withGraph(updatedGraph),
            experiment2.withGraph(updatedGraph),
            experiment3.withGraph(updatedGraph)))
      val expectedExperimentsOfTenant2 =
        Map(experiment4.tenantId -> Set(experiment4.withGraph(updatedGraph)))

      "received ListExperiments with tenantId and tenant has experiments" in {
        pending
      }
      "received ListExperiments without tenantId" in {
        pending
      }
    }
    "answer with empty map" when {
      "received ListExperiments with tenantId and tenant has no experiments" in {
        probe.send(runningExperimentActorRef, ExperimentsByTenant(Some("tenantWithNoExperiments")))
        probe.expectMsgAnyClassOf(classOf[ExperimentsMap])
          .experimentsByTenantId shouldBe Map.empty
      }
    }
    "update experiments' statuses when they are running" in {
      val mockOperation = mock[DOperation]
      when(mockOperation.inArity).thenReturn(1)
      when(mockOperation.outArity).thenReturn(1)
      when(mockOperation.id).thenReturn(DOperation.Id.randomId)
      val mockNode = Node(Node.Id.randomId, mockOperation)
      val experimentWithNode = Experiment(
        Experiment.Id.randomId,
        "A",
        "Experiment",
        Graph(Set(mockNode)),
        created,
        updated)
      val expectedExperiment = experimentWithNode.withGraph(updatedGraph)
      withLaunchedExperiments(Set(experimentWithNode)) {
        eventually (timeout(6.seconds), interval(1.second)) {
          probe.send(runningExperimentActorRef, GetStatus(experimentWithNode.id))
          val Status(Some(exp)) = probe.expectMsgType[Status]
          exp shouldBe expectedExperiment
        }
      }
    }
  }

  private def createMockGraphExecutorClient(graph: Graph): GraphExecutorClient = {
    val gec = mock[GraphExecutorClient]
    when(gec.waitForSpawn(any())).thenReturn(true)
    when(gec.sendExperiment(any())).thenReturn(true)
    when(gec.getExecutionState()).thenReturn(Some(graph))
    when(gec.terminateExecution()).thenReturn(true)
    gec
  }

  private def createTestedActor(
      refreshIntervalMillis: Long,
      refreshTimeoutMillis: Long): TestActorRef[RunningExperimentsActor] =
    TestActorRef(Props(new RunningExperimentsActor(
      SimpleGraphExecutionIntegSuiteEntities.Name,
      5000L,
      refreshIntervalMillis,
      refreshTimeoutMillis,
      mockClientFactory)))
}
