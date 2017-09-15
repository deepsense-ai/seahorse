/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import scala.concurrent.Future

import org.mockito.Matchers._
import org.mockito.Mockito._
import spray.json.{JsString, JsObject}

import io.deepsense.commons.auth.usercontext.{Role, UserContext}
import io.deepsense.commons.auth.{AuthorizatorProvider, UserContextAuthorizator}
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.graph.DeeplangGraph.DeeplangNode
import io.deepsense.graph._
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.storage.{NotebookStorage, WorkflowStateStorage, WorkflowStorage}

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

  val graph = mock[DeeplangGraph]
  when(graph.nodes).thenReturn(Set[DeeplangNode]())
  val metadata = mock[WorkflowMetadata]
  val name = "test name"
  val thirdPartyData = JsObject(
    "gui" -> JsObject("name" -> JsString(name)),
    "notebooks" -> JsObject())
  val storedWorkflow = Workflow(metadata, graph, thirdPartyData)
  val storedWorkflowId = Workflow.Id.randomId
  val storedNodeId = Node.Id.randomId
  val storedWorkflowWithResults = WorkflowWithResults(
    storedWorkflowId,
    metadata,
    graph,
    thirdPartyData,
    ExecutionReport(Map[io.deepsense.graph.Node.Id, NodeState]()))
  val workflowStorage: WorkflowStorage = mock[WorkflowStorage]
  val workflowStateStorage = mock[WorkflowStateStorage]
  val notebookStorage = mock[NotebookStorage]
  val workflowWithResults = WorkflowWithResults(
    Workflow.Id.randomId,
    mock[WorkflowMetadata],
    mock[DeeplangGraph],
    mock[JsObject],
    ExecutionReport(Map[io.deepsense.graph.Node.Id, NodeState]()))

  val workflowManager = new WorkflowManagerImpl(
    authorizatorProvider,
    workflowStorage,
    workflowStateStorage,
    notebookStorage,
    userContextFuture,
    roleForAll,
    roleForAll,
    roleForAll,
    roleForAll)

  "WorkflowManager.get(...)" should {
    "return None" when {
      "the requested workflow does not exist" in {
        reset(workflowStorage)
        when(workflowStorage.get(any()))
          .thenReturn(Future.successful(None))

        when(notebookStorage.get(any(), any()))
          .thenReturn(Future.successful(None))

        val eventualWorkflow = workflowManager.get(Workflow.Id.randomId)
        whenReady(eventualWorkflow) { _ shouldBe None }
      }
    }
    "return workflow from the storage" in {
      reset(workflowStorage)
      reset(workflowStateStorage)
      when(workflowStorage.get(storedWorkflowId))
        .thenReturn(Future.successful(Some(storedWorkflow)))
      when(workflowStateStorage.get(storedWorkflowId))
        .thenReturn(Future.successful(Map[Node.Id, NodeState]()))
      when(notebookStorage.getAll(storedWorkflowId))
        .thenReturn(Future.successful(Map[Node.Id, String]()))

      val eventualWorkflow = workflowManager.get(storedWorkflowId)
      whenReady(eventualWorkflow) { _.get shouldEqual storedWorkflowWithResults }
    }
    "delete workflow from the storage" is pending
    "save workflow in storage" is pending
    "update workflow in storage" in {
      reset(workflowStorage)
      when(workflowStorage.get(storedWorkflowId))
        .thenReturn(Future.successful(Some(storedWorkflow)))
      when(workflowStorage.update(storedWorkflowId, storedWorkflow))
        .thenReturn(Future.successful(()))

      val res = workflowManager.update(storedWorkflowId, storedWorkflow)
      whenReady(res) { _ => () }
      verify(workflowStorage).update(storedWorkflowId, storedWorkflow)
      ()
    }
    "clone workflow" in {
      reset(workflowStorage)
      when(workflowStorage.get(storedWorkflowId))
        .thenReturn(Future.successful(Some(storedWorkflow)))
      when(notebookStorage.getAll(any()))
        .thenReturn(Future.successful(Map[Node.Id, String]()))
      when(workflowStorage.create(any(), any()))
        .thenReturn(Future.successful(()))

      val res = workflowManager.clone(storedWorkflowId)
      whenReady(res) { workflow =>
        workflow shouldBe 'defined
        workflow.get.id should not be storedWorkflowId
        workflow.get.metadata shouldBe storedWorkflow.metadata
        workflow.get.graph shouldBe storedWorkflow.graph

        val workflowName = workflow.get.thirdPartyData.fields("gui").asJsObject.fields("name")
        workflowName shouldBe JsString("Copy of \"" + name + "\"")
      }
    }
    "update StructAndStates in storages" in {
      reset(workflowStorage)
      reset(workflowStateStorage)
      when(workflowStorage.get(storedWorkflowId))
        .thenReturn(Future.successful(Some(storedWorkflow)))
      when(workflowStorage.update(storedWorkflowId, storedWorkflow))
        .thenReturn(Future.successful(()))
      when(
        workflowStateStorage
          .save(storedWorkflowId, storedWorkflowWithResults.executionReport.states))
        .thenReturn(Future.successful(()))

      val res = workflowManager.updateStructAndStates(storedWorkflowId, storedWorkflowWithResults)
      whenReady(res) { _ => () }
      verify(workflowStorage).update(storedWorkflowId, storedWorkflow)
      verify(workflowStateStorage)
        .save(storedWorkflowId, storedWorkflowWithResults.executionReport.states)
      ()
    }
    "update States in storage" in {
      reset(workflowStateStorage)
      when(
        workflowStateStorage
          .save(storedWorkflowId, storedWorkflowWithResults.executionReport.states))
        .thenReturn(Future.successful(()))

      val res = workflowManager
        .updateStates(storedWorkflowId, storedWorkflowWithResults.executionReport)
      whenReady(res) { _ => () }
      verify(workflowStateStorage)
        .save(storedWorkflowId, storedWorkflowWithResults.executionReport.states)
      ()
    }
  }
}
