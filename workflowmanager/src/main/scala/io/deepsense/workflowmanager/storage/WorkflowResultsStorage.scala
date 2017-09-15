/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.concurrent.Future

import io.deepsense.commons.models.Id
import io.deepsense.models.workflows.WorkflowWithResults

trait WorkflowResultsStorage {

  /**
   * Returns an workflow results for workflow with the specified id.
   * @param id Id of the workflow.
   * @return Execution results
   */
  def get(id: Id): Future[List[WorkflowWithResults]]

  /**
   * Saves workflow results.
   * @param results WorkflowResults to be saved.
   * @return Saved workflow.
   */
  def save(id: Id, results: WorkflowWithResults): Future[Unit]
}
