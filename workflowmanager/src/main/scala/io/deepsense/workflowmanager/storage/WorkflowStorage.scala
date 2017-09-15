/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.concurrent.Future

import io.deepsense.commons.models.Id
import io.deepsense.models.workflows.Workflow

/**
 * Abstraction layer to make implementation of Workflow Manager easier.
 */
trait WorkflowStorage {

  /**
   * Returns an workflow with the specified id.
   * @param id Id of the workflow.
   * @return Workflow with the id or None.
   */
  def get(id: Id): Future[Option[Workflow]]

  /**
   * Saves an workflow.
   * @param workflow Workflow to be saved.
   * @return Saved workflow.
   */
  def save(id: Id, workflow: Workflow): Future[Unit]

  /**
   * Removes an workflow with the specified id.
   * @param id Id of the workflow to be deleted.
   * @return Future.successful whether the workflow was found or not.
   *         If there were hard failures (e.g. connection error) the returned
   *         future will fail.
   */
  def delete(id: Id): Future[Unit]
}
