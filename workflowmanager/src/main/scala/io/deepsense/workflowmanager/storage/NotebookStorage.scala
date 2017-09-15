/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.concurrent.Future

import io.deepsense.commons.models.Id
import io.deepsense.graph.Node
import io.deepsense.models.workflows.Workflow

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
