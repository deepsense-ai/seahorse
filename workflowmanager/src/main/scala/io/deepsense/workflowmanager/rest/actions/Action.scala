/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest.actions

import scala.concurrent.Future
import io.deepsense.commons.models.Id
import io.deepsense.workflowmanager.WorkflowManager
import io.deepsense.models.workflows.{Workflow, Workflow$}

/**
 * Experiment Manager's REST API action.
 */
trait Action {
  def run(id: Id, experimentManager: WorkflowManager): Future[Workflow]
}
