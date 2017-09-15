/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import scala.concurrent.Future

import org.joda.time.DateTime

import io.deepsense.commons.models.Id
import io.deepsense.graph.Node
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.model.WorkflowDescription

/**
 * Workflow Manager's API
 */
trait WorkflowManager {

  /**
   * Returns a workflow with results with the specified Id.
   * @param id An identifier of the workflow.
   * @return A workflow with results with the specified Id.
   */
  def get(id: Id): Future[Option[WorkflowWithResults]]

  /**
   * Returns a workflow with an empty variables section and specified Id.
   * @param id An identifier of the workflow.
   * @return A workflow with an empty variables section and specified Id.
   */
  def download(id: Id): Future[Option[WorkflowWithVariables]]

  /**
   * Updates an workflow.
   * @param workflowId Id of workflow to be updated.
   * @param workflow An workflow to be updated.
   * @return The updated workflow with knowledge.
   */
  def update(workflowId: Id, workflow: Workflow): Future[Unit]

  /**
   * Creates new workflow.
   * @param workflow New workflow.
   * @return created workflow id.
   */
  def create(workflow: Workflow): Future[Workflow.Id]

  /**
   * Deletes a workflow by Id.
   * @param id An identifier of the workflow to delete.
   * @return True if the workflow was deleted.
   *         Otherwise false.
   */
  def delete(id: Id): Future[Boolean]

  /**
    * Clones a workflow by Id.
    * @param id An identifier of the workflow to clone.
    * @param workflowDescription A description for a cloned workflow.
    * @return Cloned workflow or None, if workflow with specified id does not exist.
    */
  def clone(id: Id, workflowDescription: WorkflowDescription): Future[Option[WorkflowWithVariables]]

  /**
   * Lists stored workflows.
   * @return List of basic information about stored workflows.
   */
  def list(): Future[Seq[WorkflowInfo]]

  /**
   * Returns a notebook for workflow with the specified id.
   *
   * @param workflowId Id of the workflow.
   * @param nodeId Id of the node.
   * @return Notebook or None if the notebook does not exist.
   */
  def getNotebook(workflowId: Workflow.Id, nodeId: Node.Id): Future[Option[String]]

  /**
   * Saves a notebook for workflow with the specified id.
   *
   * @param workflowId Id of the workflow.
   * @param nodeId Id of the node.
   * @param notebook Notebook to be saved.
   */
  def saveNotebook(workflowId: Workflow.Id, nodeId: Node.Id, notebook: String): Future[Unit]

  /**
   * Updates nodes states.
   *
   * @param workflowId Id of the workflow.
   * @param executionReport Execution report with updated nodes to save.
   */
  def updateStates(workflowId: Workflow.Id, executionReport: ExecutionReport): Future[Unit]

  /**
   * Updates an workflow, and then updates its nodes states.
   *
   * @param workflowId Id of the workflow.
   * @param workflowWithResults Updated workflow and its updated results to save.
   */
  def updateStructAndStates(
      workflowId: Workflow.Id,
      workflowWithResults: WorkflowWithResults): Future[Unit]
}
