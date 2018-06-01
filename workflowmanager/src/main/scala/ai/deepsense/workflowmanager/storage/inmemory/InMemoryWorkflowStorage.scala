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

package ai.deepsense.workflowmanager.storage.inmemory

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.language.postfixOps

import org.joda.time.DateTime
import spray.json._

import ai.deepsense.deeplang.CatalogRecorder
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.workflows.Workflow
import ai.deepsense.workflowmanager.storage.{WorkflowFullInfo, WorkflowRaw, WorkflowStorage}

/**
 * Thread-safe, in-memory WorkflowStorage.
 */
class InMemoryWorkflowStorage extends WorkflowStorage {
  override val graphReader = new GraphReader(CatalogRecorder.resourcesCatalogRecorder.catalogs.operations)

  private val workflows: TrieMap[Workflow.Id, WorkflowRaw] = TrieMap()
  private val now = DateTime.now()

  def createRaw(id: Workflow.Id, workflow: JsValue, ownerId: String, ownerName: String): Future[Unit] = {
    save(id, workflow, Some(ownerId), Some(ownerName))
  }

  override def updateRaw(id: Workflow.Id, workflow: JsValue): Future[Unit] = {
    save(id, workflow, None, None)
  }

  private def save(id: Workflow.Id, workflow: JsValue,
      ownerId: Option[String], ownerName: Option[String]): Future[Unit] = {
    def withNewWorkflow(old: Option[WorkflowRaw]): WorkflowRaw =
      WorkflowRaw(workflow,
        old.map(_.created).getOrElse(DateTime.now),
        old.map(_.updated).getOrElse(DateTime.now),
        ownerId orElse old.map(_.ownerId) get,
        ownerName orElse old.map(_.ownerName) get)

    var oldEntry = workflows.get(id)
    var newEntry = withNewWorkflow(oldEntry)

    while (!workflows.replace(id, oldEntry.orNull, newEntry)) {
      oldEntry = workflows.get(id)
      newEntry = withNewWorkflow(oldEntry)
    }
    Future.successful(())
  }

  override def get(id: Workflow.Id): Future[Option[WorkflowFullInfo]] = {
    Future.successful(workflows.get(id).map(rawWorkflowToFullWorkflow))
  }

  override def getAllRaw: Future[Map[Workflow.Id, WorkflowRaw]] = {
    Future.successful(workflows.toMap)
  }

  override def delete(id: Workflow.Id): Future[Unit] = {
    Future.successful(workflows.remove(id))
  }
}
