/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.app

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

import akka.actor.ActorRef
import akka.testkit.TestProbe
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalactic.Equality

import io.deepsense.commons.auth.usercontext.{Role, UserContext}
import io.deepsense.commons.auth.{AuthorizatorProvider, UserContextAuthorizator}
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.models.messages._
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.WorkflowManagerImpl
import io.deepsense.workflowmanager.exceptions.WorkflowNotFoundException
import io.deepsense.workflowmanager.storage.WorkflowStorage

class WorkflowManagerImplSpec extends StandardSpec with UnitTestSupport {
  val tenantId = "tenantId"
  val roleForAll = "aRole"
  val userContext = mock[UserContext]
  when(userContext.tenantId).thenReturn(tenantId)
  when(userContext.roles).thenReturn(Set(Role(roleForAll)))
  val userContextFuture: Future[UserContext] = Future.successful(userContext)

  val authorizator = new UserContextAuthorizator(userContextFuture)
  val authorizatorProvider: AuthorizatorProvider = mock[AuthorizatorProvider]
  when(authorizatorProvider.forContext(any(classOf[Future[UserContext]]))).thenReturn(authorizator)

  implicit val experimentEquality = new Equality[Workflow] {
    def areEqual(a: Workflow, b: Any): Boolean =
      b match {
        case e: Workflow => e eq a
        case _ => false
      }
  }

  val probe = TestProbe()
  val runningExperimentsActor: ActorRef = probe.testActor
  val storedWorkflow = mock[Workflow]
  when(storedWorkflow.assureOwnedBy(any())).thenReturn(storedWorkflow)
  val storage: WorkflowStorage = mock[WorkflowStorage]

  val experimentManager = new WorkflowManagerImpl(
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
        when(storage.get(any(), any()))
          .thenReturn(Future.successful(None))

        val eventualExperiment = experimentManager.get(Workflow.Id.randomId)
        whenReady(eventualExperiment) { _ shouldBe None }
      }
    }
    "return experiment from the storage when the experiment is not running" in {
      when(storage.get(any(), any()))
        .thenReturn(Future.successful(Some(storedWorkflow)))
      val id = Workflow.Id.randomId

      val eventualExperiment = experimentManager.get(id)
      probe.expectMsg(io.deepsense.models.messages.Get(id))
      probe.reply(None)
      whenReady(eventualExperiment) { _.get shouldEqual storedWorkflow }
    }
    "return running experiment" in {
      val runningExperiment = mock[Workflow]
      when(storage.get(any(), any()))
        .thenReturn(Future.successful(Some(storedWorkflow)))

      val eventualExperiment = experimentManager.get(storedWorkflow.id)
      probe.expectMsg(io.deepsense.models.messages.Get(storedWorkflow.id))
      probe.reply(Some(runningExperiment))
      whenReady(eventualExperiment) { _.get shouldEqual runningExperiment }
    }
  }

  "ExperimentManager.launch(...)" should {
    "launch experiment and return it" when {
      "the experiment exists" in {
        val launchedExperiment = mock[Workflow]
        when(storage.get(any(), any())).thenReturn(Future.successful(Some(storedWorkflow)))

        val eventualExperiment = experimentManager.launch(storedWorkflow.id, Seq.empty)
        probe.expectMsg(Launch(storedWorkflow))
        probe.reply(Success(launchedExperiment))
        whenReady(eventualExperiment) { _ shouldEqual launchedExperiment }
      }
    }
    "fail" when {
      "the experiment does not exists" in {
        when(storage.get(any(), any())).thenReturn(Future.successful(None))

        val eventualExperiment = experimentManager.launch(storedWorkflow.id, Seq.empty)
        whenReady(eventualExperiment.failed) {
          _ shouldBe new WorkflowNotFoundException(storedWorkflow.id)
        }
      }
    }
    "fail" when {
      "the experiment is already running" is pending
    }
  }

  "ExperimentManager.abort(...)" should {
    "abort experiment and return it" when {
      "the experiment exists" is pending
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
        when(storage.get(any(), any())).thenReturn(Future.successful(None))
        val eventualExperiment = experimentManager.abort(storedWorkflow.id, Seq.empty)
        whenReady(eventualExperiment.failed) {
          _ shouldBe new WorkflowNotFoundException(storedWorkflow.id)
        }
      }
    }
  }

  "ExperimentManager.list(...)" should {
    "contain running experiments" in {
      val runningExperimentId = Workflow.Id.randomId
      val e1 = mock[Workflow]
      val e2 = mock[Workflow]
      val e3 = mock[Workflow]
      val e4 = mock[Workflow]
      when(e1.id).thenReturn(Workflow.Id.randomId)
      when(e2.id).thenReturn(runningExperimentId)
      when(e3.id).thenReturn(Workflow.Id.randomId)
      when(e4.id).thenReturn(Workflow.Id.randomId)

      val runningExperiment = mock[Workflow]
      when(runningExperiment.id).thenReturn(runningExperimentId)

      val storedExperiments = Seq(e1, e2, e3, e4)
      when(storage.list(any(), any(), any(), any()))
        .thenReturn(Future.successful(storedExperiments))

      val mergedExperiments = experimentManager.experiments(None, None, None)
      probe.expectMsg(GetAllByTenantId(tenantId))
      probe.reply(WorkflowsMap(Map(tenantId -> Set(runningExperiment))))
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
