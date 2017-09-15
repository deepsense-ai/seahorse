/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.concurrent.Future

import io.deepsense.commons.models.Id
import io.deepsense.models.workflows.{Workflow, WorkflowWithSavedResults}

/**
 * Abstraction layer to make implementation of Workflow Manager easier.
 */
trait WorkflowStorage {

  /**
   * Returns a workflow with the specified id. If the workflow is compatible with the current
   * API version it is returned as an object otherwise as a string.
   * @param id Id of the workflow.
   * @return Workflow with the id as an object or String, or None if the workflow does not exist.
   */
  def get(id: Id): Future[Option[Either[String, Workflow]]]

  /**
   * Saves a workflow.
   * @param workflow Workflow to be saved.
   */
  def save(id: Id, workflow: Workflow): Future[Unit]

  /**
   * Returns latest execution results for given workflow id. If the workflow is compatible with the
   * current API version it is returned as an object otherwise as a string.
   * @param workflowId id of the workflow
   * @return Latest execution results for given workflow.
   */
  def getLatestExecutionResults(
    workflowId: Id): Future[Option[Either[String, WorkflowWithSavedResults]]]

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
