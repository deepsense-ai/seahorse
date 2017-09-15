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

package ai.deepsense.workflowmanager.storage.impl

import scala.concurrent.{Await, Future}

import com.typesafe.config.{Config, ConfigFactory}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers}
import spray.json.JsObject

import ai.deepsense.commons.StandardSpec
import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang.DOperation
import ai.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import ai.deepsense.graph._
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.workflows._
import ai.deepsense.workflowmanager.rest.CurrentBuild
import ai.deepsense.workflowmanager.storage.GraphJsonTestSupport

class WorkflowDaoImplIntegSpec
  extends StandardSpec
  with ScalaFutures
  with MockitoSugar
  with Matchers
  with BeforeAndAfter
  with GraphJsonTestSupport
  with SlickTestSupport
  with Logging {

  var workflowsDao: WorkflowDaoImpl = _
  val catalog = mock[DOperationsCatalog]
  val graphReader: GraphReader = new GraphReader(catalog)

  val operation1 = mockOperation(0, 1, DOperation.Id.randomId, "name1")
  val operation2 = mockOperation(1, 1, DOperation.Id.randomId, "name2")
  val operation3 = mockOperation(1, 1, DOperation.Id.randomId, "name3")
  val operation4 = mockOperation(2, 1, DOperation.Id.randomId, "name4")

  when(catalog.createDOperation(operation1.id)).thenReturn(operation1)
  when(catalog.createDOperation(operation2.id)).thenReturn(operation2)
  when(catalog.createDOperation(operation3.id)).thenReturn(operation3)
  when(catalog.createDOperation(operation4.id)).thenReturn(operation4)

  val (workflow1Id, workflow1) = createWorkflow(graph = DeeplangGraph())
  val (workflow2Id, workflow2) = createWorkflow(graph = createGraph())
  val (schedulerWorkflowId, schedulerWorkflow) = createWorkflow(graph = DeeplangGraph())

  case class StoredWorkflow(id: Workflow.Id, workflow: Workflow, ownerId: String, ownerName: String)

  val config: Config = ConfigFactory.load()

  val genericUserId = "ownerid"
  val genericUserName = "ownername"
  val schedulerUserId = config.getString("scheduling-manager.user.id")
  val schedulerUserName = config.getString("scheduling-manager.user.name")

  val storedWorkflows = Set(
    StoredWorkflow(workflow1Id, workflow1, genericUserId, genericUserName),
    StoredWorkflow(workflow2Id, workflow2, genericUserId, genericUserName),
    StoredWorkflow(schedulerWorkflowId, schedulerWorkflow, schedulerUserId, schedulerUserName)
  )

  before {
    workflowsDao = WorkflowDaoImpl(db, driver, graphReader, schedulerUserId)
  }

  "WorkflowsDao" should {

    "not get workflow" when {
      "workflow is deleted" in {
        withStoredWorkflows(storedWorkflows) {
          whenReady(workflowsDao.delete(workflow2Id)) { _ =>
            whenReady(workflowsDao.get(workflow2Id)) { workflow =>
              workflow shouldBe None
            }
          }
        }
      }

      "workflow belongs to scheduler user" in {
        withStoredWorkflows(storedWorkflows) {
          whenReady(workflowsDao.getAll) { workflows =>
            workflows.keySet should not contain schedulerWorkflowId
          }
        }
      }
    }

    "update workflow" in withStoredWorkflows(storedWorkflows) {
      val modifiedWorkflow2 = workflow2.copy(additionalData = JsObject())
      whenReady(workflowsDao.update(workflow2Id, modifiedWorkflow2)) { _ =>
        whenReady(workflowsDao.get(workflow2Id)) { workflow =>
          workflow.get.workflow shouldBe modifiedWorkflow2
        }
      }
    }

    "find workflow by id" in withStoredWorkflows(storedWorkflows) {
      whenReady(workflowsDao.get(workflow1Id)) { workflow =>
        workflow.get.workflow shouldBe workflow1
      }
    }

    "get all workflows" in withStoredWorkflows(storedWorkflows) {
      whenReady(workflowsDao.getAll) { workflows =>
        workflows.size shouldBe 2
        workflows(workflow1Id).workflow shouldBe workflow1
        workflows(workflow2Id).workflow shouldBe workflow2
      }
    }
  }

  private def withStoredWorkflows(
      storedWorkflows: Set[StoredWorkflow])(testCode: => Any): Unit = {

    Await.ready(workflowsDao.create(), operationDuration)

    val s = Future.sequence(storedWorkflows.map {
      case StoredWorkflow(id, workflow, ownerId, ownerName) => workflowsDao.create(id, workflow, ownerId, ownerName)
    })
    Await.ready(s, operationDuration)

    try {
      testCode
    } finally {
      Await.ready(workflowsDao.drop(), operationDuration)
    }
  }

  def createWorkflow(graph: DeeplangGraph): (Workflow.Id, Workflow) = {
    val metadata = WorkflowMetadata(
      apiVersion = CurrentBuild.version.humanReadable,
      workflowType = WorkflowType.Batch)
    val thirdPartyData = JsObject()
    (Workflow.Id.randomId, Workflow(metadata, graph, thirdPartyData))
  }

  def createGraph() : DeeplangGraph = {
    val node1 = Node(Node.Id.randomId, operation1)
    val node2 = Node(Node.Id.randomId, operation2)
    val node3 = Node(Node.Id.randomId, operation3)
    val node4 = Node(Node.Id.randomId, operation4)
    val nodes = Set(node1, node2, node3, node4)
    val edgesList = List(
      (node1, node2, 0, 0),
      (node1, node3, 0, 0),
      (node2, node4, 0, 0),
      (node3, node4, 0, 1))
    val edges = edgesList.map(n => Edge(Endpoint(n._1.id, n._3), Endpoint(n._2.id, n._4))).toSet
    DeeplangGraph(nodes, edges)
  }
}
