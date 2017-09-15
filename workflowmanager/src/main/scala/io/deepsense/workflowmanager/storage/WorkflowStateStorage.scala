/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.concurrent.Future

import io.deepsense.graph.Node
import io.deepsense.models.workflows.{NodeState, Workflow}


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
