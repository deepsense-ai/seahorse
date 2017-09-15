/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.experimentmanager.execution

import java.util.UUID

import io.deepsense.deeplang.doperations.LoadDataFrame

import scala.concurrent.duration._

import akka.actor.Props
import akka.testkit.{TestActorRef, TestProbe}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.{Eventually, ScaledTimeSpans}
import org.scalatest.{BeforeAndAfter, WordSpecLike}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
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

  val created = DateTimeConverter.now
  val updated = created.plusHours(2)
  type TestCode = (TestActorRef[RunningExperimentsActor], TestProbe,
    RunningExperimentsActor, GraphExecutorClient) => Any

  val emptyExperiment = Experiment(Experiment.Id.randomId,
    "B", "Experiment", Graph(), created, updated)
  val failedExperiment = Experiment(Experiment.Id.randomId, "B", "Experiment", Graph(nodes =
    Set(Node(Node.Id.randomId, LoadDataFrame(UUID.randomUUID().toString)).markFailed)),
    created, updated)
  val experiment = Experiment(Experiment.Id.randomId, "B", "Experiment",
    Graph(nodes = Set(Node(Node.Id.randomId, LoadDataFrame(UUID.randomUUID().toString)))),
    created, updated)
  val runningExperiment = Experiment(Experiment.Id.randomId, "B", "Experiment",
    Graph(nodes =
      Set(Node(Node.Id.randomId, LoadDataFrame(UUID.randomUUID().toString)).markQueued)),
    created, updated)

  private def withExperiment(experiment: Experiment)(testCode: TestCode): Unit = {
    val mockClientFactory = mock[GraphExecutorClientFactory]
    val graphExecutorClient = createMockGraphExecutorClient(experiment.graph)
    val actorRef = createTestedActor(3L, 15000L, mockClientFactory)
    val actor = actorRef.underlyingActor
    val probe = TestProbe()
    when(mockClientFactory.create()).thenReturn(graphExecutorClient)
    testCode(actorRef, probe, actor, graphExecutorClient)
  }


  private def withLaunchedExperiments(experimentsToLaunch: Set[Experiment],
      experimentForGEC: Experiment = emptyExperiment)(testCode: TestCode): Unit = {
    withExperiment(experimentForGEC) {
      (actorRef, probe, actor, gec) => {
        experimentsToLaunch.foreach(e => probe.send(actorRef, Launch(e)))
        probe.receiveN(experimentsToLaunch.size)
        testCode(actorRef, probe, actor, gec)
      }
    }
  }

  "RunningExperimentsActor" should {

    "launch experiment" when {
      "received Launch on empty experiment" in {
        withExperiment(emptyExperiment) {
          (actorRef, probe, actor, gec) => {
            probe.send(actorRef, Launch(emptyExperiment))
            probe.expectMsg(Launched(emptyExperiment.markRunning))
            eventually(timeout(1.seconds), interval(100.milliseconds)) {
              actor.experiments should contain key emptyExperiment.id
              verify(gec).sendExperiment(emptyExperiment.markRunning)
              probe.send(actorRef, GetStatus(emptyExperiment.id))
              probe.expectMsg(Status(Some(emptyExperiment.markCompleted)))
            }
          }
        }
      }
    }

    "launch experiment" when {
      "received launch on not empty experiment" in {
        withExperiment(runningExperiment) {
          (actorRef, probe, actor, gec) => {
            probe.send(actorRef, Launch(runningExperiment))
            probe.expectMsg(Launched(runningExperiment.markRunning))
            eventually(timeout(1.seconds), interval(100.milliseconds)) {
              actor.experiments should contain key runningExperiment.id
              verify(gec).sendExperiment(runningExperiment.markRunning)
              probe.send(actorRef, GetStatus(runningExperiment.id))
              probe.expectMsg(Status(Some(runningExperiment.markRunning)))
            }
          }
        }
      }
    }

    "answer Rejected" when {
      "launching already-running experiment" in {
        withLaunchedExperiments(Set(runningExperiment), runningExperiment) {
          (actorRef, probe, actor, gec) => {
            eventually(timeout(1.seconds), interval(100.milliseconds)) {
              probe.send(actorRef, GetStatus(runningExperiment.id))
              val msg = probe.expectMsgClass(classOf[Status])
              msg.experiment.get.isRunning shouldBe true
            }
            eventually(timeout(1.seconds), interval(100.milliseconds)) {
              probe.send(actorRef, Launch(runningExperiment))
              probe.expectMsgType[Rejected]
            }
          }
        }
      }
    }

    "should mark experiment as failed" when {
      "Launch fails" in {
        withExperiment(experiment) {
          (actorRef, probe, actor, gec) => {
            when(gec.waitForSpawn(anyInt()))
              .thenThrow(new RuntimeException("Launching failed"))
            probe.send(actorRef, Launch(experiment))
            probe.expectMsg(Launched(experiment.markRunning))
            eventually(timeout(1.seconds), interval(100.milliseconds)) {
              actor.experiments(experiment.id)._1.state.status shouldBe Experiment.Status.Failed
              probe.send(actorRef, GetStatus(experiment.id))
              val msg = probe.expectMsgClass(classOf[Status])
              msg.experiment.get.isFailed shouldBe true
            }
          }
        }
      }
    }

    "answer with Status(None)" when {
      "received GetStatus but the experiment was not launched" in {
        withLaunchedExperiments(Set()) {
          (actorRef, probe, actor, gec) => {
            probe.send(actorRef, GetStatus(emptyExperiment.id))
            probe.expectMsg(Status(None))
          }
        }
      }
    }

    "abort experiment" when {
      "received Abort on launched experiment" in {
        withExperiment(experiment) {
          (actorRef, probe, actor, gec) => {
            probe.send(actorRef, Launch(experiment))
            probe.send(actorRef, Abort(experiment.id))
            eventually(timeout(1.seconds), interval(100.milliseconds)) {
              probe.send(actorRef, GetStatus(experiment.id))
              val msg = probe.expectMsgClass(classOf[Status])
              msg.experiment.get.isAborted shouldBe true
              verify(gec).terminateExecution()
            }
          }
        }
      }
    }

    "not abort experiment" when {
      "received Abort on not running experiment" in {
        withExperiment(failedExperiment) {
          (actorRef, probe, actor, gec) => {
            probe.send(actorRef, Launch(failedExperiment))
            eventually(timeout(1.seconds), interval(100.milliseconds)) {
              probe.send(actorRef, GetStatus(failedExperiment.id))
              val msg = probe.expectMsgClass(classOf[Status])
              msg.experiment.get.isFailed shouldBe true
            }
            probe.send(actorRef, Abort(failedExperiment.id))
            eventually(timeout(1.seconds), interval(100.milliseconds)) {
              probe.send(actorRef, GetStatus(failedExperiment.id))
              val msg = probe.expectMsgClass(classOf[Status])
              msg.experiment.get.isFailed shouldBe true
            }
          }
        }
      }
    }

    "abort experiment" when {
      "received Abort on inDraft experiment" in {
        withExperiment(experiment) {
          (actorRef, probe, actor, gec) => {
            probe.send(actorRef, Abort(experiment.id))
            eventually(timeout(1.seconds), interval(100.milliseconds)) {
              probe.send(actorRef, GetStatus(experiment.id))
              probe.expectMsg(Status(None))
            }
          }
        }
      }
    }

    "list experiments" when {
      val updatedGraph = Graph()
      val tenantId = emptyExperiment.tenantId
      val experiment1 = emptyExperiment.copy(id = UUID.randomUUID(), description = "1")
      val experiment2 = emptyExperiment.copy(id = UUID.randomUUID(), description = "2")
      val experiment3 = emptyExperiment.copy(id = UUID.randomUUID(), description = "3")
      val otherTenantId = tenantId + "other"
      val experiment4 = emptyExperiment.copy(
        id = UUID.randomUUID(),
        tenantId = otherTenantId,
        description = "4")
      val experiments = Set(experiment1, experiment2, experiment3, experiment4)
      val expectedExperimentsOfTenant1 =
        Map(tenantId ->
          Set(experiment1.withGraph(updatedGraph),
            experiment2.withGraph(updatedGraph),
            experiment3.withGraph(updatedGraph)))
      val expectedExperimentsOfTenant2 =
        Map(otherTenantId -> Set(experiment4.withGraph(updatedGraph)))

      "received ListExperiments with id of tenant that has experiments" in {
        withLaunchedExperiments(experiments) {
          (actorRef, probe, actor, gec) => {
            eventually(timeout(1.seconds), interval(100.milliseconds)) {
              probe.send(actorRef, ExperimentsByTenant(Some(experiment1.tenantId)))
              val receivedExperiments = probe.expectMsgAnyClassOf(classOf[ExperimentsMap])
              receivedExperiments.experimentsByTenantId should have size 1
              receivedExperiments.experimentsByTenantId(experiment1.tenantId) should
                contain theSameElementsAs expectedExperimentsOfTenant1(experiment1.tenantId)
            }
          }
        }
      }

      "received ListExperiments without tenantId" in {
        withLaunchedExperiments(experiments) {
          (actorRef, probe, actor, gec) => {
            eventually(timeout(1.seconds), interval(100.milliseconds)) {
              probe.send(actorRef, ExperimentsByTenant(None))
              val receivedExperiments = probe.expectMsgAnyClassOf(classOf[ExperimentsMap])
              receivedExperiments.experimentsByTenantId should have size 2
              receivedExperiments.experimentsByTenantId(tenantId) should
                contain theSameElementsAs expectedExperimentsOfTenant1(tenantId)
              receivedExperiments.experimentsByTenantId(otherTenantId) should
                contain theSameElementsAs expectedExperimentsOfTenant2(otherTenantId)
            }
          }
        }
      }
    }

    "answer with empty map" when {
      "received ListExperiments with id of tenant that has no experiments" in {
        withExperiment(emptyExperiment) {
          (actorRef, probe, actor, gec) => {
            probe.send(actorRef, ExperimentsByTenant(Some("tenantWithNoExperiments")))
            probe.expectMsgAnyClassOf(classOf[ExperimentsMap])
              .experimentsByTenantId shouldBe Map.empty
          }
        }
      }
    }

    "update experiments' statuses when they are running" in {
      val experimentWithNode = experiment
      val expectedExperiment = experimentWithNode.withGraph(Graph())
      withLaunchedExperiments(Set(experimentWithNode), expectedExperiment) {
        (actorRef, probe, actor, gec) => {
          eventually(timeout(1.seconds), interval(100.milliseconds)) {
            probe.send(actorRef, GetStatus(experimentWithNode.id))
            val Status(Some(exp)) = probe.expectMsgType[Status]
            exp shouldBe expectedExperiment
          }
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
     refreshTimeoutMillis: Long,
     mockClientFactory: GraphExecutorClientFactory): TestActorRef[RunningExperimentsActor] =
    TestActorRef(Props(new RunningExperimentsActor(
      SimpleGraphExecutionIntegSuiteEntities.Name,
      5000L,
      refreshIntervalMillis,
      refreshTimeoutMillis,
      mockClientFactory)))
}
