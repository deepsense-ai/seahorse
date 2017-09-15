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

import ai.deepsense.graph.Node
import ai.deepsense.models.workflows.{NodeState, Workflow}


trait WorkflowStateStorage {

  /**
   * Retrieves result entities' ids and reports for all operations in the specified workflow.
   * The returned statuses are always Draft().
   * Note, that this method doesn't know anything about the current structure of the workflow,
   * therefore it might return nodes that are not in the workflow anymore.
   */
  def get(workflowId: Workflow.Id): Future[Map[Node.Id, NodeState]]

  /**
   * For each entry in state, inserts/updates the appropriate row in storage.
   * Persists only result entities' ids and reports. Statuses are not persisted.
   * Note, that if reports are None, the field will not be updated.
   * In order to remove reports from a row, empty EntitiesMap should be passed.
   */
  def save(
    workflowId: Workflow.Id,
    state: Map[Node.Id, NodeState]): Future[Unit]
}
