/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import scala.concurrent.Future

import io.deepsense.commons.models.Id
import io.deepsense.models.workflows.{WorkflowWithResults, Workflow, WorkflowWithKnowledge, WorkflowWithVariables}
import io.deepsense.workflowmanager.model.WorkflowWithSavedResults

/**
 * Workflow Manager's API
 */
trait WorkflowManager {

  /**
   * Returns a workflow with knowledge with the specified Id.
   * @param id An identifier of the workflow.
   * @return A workflow with knowledge with the specified Id.
   */
  def get(id: Id): Future[Option[WorkflowWithKnowledge]]

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
  def update(workflowId: Id, workflow: Workflow): Future[WorkflowWithKnowledge]

  /**
   * Creates new workflow.
   * @param workflow New workflow.
   * @return Enhanced, saved version of the workflow with knowledge (includes identifier etc).
   */
  def create(workflow: Workflow): Future[WorkflowWithKnowledge]

  /**
   * Deletes a workflow by Id.
   * @param id An identifier of the workflow to delete.
   * @return True if the workflow was deleted.
   *         Otherwise false.
   */
  def delete(id: Id): Future[Boolean]

  /**
   * Saves workflow results
   * @param workflowWithResults workflow results to save
   * @return saved workflow results
   */
  def saveWorkflowResults(
    workflowWithResults: WorkflowWithResults): Future[WorkflowWithSavedResults]

}
