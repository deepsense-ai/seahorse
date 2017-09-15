/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import scala.concurrent.Future

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, Mockito}

import io.deepsense.commons.auth.usercontext.{Role, UserContext}
import io.deepsense.commons.auth.{AuthorizatorProvider, UserContextAuthorizator}
import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph.{Graph, GraphKnowledge, Status}
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.storage.{WorkflowResultsStorage, WorkflowStorage}

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

  val inferContext: InferContext = mock[InferContext]
  val graph = mock[Graph]
  val knowledge = mock[GraphKnowledge]
  when(graph.inferKnowledge(inferContext)).thenReturn(knowledge)
  val metadata = mock[WorkflowMetadata]
  val thirdPartyData = mock[ThirdPartyData]
  val storedWorkflow = Workflow(metadata, graph, thirdPartyData)
  val storedWorkflowId = Workflow.Id.randomId
  val storedWorkflowWithKnowledge = WorkflowWithKnowledge(
    storedWorkflowId, metadata, graph, thirdPartyData, knowledge)
  val workflowStorage: WorkflowStorage = mock[WorkflowStorage]
  val workflowResultStorage = mock[WorkflowResultsStorage]
  val workflowWithResults = WorkflowWithResults(
    Workflow.Id.randomId,
    mock[WorkflowMetadata],
    mock[Graph],
    mock[ThirdPartyData],
    ExecutionReport(
      Status.Aborted,
      DateTimeConverter.now,
      DateTimeConverter.now,
      None,
      Map[io.deepsense.graph.Node.Id, io.deepsense.graph.State](),
      EntitiesMap()
    )
  )
  Mockito.when(workflowStorage.saveExecutionResults(any())).thenReturn(Future.successful(()))
  Mockito.when(workflowResultStorage.save(any())).thenReturn(Future.successful(()))

  val workflowManager = new WorkflowManagerImpl(
    authorizatorProvider,
    workflowStorage,
    workflowResultStorage,
    inferContext,
    userContextFuture,
    roleForAll,
    roleForAll,
    roleForAll,
    roleForAll)

  "WorkflowManager.get(...)" should {
    "return None" when {
      "the requested workflow does not exist" in {
        when(workflowStorage.get(any()))
          .thenReturn(Future.successful(None))

        val eventualWorkflow = workflowManager.get(Workflow.Id.randomId)
        whenReady(eventualWorkflow) { _ shouldBe None }
      }
    }
    "return workflow from the storage" in {
      when(workflowStorage.get(storedWorkflowId))
        .thenReturn(Future.successful(Some(storedWorkflow)))

      val eventualWorkflow = workflowManager.get(storedWorkflowId)
      whenReady(eventualWorkflow) { _.get shouldEqual storedWorkflowWithKnowledge }
    }
    "delete workflow from the storage" is pending
    "save workflow in storage" is pending
  }

  "WorkflowManager" should {
    "generate id and store result in two tables" in {
      whenReady(workflowManager.saveWorkflowResults(workflowWithResults)) { results =>
        val resultId = results.executionReport.id
        val captor1 = ArgumentCaptor.forClass(classOf[WorkflowWithSavedResults])
        val captor2 = ArgumentCaptor.forClass(classOf[WorkflowWithSavedResults])
        verify(workflowStorage, times(1)).saveExecutionResults(captor1.capture())
        verify(workflowResultStorage, times(1)).save(captor2.capture())
        captor1.getValue.executionReport.id shouldBe resultId
        captor2.getValue.executionReport.id shouldBe resultId
      }
    }
  }
}
