/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import scala.concurrent.Future

import io.deepsense.commons.models.Id
import io.deepsense.graph.Node
import io.deepsense.models.actions.Action
import io.deepsense.models.workflows.{WorkflowsList, InputWorkflow, Workflow}

/**
 * Workflow Manager's API
 */
trait WorkflowManager {

  /**
   * Returns a workflow with the specified Id.
   * @param id An identifier of the workflow.
   * @return A workflow with the specified Id.
   */
  def get(id: Id): Future[Option[Workflow]]

  /**
   * Updates an workflow.
   * @param workflowId Id of workflow to be updated.
   * @param workflow An workflow to be updated.
   * @return The updated workflow.
   */
  def update(workflowId: Id, workflow: InputWorkflow): Future[Workflow]

  /**
   * Creates new workflow.
   * @param workflow New workflow.
   * @return Enhanced, saved version of the workflow (includes identifier etc).
   */
  def create(workflow: InputWorkflow): Future[Workflow]

  /**
   * Lists workflows. Supports pagination.
   * @param limit Size of a page.
   * @param page Number of a page.
   * @param status Filters workflows by their statuses.
   * @return A filtered page of workflows.
   */
  def workflows(
      limit: Option[Int],
      page: Option[Int],
      status: Option[Workflow.Status.Value]): Future[WorkflowsList]

  /**
   * Deletes an workflow by Id.
   * @param id An identifier of the workflow to delete.
   * @return True if the workflow was deleted.
   *         Otherwise false.
   */
  def delete(id: Id): Future[Boolean]

  /**
   * Launches an workflow with the specified Id.
   * @param id The Id of an workflow to launch.
   * @param targetNodes Nodes that should be calculated in the launch.
   *                    Empty means to calculate all.
   * @return The launched workflow.
   */
  def launch(
      id: Id,
      targetNodes: Seq[Node.Id]): Future[Workflow]

  /**
   * Aborts an workflow. Allows to specify nodes to abort. If nodes list is
   * empty aborts whole workflow.
   * @param id An identifier of the workflow to abort.
   * @param nodes List of workflow's nodes to abort.
   * @return The aborted workflow.
   */
  def abort(id: Id, nodes: Seq[Node.Id]): Future[Workflow]

  /**
   * Runs action.
   * @param id An identifier of the workflow to run oction on.
   * @param action to run
   * @return Workflow a
   */
  def runAction(id: Id, action: Action): Future[Workflow]
}
