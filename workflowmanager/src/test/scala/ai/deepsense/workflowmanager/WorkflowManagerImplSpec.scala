/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.workflowmanager

import java.util.UUID

import scala.concurrent.Future

import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.{Seconds, Span}
import spray.json.{JsArray, JsObject, JsString, JsNull}

import ai.deepsense.commons.auth.usercontext.{Role, User, UserContext}
import ai.deepsense.commons.auth.{AuthorizatorProvider, UserContextAuthorizator}
import ai.deepsense.commons.{StandardSpec, UnitTestSupport}
import ai.deepsense.graph.DeeplangGraph.DeeplangNode
import ai.deepsense.graph._
import ai.deepsense.models.workflows._
import ai.deepsense.workflowmanager.model.WorkflowDescription
import ai.deepsense.workflowmanager.rest.CurrentBuild
import ai.deepsense.workflowmanager.storage._

class WorkflowManagerImplSpec extends StandardSpec with UnitTestSupport {
  val tenantId = "tenantId"
  val roleForAll = "aRole"
  val ownerId = "ownerId"
  val ownerName = "ownerName"
  val fakeDatasourcesServerAddress = "http://mockedHttpAddress/"
  val userContext = mock[UserContext]
  when(userContext.tenantId).thenReturn(tenantId)
  when(userContext.roles).thenReturn(Set(Role(roleForAll)))
  when(userContext.user).thenReturn(User(
    id = ownerId,
    name = ownerName,
    email = None,
    enabled = Some(true),
    tenantId = None))
  val userContextFuture: Future[UserContext] = Future.successful(userContext)

  val authorizator = new UserContextAuthorizator(userContextFuture)
  val authorizatorProvider: AuthorizatorProvider = mock[AuthorizatorProvider]
  when(authorizatorProvider.forContext(any(classOf[Future[UserContext]]))).thenReturn(authorizator)

  val graph = mock[DeeplangGraph]
  when(graph.nodes).thenReturn(Set[DeeplangNode]())
  when(graph.getDatasourcesIds).thenReturn(Set[UUID]())
  val metadata = WorkflowMetadata(
    workflowType = WorkflowType.Batch,
    apiVersion = CurrentBuild.version.humanReadable)
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
    ExecutionReport(Map[ai.deepsense.graph.Node.Id, NodeState]()),
    WorkflowInfo.forId(storedWorkflowId))
  val workflowStorage: WorkflowStorage = mock[WorkflowStorage]
  val workflowStateStorage = mock[WorkflowStateStorage]
  val notebookStorage = mock[NotebookStorage]
  val workflowId = Workflow.Id.randomId
  val workflowWithResults = WorkflowWithResults(
    workflowId,
    mock[WorkflowMetadata],
    mock[DeeplangGraph],
    mock[JsObject],
    ExecutionReport(Map[ai.deepsense.graph.Node.Id, NodeState]()),
    WorkflowInfo.forId(workflowId))


  val storedWorkflowFullInfo = WorkflowFullInfo(
    storedWorkflow, DateTime.now, DateTime.now, ownerId, ownerName)

  val workflowManager = new WorkflowManagerImpl(
    authorizatorProvider,
    workflowStorage,
    workflowStateStorage,
    notebookStorage,
    userContextFuture,
    fakeDatasourcesServerAddress,
    roleForAll,
    roleForAll,
    roleForAll,
    roleForAll)

  "WorkflowManager" should {
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
        .thenReturn(Future.successful(Some(storedWorkflowFullInfo)))
      when(workflowStateStorage.get(storedWorkflowId))
        .thenReturn(Future.successful(Map[Node.Id, NodeState]()))
      when(notebookStorage.getAll(storedWorkflowId))
        .thenReturn(Future.successful(Map[Node.Id, String]()))

      val eventualWorkflow = workflowManager.get(storedWorkflowId)
      whenReady(eventualWorkflow) { w =>
        val withIgnoredInfo = w.get.copy(workflowInfo = storedWorkflowWithResults.workflowInfo)
        withIgnoredInfo shouldEqual storedWorkflowWithResults
      }
    }
    "delete workflow from the storage" is pending
    "save workflow in storage" is pending
    "update workflow in storage" in {
      reset(workflowStorage)
      when(workflowStorage.get(storedWorkflowId))
        .thenReturn(Future.successful(Some(storedWorkflowFullInfo)))
      when(workflowStorage.update(storedWorkflowId, storedWorkflow))
        .thenReturn(Future.successful(()))

      val res = workflowManager.update(storedWorkflowId, storedWorkflow)
      whenReady(res) { _ => () }
      verify(workflowStorage).update(storedWorkflowId, storedWorkflow)
      ()
    }
    "download workflow without datasource" in {
      val res = workflowManager.download(storedWorkflowId, exportDatasources = false)
      whenReady(res, timeout = PatienceConfiguration.Timeout(Span(2, Seconds))) { workflow =>
        workflow.get.thirdPartyData.fields.get("datasources").get shouldBe JsNull
      }
    }

    "download workflow with datasource" in {
      val res = workflowManager.download(storedWorkflowId, exportDatasources = true)
      whenReady(res, timeout = PatienceConfiguration.Timeout(Span(2, Seconds))) { workflow =>
        workflow.get.thirdPartyData.fields.get("datasources").get shouldBe JsArray()
      }
    }

    "clone workflow" in {
      reset(workflowStorage)
      when(workflowStorage.get(storedWorkflowId))
        .thenReturn(Future.successful(Some(storedWorkflowFullInfo)))
      when(notebookStorage.getAll(any()))
        .thenReturn(Future.successful(Map[Node.Id, String]()))
      when(workflowStorage.create(any(), any(), any(), any()))
        .thenReturn(Future.successful(()))
      val workflowDescription = WorkflowDescription("Brand new name", "Brand new desc")

      val res = workflowManager.clone(storedWorkflowId, workflowDescription)
      whenReady(res, timeout = PatienceConfiguration.Timeout(Span(2, Seconds))) { workflow =>
        workflow shouldBe 'defined
        workflow.get.id should not be storedWorkflowId
        workflow.get.metadata shouldBe storedWorkflow.metadata
        workflow.get.graph shouldBe storedWorkflow.graph

        val guiFields = workflow.get.thirdPartyData.fields("gui").asJsObject.fields
        val workflowName = guiFields("name")
        val workflowDesc = guiFields("description")
        workflowName shouldBe JsString(workflowDescription.name)
        workflowDesc shouldBe JsString(workflowDescription.description)
      }
    }
    "update StructAndStates in storages" in {
      reset(workflowStorage)
      reset(workflowStateStorage)
      when(workflowStorage.get(storedWorkflowId))
        .thenReturn(Future.successful(Some(storedWorkflowFullInfo)))
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
    "copy notebooks" in {
      val workflowId = Workflow.Id.randomId
      val sourceNodeId = Node.Id.randomId
      val destinationNodeId = Node.Id.randomId
      val notebookContent = "cool notebook"
      when(notebookStorage.get(workflowId, sourceNodeId))
        .thenReturn(Future.successful(Some(notebookContent)))

      val res = workflowManager.copyNotebook(workflowId, sourceNodeId, destinationNodeId)
      whenReady(res) { _ => () }
      verify(notebookStorage).save(workflowId, destinationNodeId, notebookContent)
    }
  }
}
