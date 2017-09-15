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

package ai.deepsense.workflowmanager.storage

import scala.concurrent.Future

import ai.deepsense.commons.models.Id
import ai.deepsense.graph.Node
import ai.deepsense.models.workflows.Workflow

trait NotebookStorage {

  /**
   * Returns a notebook with the specified workflow and node id.
   *
   * @param workflowId Id of the workflow.
   * @param nodeId Id of the node.
   * @return Notebook or None if the notebook does not exist.
   */
  def get(workflowId: Workflow.Id, nodeId: Node.Id): Future[Option[String]]

  /**
   * Saves a notebook.
   *
   * @param workflowId Id of the notebook.
   * @param nodeId Id of the node.
   * @param notebook Notebook to be saved.
   */
  def save(workflowId: Workflow.Id, nodeId: Node.Id, notebook: String): Future[Unit]

  /**
   * Returns all notebooks for workflow with specified id.
   *
   * @param workflowId Id of the workflow
   * @return Notebooks for specified workflow.
   */
  def getAll(workflowId: Workflow.Id): Future[Map[Node.Id, String]]
}
