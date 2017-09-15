/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager

import java.util.UUID

import scala.concurrent.Future
import scala.concurrent.duration._

import akka.actor.ActorRef
import akka.testkit.TestProbe
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalactic.Equality

import io.deepsense.commons.auth.usercontext.{Role, UserContext}
import io.deepsense.commons.auth.{AuthorizatorProvider, UserContextAuthorizator}
import io.deepsense.commons.models.Id
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.experimentmanager.exceptions.ExperimentNotFoundException
import io.deepsense.experimentmanager.execution.RunningExperimentsActor._
import io.deepsense.experimentmanager.storage.ExperimentStorage
import io.deepsense.models.experiments.Experiment

class ExperimentManagerImplSpec extends StandardSpec with UnitTestSupport {
  val tenantId = "tenantId"
  val roleForAll = "aRole"
  val userContext = mock[UserContext]
  when(userContext.tenantId).thenReturn(tenantId)
  when(userContext.roles).thenReturn(Set(Role(roleForAll)))
  val userContextFuture: Future[UserContext] = Future.successful(userContext)

  val authorizator = new UserContextAuthorizator(userContextFuture)
  val authorizatorProvider: AuthorizatorProvider = mock[AuthorizatorProvider]
  when(authorizatorProvider.forContext(any(classOf[Future[UserContext]]))).thenReturn(authorizator)

  implicit val experimentEquality = new Equality[Experiment] {
    def areEqual(a: Experiment, b: Any): Boolean =
      b match {
        case e: Experiment => e eq a
        case _ => false
      }
  }

  val probe = TestProbe()
  val runningExperimentsActor: ActorRef = probe.testActor
  val storedExperiment = mock[Experiment]
  when(storedExperiment.assureOwnedBy(any())).thenReturn(storedExperiment)
  val storage: ExperimentStorage = mock[ExperimentStorage]

  val experimentManager = new ExperimentManagerImpl(
    authorizatorProvider,
    storage,
    userContextFuture,
    roleForAll,
    roleForAll,
    roleForAll,
    roleForAll,
    roleForAll,
    roleForAll,
    roleForAll,
    runningExperimentsActor,
    3.seconds.toMillis)

  "ExperimentManager.get(...)" should {
    "return None" when {
      "the requested experiment does not exist" in {
        when(storage.get(any()))
          .thenReturn(Future.successful(None))

        val eventualExperiment = experimentManager.get(Id(UUID.randomUUID()))
        whenReady(eventualExperiment) { _ shouldBe None }
      }
    }
    "return experiment from the storage when the experiment is not running" in {
      when(storage.get(any()))
        .thenReturn(Future.successful(Some(storedExperiment)))
      val id = UUID.randomUUID()

      val eventualExperiment = experimentManager.get(id)
      probe.expectMsg(GetStatus(id))
      probe.reply(Status(None))
      whenReady(eventualExperiment) { _.get shouldEqual storedExperiment }
    }
    "return running experiment" in {
      val runningExperiment = mock[Experiment]
      when(storage.get(any()))
        .thenReturn(Future.successful(Some(storedExperiment)))

      val eventualExperiment = experimentManager.get(storedExperiment.id)
      probe.expectMsg(GetStatus(storedExperiment.id))
      probe.reply(Status(Some(runningExperiment)))
      whenReady(eventualExperiment) { _.get shouldEqual runningExperiment }
    }
  }

  "ExperimentManager.launch(...)" should {
    "launch experiment and return it" when {
      "the experiment exists" in {
        val launchedExperiment = mock[Experiment]
        when(storage.get(any()))
          .thenReturn(Future.successful(Some(storedExperiment)))

        val eventualExperiment = experimentManager.launch(storedExperiment.id, List())
        probe.expectMsg(Launch(storedExperiment))
        probe.reply(Status(Some(launchedExperiment)))
        whenReady(eventualExperiment) { _ shouldEqual launchedExperiment }
      }
    }
    "return fail" when {
      "the experiment does not exists" in {
        when(storage.get(any()))
          .thenReturn(Future.successful(None))

        val eventualExperiment = experimentManager.launch(storedExperiment.id, List())
        whenReady(eventualExperiment.failed) {
          _ shouldBe new ExperimentNotFoundException(storedExperiment.id)
        }
      }
    }
  }

  "ExperimentManager.abort(...)" should {
    "abort experiment and return it" when {
      "the experiment exists" in pending
//        {
//        val abortedExperiment = mock[Experiment]
//        val storage: ExperimentStorage = mock[ExperimentStorage]
//        when(storage.get(any()))
//          .thenReturn(Future.successful(Some(storedExperiment)))
//        val experimentManager = createExperimentManagerImpl(storage)
//
//        val eventualExperiment = experimentManager.abort(storedExperiment.id, List())
//        probe.expectMsg(Abort(storedExperiment.id))
//        probe.reply(Status(Some(abortedExperiment)))
//        whenReady(eventualExperiment) { _ shouldEqual abortedExperiment }
//      }
    }
    "return fail" when {
      "the experiment does not exists" in {
        when(storage.get(any()))
          .thenReturn(Future.successful(None))
        val eventualExperiment = experimentManager.abort(storedExperiment.id, List())
        whenReady(eventualExperiment.failed) {
          _ shouldBe new ExperimentNotFoundException(storedExperiment.id)
        }
      }
    }
  }

  "ExperimentManager.list(...)" should {
    "contain running experiments" in {
      val runningExperimentId = Id(UUID.randomUUID())
      val e1 = mock[Experiment]
      val e2 = mock[Experiment]
      val e3 = mock[Experiment]
      val e4 = mock[Experiment]
      when(e1.id).thenReturn(UUID.randomUUID())
      when(e2.id).thenReturn(runningExperimentId)
      when(e3.id).thenReturn(UUID.randomUUID())
      when(e4.id).thenReturn(UUID.randomUUID())

      val runningExperiment = mock[Experiment]
      when(runningExperiment.id).thenReturn(runningExperimentId)

      val storedExperiments = List(e1, e2, e3, e4)
      when(storage.list(any(), any(), any(), any()))
        .thenReturn(Future.successful(storedExperiments))

      val mergedExperiments = experimentManager.experiments(None, None, None)
      probe.expectMsg(ListExperiments(Some(tenantId)))
      probe.reply(Experiments(Map(tenantId -> Set(runningExperiment))))
      whenReady(mergedExperiments) { experimentsLists =>
        val experiments = experimentsLists.experiments
        val experimentsById = experiments.map( experiment => experiment.id -> experiment).toMap
        experimentsById(e1.id) shouldBe e1
        experimentsById(e2.id) shouldBe runningExperiment
        experimentsById(e3.id) shouldBe e3
        experimentsById(e4.id) shouldBe e4
        experiments should have size 4
      }
    }
  }
}
