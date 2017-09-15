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

  implicit val workflowEquality = new Equality[Workflow] {
    def areEqual(a: Workflow, b: Any): Boolean =
      b match {
        case e: Workflow => e eq a
        case _ => false
      }
  }

  val probe = TestProbe()
  val runningWorkflowssActor: ActorRef = probe.testActor
  val storedWorkflow = mock[Workflow]
  when(storedWorkflow.assureOwnedBy(any())).thenReturn(storedWorkflow)
  val storage: WorkflowStorage = mock[WorkflowStorage]

  val workflowManager = new WorkflowManagerImpl(
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
    runningWorkflowssActor,
    3.seconds.toMillis)

  "WorkflowManager.get(...)" should {
    "return None" when {
      "the requested workflow does not exist" in {
        when(storage.get(any(), any()))
          .thenReturn(Future.successful(None))

        val eventualWorkflow = workflowManager.get(Workflow.Id.randomId)
        whenReady(eventualWorkflow) { _ shouldBe None }
      }
    }
    "return workflow from the storage when the workflow is not running" in {
      when(storage.get(any(), any()))
        .thenReturn(Future.successful(Some(storedWorkflow)))
      val id = Workflow.Id.randomId

      val eventualWorkflow = workflowManager.get(id)
      probe.expectMsg(io.deepsense.models.messages.Get(id))
      probe.reply(None)
      whenReady(eventualWorkflow) { _.get shouldEqual storedWorkflow }
    }
    "return running workflow" in {
      val runningWorkflow = mock[Workflow]
      when(storage.get(any(), any()))
        .thenReturn(Future.successful(Some(storedWorkflow)))

      val eventualWorkflow = workflowManager.get(storedWorkflow.id)
      probe.expectMsg(io.deepsense.models.messages.Get(storedWorkflow.id))
      probe.reply(Some(runningWorkflow))
      whenReady(eventualWorkflow) { _.get shouldEqual runningWorkflow }
    }
  }

  "WorkflowManager.launch(...)" should {
    "launch workflow and return it" when {
      "the workflow exists" in {
        val launchedWorkflow = mock[Workflow]
        when(storage.get(any(), any())).thenReturn(Future.successful(Some(storedWorkflow)))

        val eventualWorkflow = workflowManager.launch(storedWorkflow.id, Seq.empty)
        probe.expectMsg(Launch(storedWorkflow))
        probe.reply(Success(launchedWorkflow))
        whenReady(eventualWorkflow) { _ shouldEqual launchedWorkflow }
      }
    }
    "fail" when {
      "the workflow does not exists" in {
        when(storage.get(any(), any())).thenReturn(Future.successful(None))

        val eventualWorkflow = workflowManager.launch(storedWorkflow.id, Seq.empty)
        whenReady(eventualWorkflow.failed) {
          _ shouldBe new WorkflowNotFoundException(storedWorkflow.id)
        }
      }
    }
    "fail" when {
      "the workflow is already running" is pending
    }
  }

  "WorkflowManager.abort(...)" should {
    "abort workflow and return it" when {
      "the workflow exists" is pending
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
      "the workflow does not exists" in {
        when(storage.get(any(), any())).thenReturn(Future.successful(None))
        val eventualWorkflow = workflowManager.abort(storedWorkflow.id, Seq.empty)
        whenReady(eventualWorkflow.failed) {
          _ shouldBe new WorkflowNotFoundException(storedWorkflow.id)
        }
      }
    }
  }

  "WorkflowManager.list(...)" should {
    "contain running workflows" in {
      val runningWorkflowId = Workflow.Id.randomId
      val e1 = mock[Workflow]
      val e2 = mock[Workflow]
      val e3 = mock[Workflow]
      val e4 = mock[Workflow]
      when(e1.id).thenReturn(Workflow.Id.randomId)
      when(e2.id).thenReturn(runningWorkflowId)
      when(e3.id).thenReturn(Workflow.Id.randomId)
      when(e4.id).thenReturn(Workflow.Id.randomId)

      val runningWorkflow = mock[Workflow]
      when(runningWorkflow.id).thenReturn(runningWorkflowId)

      val storedWorkflows = Seq(e1, e2, e3, e4)
      when(storage.list(any(), any(), any(), any()))
        .thenReturn(Future.successful(storedWorkflows))

      val mergedWorkflows = workflowManager.workflows(None, None, None)
      probe.expectMsg(GetAllByTenantId(tenantId))
      probe.reply(WorkflowsMap(Map(tenantId -> Set(runningWorkflow))))
      whenReady(mergedWorkflows) { workflowsLists =>
        val workflows = workflowsLists.workflows
        val workflowsById = workflows.map( workflow => workflow.id -> workflow).toMap
        workflowsById(e1.id) shouldBe e1
        workflowsById(e2.id) shouldBe runningWorkflow
        workflowsById(e3.id) shouldBe e3
        workflowsById(e4.id) shouldBe e4
        workflows should have size 4
      }
    }
  }
}
