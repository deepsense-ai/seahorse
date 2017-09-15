/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar.mock

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.auth.exceptions.NoRoleException
import io.deepsense.commons.auth.usercontext.UserContext
import io.deepsense.commons.auth.{Authorizator, AuthorizatorProvider}
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.model.WorkflowPreset
import io.deepsense.workflowmanager.storage.WorkflowStorage
import io.deepsense.workflowmanager.storage.impl.PresetsDao

class PresetServiceSpec extends StandardSpec
{
  "saveWorkflowsPreset" should {
    "throw NoRoleException" in {
      val presetStore = mock[PresetsDao]
      val workflowStorage = mock[WorkflowStorage]
      val authorizatorProvider = mock[AuthorizatorProvider]
      val authorizator = mock[Authorizator]
      when(authorizatorProvider.forContext(any())).thenReturn(authorizator)
      val userContext = mock[UserContext]
      val badRoleException = new NoRoleException(userContext, "any")
      when(authorizator.withRole(any())(any())).thenReturn(Future.failed(badRoleException))


      val userContextFuture = Future.successful(mock[UserContext])
      val presetService = new PresetService(presetStore, workflowStorage,
        authorizatorProvider, "any")
      val workflowId = Workflow.Id.randomId
      val wp = new WorkflowPreset(workflowId, 2L)

      val f = presetService.saveWorkflowsPreset(userContextFuture, workflowId, wp)

      whenReady(f.failed) { ex =>
        ex shouldBe an[NoRoleException]
      }
    }
  }
}
