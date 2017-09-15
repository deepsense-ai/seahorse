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

import org.apache.commons.lang3.RandomStringUtils
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers}

import ai.deepsense.commons.StandardSpec
import ai.deepsense.commons.utils.Logging
import ai.deepsense.graph.Node
import ai.deepsense.models.workflows.Workflow
import ai.deepsense.workflowmanager.storage.GraphJsonTestSupport

class NotebookDaoImplIntegSpec
  extends StandardSpec
  with ScalaFutures
  with MockitoSugar
  with Matchers
  with BeforeAndAfter
  with GraphJsonTestSupport
  with SlickTestSupport
  with Logging {

  var notebooksDao: NotebookDaoImpl = _

  val n1@(notebook1Id, node1Id, notebook1) = createNotebook()
  val n2@(notebook2Id, node2Id, notebook2) = createNotebook()
  val n3@(notebook3Id, node3Id, notebook3) = createNotebook()
  val n4 = (notebook1Id, node2Id, notebook2)

  val storedNotebooks = Set(n1, n2, n4)

  before {
    notebooksDao = new NotebookDaoImpl(db, driver)
  }

  "NotebooksDao" should {

    "find notebook by id" in withStoredNotebooks(storedNotebooks) {
      whenReady(notebooksDao.get(notebook1Id, node1Id)) { notebook =>
        notebook shouldBe Some(notebook1)
      }
    }

    "get all notebooks for workflow" in withStoredNotebooks(storedNotebooks) {
      whenReady(notebooksDao.getAll(notebook1Id)) { notebooks =>
        notebooks.size shouldBe 2
        notebooks.get(node1Id) shouldBe Some(notebook1)
        notebooks.get(node2Id) shouldBe Some(notebook2)
      }
    }

    "return None if notebook does not exist" in withStoredNotebooks(storedNotebooks) {
      whenReady(notebooksDao.get(notebook3Id, node3Id)) { notebook =>
        notebook shouldBe None
      }
    }

    "create notebook" in withStoredNotebooks(storedNotebooks) {
      whenReady(notebooksDao.save(notebook3Id, node3Id, notebook3)) { _ =>
        whenReady(notebooksDao.get(notebook3Id, node3Id)) { notebook =>
          notebook shouldBe Some(notebook3)
        }
      }
    }

    "update notebook" in withStoredNotebooks(storedNotebooks) {
      val modifiedNotebook2 = "modified"
      whenReady(notebooksDao.save(notebook2Id, node2Id, modifiedNotebook2)) { _ =>
        whenReady(notebooksDao.get(notebook2Id, node2Id)) { notebook =>
          notebook shouldBe Some(modifiedNotebook2)
        }
      }
    }
  }

  private def withStoredNotebooks(
      storedNotebooks: Set[(Workflow.Id, Node.Id, String)])(testCode: => Any): Unit = {

    Await.ready(notebooksDao.create(), operationDuration)

    val s = Future.sequence(storedNotebooks.map {
      case (workflowId, nodeId, notebook) => notebooksDao.save(workflowId, nodeId, notebook)
    })
    Await.ready(s, operationDuration)

    try {
      testCode
    } finally {
      Await.ready(notebooksDao.drop(), operationDuration)
    }
  }

  def createNotebook(): (Workflow.Id, Node.Id, String) = {
    (Workflow.Id.randomId, Node.Id.randomId, RandomStringUtils.randomAlphanumeric(16))
  }
}
