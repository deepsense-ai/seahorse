/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.concurrent.Future

import io.deepsense.workflowmanager.model.{WorkflowWithSavedResults, ExecutionReportWithId}

trait WorkflowResultsStorage {

  /**
   * Returns execution report with a specified id.
   * @param id Id of the execution report.
   * @return Execution report.
   */
  def get(id: ExecutionReportWithId.Id): Future[Option[WorkflowWithSavedResults]]

  /**
   * Saves workflow execution report.
   * @param results WorkflowResults to be saved.
   */
  def save(results: WorkflowWithSavedResults): Future[Unit]
}
