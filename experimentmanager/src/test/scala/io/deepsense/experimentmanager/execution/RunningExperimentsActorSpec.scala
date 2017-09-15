/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.execution

import java.util.UUID

import akka.actor.{ActorRef, Actor, Props}
import akka.testkit.{TestActorRef, TestProbe}
import io.deepsense.experimentmanager.exceptions.ExperimentNotRunningException
import io.deepsense.graphexecutor.clusterspawner.DefaultClusterSpawner
import org.scalatest.concurrent.{Eventually, ScaledTimeSpans}
import org.scalatest.{BeforeAndAfter, WordSpecLike}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.doperations.LoadDataFrame
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphexecutor.SimpleGraphExecutionIntegSuiteEntities
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.messages._
import io.deepsense.models._

import scala.util._

class RunningExperimentsActorSpec
  extends StandardSpec
  with UnitTestSupport
  with WordSpecLike
  with BeforeAndAfter
  with Eventually
  with ScaledTimeSpans {

  val created = DateTimeConverter.now
  val updated = created.plusHours(2)
  val node = Node(Node.Id.randomId, LoadDataFrame(UUID.randomUUID().toString))
  val experiment = Experiment(Experiment.Id.randomId, "B", "Experiment",
    Graph(nodes = Set(node)),
    created, updated)
  val runningExperiment = experiment.copy(graph = Graph(nodes = Set(node.markRunning))).markRunning

  class Wrapper(target: ActorRef) extends Actor {
    def receive: Receive = {
      case x => target forward x
    }
  }

  trait TestCase {

    trait TestGraphExecutorClientFactory extends GraphExecutorClientFactory {
      def createGraphExecutorClient(): Actor = new Actor {
        def receive: Receive = {
          case x => probeGEC.ref forward x
        }
      }
    }

    val probeGEC = TestProbe()
    val actorRef: TestActorRef[RunningExperimentsActor] = TestActorRef(
      Props(new RunningExperimentsActor(
        SimpleGraphExecutionIntegSuiteEntities.Name,
        5000L, DefaultClusterSpawner) with TestGraphExecutorClientFactory)
    )
    val actor = actorRef.underlyingActor
    val probe = TestProbe()

    def launch(experiments: Set[Experiment]): Unit = {
      experiments.foreach(e => probe.send(actorRef, Launch(e)))
      probe.receiveN(experiments.size)
    }
  }

  "RunningExperimentsActor" should {

    "launch experiment and respond Success" when {
      "received Launch on experiment" in new TestCase {
        probe.send(actorRef, Launch(experiment))
        probe.expectMsg(Success(runningExperiment))
        probeGEC.expectMsg(Launch(experiment))
      }
    }

    "not launch experiment and respond Failure" when {
      "received Launch on running experiment" in new TestCase {
        probe.send(actorRef, Launch(experiment))
        probe.expectMsgClass(classOf[Success[Experiment]])
        probe.send(actorRef, Launch(experiment))
        val failure = probe.expectMsgClass(classOf[Failure[Experiment]])
        // TODO use better exception class
        failure.exception shouldBe an [IllegalStateException]
      }
    }

    "abort experiment and respond Success" when {
      "received Abort on running experiment" in new TestCase {
        probe.send(actorRef, Launch(experiment))
        probe.expectMsgClass(classOf[Success[Experiment]])
        probeGEC.expectMsg(Launch(experiment))
        probe.send(actorRef, Abort(experiment.id))
        val success = probe.expectMsgClass(classOf[Success[Experiment]])
        success.get shouldBe 'Aborted
        probeGEC.expectMsgClass(classOf[Abort])
      }
    }


    "not abort experiment and respond Failure" when {
      "received Abort on not running experiment" in new TestCase {
        probe.send(actorRef, Launch(experiment))
        probe.expectMsgClass(classOf[Success[Experiment]])
        probe.send(actorRef, Update(experiment))
        probe.send(actorRef, Abort(experiment.id))
        val failure = probe.expectMsgClass(classOf[Failure[Experiment]])
        failure.exception shouldBe a [ExperimentNotRunningException]
      }
      "received Abort on not existing experiment" in new TestCase {
        probe.send(actorRef, Abort(experiment.id))
        val failure = probe.expectMsgClass(classOf[Failure[Experiment]])
        failure.exception shouldBe an [ExperimentNotRunningException]
      }
    }

    "respond with experiment" when {
      "ask with Get" in new TestCase {
        probe.send(actorRef, Launch(experiment))
        probe.expectMsgClass(classOf[Success[Experiment]])
        probe.send(actorRef, messages.Get(experiment.id))
        eventually {
          probe.expectMsg(Some(runningExperiment))
        }
      }
      "experiment is updated via Update" in new TestCase {
        probe.send(actorRef, Launch(experiment))
        probe.send(actorRef, Update(runningExperiment))
        probe.send(actorRef, messages.Get(experiment.id))
        eventually {
          probe.expectMsg(Some(runningExperiment))
        }
      }
    }

    "respond with None" when {
      "ask with Get for non-existing experiment" in new TestCase {
        probe.send(actorRef, messages.Get(experiment.id))
        probe.expectMsg(None)
      }
    }
    "delete experiment" when {
      "received Delete" in new TestCase {
        probe.send(actorRef, Launch(experiment))
        probe.expectMsgClass(classOf[Success[Experiment]])
        probe.send(actorRef, Update(experiment))
        probe.send(actorRef, messages.Delete(experiment.id))
        probe.send(actorRef, messages.Get(experiment.id))
        probe.expectMsg(None)
      }
    }

    "list experiments" when {
      val tenantId = experiment.tenantId
      val experiment1 = experiment.copy(id = UUID.randomUUID(), description = "1")
      val experiment2 = experiment.copy(id = UUID.randomUUID(), description = "2")
      val experiment3 = experiment.copy(id = UUID.randomUUID(), description = "3")
      val otherTenantId = tenantId + "other"
      val experiment4 = experiment.copy(
        id = UUID.randomUUID(),
        tenantId = otherTenantId,
        description = "4")
      val experiments = Set(experiment1, experiment2, experiment3, experiment4)
      val expectedExperimentsOfTenant1 =
        Map(tenantId -> Set(experiment1, experiment2, experiment3).map(_.markRunning))
      val expectedExperimentsOfTenant2 = Set(experiment4.markRunning)

      "received GetAllByTenantId with id of tenant that has experiments" in new TestCase {
        launch(experiments)
        probe.send(actorRef, GetAllByTenantId(experiment1.tenantId))

        {
          val receivedExperiments = probe.expectMsgAnyClassOf(classOf[ExperimentsMap])
          receivedExperiments.experimentsByTenantId should have size 1
          receivedExperiments.experimentsByTenantId(experiment1.tenantId) should
            contain theSameElementsAs expectedExperimentsOfTenant1(experiment1.tenantId)
        }

        probe.send(actorRef, GetAllByTenantId(otherTenantId))

        {
          val receivedExperiments = probe.expectMsgAnyClassOf(classOf[ExperimentsMap])
          receivedExperiments.experimentsByTenantId should have size 1
          receivedExperiments.experimentsByTenantId(otherTenantId) should
            contain theSameElementsAs expectedExperimentsOfTenant2
        }
      }
    }

    "answer with empty map" when {
      "received GetAllByTenantId with id of tenant that has no experiments" in new TestCase {
        probe.send(actorRef, GetAllByTenantId("tenantWithNoExperiments"))
        probe.expectMsgAnyClassOf(classOf[ExperimentsMap])
          .experimentsByTenantId shouldBe Map.empty
      }
    }
  }
}
