/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest.actions

import scala.concurrent.Future
import io.deepsense.commons.models.Id
import io.deepsense.workflowmanager.WorkflowManager
import io.deepsense.graph.Node
import io.deepsense.models.workflows.{Workflow, Workflow$}

/**
 * Launch experiment action.
 */
case class LaunchAction(targetNodes: Option[List[Node.Id]])
  extends Action {
  override def run(id: Id, experimentManager: WorkflowManager): Future[Workflow] = {
    experimentManager.launch(id, targetNodes.getOrElse(List()))
  }
}
