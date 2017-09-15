/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.execution

import java.util.UUID

import scala.util.{Failure, Success}

import akka.actor.{ActorRef, ActorRefFactory, Props}
import akka.testkit.{TestActorRef, TestProbe}
import org.scalatest.concurrent.{Eventually, ScaledTimeSpans}
import org.scalatest.{BeforeAndAfter, WordSpecLike}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.FailureCode.UnexpectedError
import io.deepsense.commons.exception.{DeepSenseFailure, FailureDescription}
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.doperations.LoadDataFrame
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphexecutor.SimpleGraphExecutionIntegSuiteEntities
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.messages.{Delete => READelete, Get => REAGet, _}

class RunningExperimentsActorSpec
  extends StandardSpec
  with UnitTestSupport
  with WordSpecLike
  with BeforeAndAfter
  with Eventually
  with ScaledTimeSpans {

  type TestCode =
    (TestActorRef[RunningExperimentsActor], TestProbe, RunningExperimentsActor, Experiment) => Any

  val created = DateTimeConverter.now
  val updated = created.plusHours(2)
  val emptyExperiment = Experiment(Experiment.Id.randomId,
    "B", "Experiment", Graph(), created, updated)
  val failedExperiment = Experiment(
    Experiment.Id.randomId,
    "B",
    "Experiment",
    Graph(nodes = Set(Node(Node.Id.randomId, LoadDataFrame("dataframe_id")).markFailed(
      FailureDescription(
        DeepSenseFailure.Id.randomId,
        UnexpectedError,
        "Something went wrong")))),
    created,
    updated)
  val oneNodeExperiment = Experiment(Experiment.Id.randomId, "B", "Experiment",
    Graph(nodes = Set(Node(Node.Id.randomId, LoadDataFrame(UUID.randomUUID().toString)))),
    created, updated)
  val runningExperiment = Experiment(Experiment.Id.randomId, "B", "Experiment",
    Graph(nodes = Set(Node(Node.Id.randomId, LoadDataFrame(UUID.randomUUID().toString)))),
    created, updated).markRunning

  private def withLaunchedExperiments(
    experimentsToLaunch: Set[Experiment],
    experimentForGEC: Experiment = emptyExperiment)(testCode: TestCode): Unit = {
    withExperiment(experimentForGEC) {
      (actorRef, probe, actor, experiment) => {
        experimentsToLaunch.foreach(e => probe.send(actorRef, Launch(e)))
        probe.receiveN(experimentsToLaunch.size * 2)
        testCode(actorRef, probe, actor, experiment)
      }
    }
  }

  private def withExperiment(experiment: Experiment)(testCode: TestCode): Unit = {
    val probe = TestProbe()
    val gecMaker: (ActorRefFactory, String, String) => ActorRef = {
      (f, entitystorageLabel, experimentId) =>
        probe.ref
    }
    val rea = TestActorRef[RunningExperimentsActor](Props(new RunningExperimentsActor(
      SimpleGraphExecutionIntegSuiteEntities.Name,
      3000L,
      gecMaker)))
    val actor = rea.underlyingActor
    testCode(rea, probe, actor, experiment)
  }

  "RunningExperimentsActor" should {

    "launch experiment and respond Success" when {
      "received Launch on experiment" in {
        withExperiment(oneNodeExperiment) { (actorRef, probe, actor, exp) =>
          probe.send(actorRef, Launch(exp))
          probe.expectMsg(Launch(exp))

          val runningExp = exp.markRunning
          probe.expectMsg(Success(runningExp))

          actor.experiments should contain key exp.id
          probe.send(actorRef, REAGet(exp.id))
          probe.expectMsg(Some(runningExp))
        }
      }
    }

    "refuse to launch experiment" when {
      "received Launch with already-running experiment" in {
        withExperiment(oneNodeExperiment) { (actorRef, probe, actor, exp) =>
          probe.send(actorRef, Launch(exp))
          probe.expectMsgType[Launch]
          probe.expectMsgType[Success[_]]

          probe.send(actorRef, Launch(exp))
          probe.expectMsgType[Failure[_]]
        }
      }
    }

    "refuse to delete experiment" when {
      "it's running" in {
        withExperiment(oneNodeExperiment) { (actorRef, probe, actor, exp) =>
          probe.send(actorRef, Launch(exp))
          probe.expectMsgType[Launch]
          probe.expectMsgType[Success[_]]

          probe.send(actorRef, READelete(exp.id))
          actor.experiments should contain key exp.id
        }
      }
    }

    "report Failure" when {
      "aborting non-existent experiment" in {
        withExperiment(oneNodeExperiment) { (actorRef, probe, actor, exp) =>
          actor.experiments shouldNot contain key exp.id
          probe.send(actorRef, Abort(exp.id))
          probe.expectMsgType[Failure[_]]
        }
      }
    }

    "abort running experiment" in {
      withExperiment(oneNodeExperiment) { (actorRef, probe, actor, exp) =>
        actor.experiments shouldNot contain key exp.id
        probe.send(actorRef, Launch(exp))
        probe.expectMsgType[Launch]
        probe.expectMsgType[Success[_]]

        probe.send(actorRef, Abort(exp.id))
        probe.expectMsg(Abort(exp.id))
        probe.expectMsgType[Success[_]]
      }
    }

    "refuse to abort" when {
      "experiment is not running" in {
        withExperiment(oneNodeExperiment) { (actorRef, probe, actor, exp) =>
          probe.send(actorRef, Launch(exp))
          probe.expectMsgType[Launch]
          probe.expectMsgType[Success[_]]

          val completed = exp.markCompleted
          probe.send(actorRef, Update(completed))
          actor.experiments(exp.id)._1 shouldBe completed

          probe.send(actorRef, Abort(exp.id))
          probe.expectMsgType[Failure[_]]
        }
      }
    }

    "list experiments" when {
      val tenantId = oneNodeExperiment.tenantId
      val experiment1 = oneNodeExperiment.copy(id = UUID.randomUUID(), description = "1")
      val experiment2 = oneNodeExperiment.copy(id = UUID.randomUUID(), description = "2")
      val experiment3 = oneNodeExperiment.copy(id = UUID.randomUUID(), description = "3")
      val otherTenantId = tenantId + "other"
      val experiment4 = oneNodeExperiment.copy(
        id = UUID.randomUUID(),
        tenantId = otherTenantId,
        description = "4")
      val experiments = Set(experiment1, experiment2, experiment3, experiment4)
      val expectedExperimentsOfTenant1 =
        Map(tenantId -> Set(experiment1, experiment2, experiment3).map(_.markRunning))
      val expectedExperimentsOfTenant2 = Set(experiment4.markRunning)

      "received GetAllByTenantId with id of tenant that has experiments" in {
        withLaunchedExperiments(experiments) {
          (actorRef, probe, actor, exp) => {
            probe.send(actorRef, GetAllByTenantId(Some(experiment1.tenantId)))

            {
              val receivedExperiments = probe.expectMsgAnyClassOf(classOf[ExperimentsMap])
              receivedExperiments.experimentsByTenantId should have size 1
              receivedExperiments.experimentsByTenantId(experiment1.tenantId) should
                contain theSameElementsAs expectedExperimentsOfTenant1(experiment1.tenantId)
            }

            probe.send(actorRef, GetAllByTenantId(Some(otherTenantId)))

            {
              val receivedExperiments = probe.expectMsgAnyClassOf(classOf[ExperimentsMap])
              receivedExperiments.experimentsByTenantId should have size 1
              receivedExperiments.experimentsByTenantId(otherTenantId) should
                contain theSameElementsAs expectedExperimentsOfTenant2
            }
          }
        }
      }
    }

    "answer with empty map" when {
      "received GetAllByTenantId with id of tenant that has no experiments" in {
        withExperiment(emptyExperiment) {
          (actorRef, probe, actor, exp) => {
            probe.send(actorRef, GetAllByTenantId(Some("tenantWithNoExperiments")))
            probe.expectMsgAnyClassOf(classOf[ExperimentsMap])
              .experimentsByTenantId shouldBe Map.empty
          }
        }
      }
    }
  }
}
