/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.concurrent.Future

import io.deepsense.commons.models.Id
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.model.WorkflowWithSavedResults

/**
 * Abstraction layer to make implementation of Workflow Manager easier.
 */
trait WorkflowStorage {

  /**
   * Returns a workflow with the specified id.
   * @param id Id of the workflow.
   * @return Workflow with the id or None.
   */
  def get(id: Id): Future[Option[Workflow]]

  /**
   * Saves a workflow.
   * @param workflow Workflow to be saved.
   */
  def save(id: Id, workflow: Workflow): Future[Unit]

  /**
   * Returns latest execution results for given workflow id.
   * @param workflowId id of the workflow
   * @return Latest execution results for given workflow.
   */
  def getLatestExecutionResults(workflowId: Id): Future[Option[WorkflowWithSavedResults]]

  /**
   * Saves a workflow execution results.
   * @param results Workflow results to be saved as latest.
   */
  def saveExecutionResults(results: WorkflowWithSavedResults): Future[Unit]

  /**
   * Removes an workflow with the specified id.
   * @param id Id of the workflow to be deleted.
   * @return Future.successful whether the workflow was found or not.
   *         If there were hard failures (e.g. connection error) the returned
   *         future will fail.
   */
  def delete(id: Id): Future[Unit]
}
