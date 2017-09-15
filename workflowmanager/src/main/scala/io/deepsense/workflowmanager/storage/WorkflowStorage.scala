/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.concurrent.Future

import org.joda.time.DateTime

import io.deepsense.commons.models.Id
import io.deepsense.models.workflows.Workflow

/**
 * Abstraction layer to make implementation of Workflow Manager easier.
 */
trait WorkflowStorage {

  /**
   * Returns a workflow with the specified id.
   * @param id Id of the workflow.
   * @return Workflow with the id as an object, or None if the workflow does not exist.
   */
  def get(id: Id): Future[Option[Workflow]]

  /**
   * Creates a workflow.
   * @param id Id of the workflow.
   * @param workflow Workflow to be created.
   */
  def create(id: Id, workflow: Workflow): Future[Unit]

  /**
   * Updates a workflow.
   * @param id Id of the workflow.
   * @param workflow Workflow to be updated.
   */
  def update(id: Id, workflow: Workflow): Future[Unit]

  /**
   * Returns all stored workflows. If the workflow is compatible with the current
   * API version it is returned as an object otherwise as a string.
   * @return Stored workflows as objects or Strings.
   */
  def getAll(): Future[Map[Workflow.Id, WorkflowWithDates]]

  /**
   * Removes an workflow with the specified id.
   * @param id Id of the workflow to be deleted.
   * @return Future.successful whether the workflow was found or not.
   *         If there were hard failures (e.g. connection error) the returned
   *         future will fail.
   */
  def delete(id: Id): Future[Unit]
}

case class WorkflowWithDates(
    workflow: Workflow,
    created: DateTime,
    updated: DateTime)
