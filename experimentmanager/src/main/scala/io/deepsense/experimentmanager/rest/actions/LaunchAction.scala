/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.rest.actions

import scala.concurrent.Future
import io.deepsense.commons.models.Id
import io.deepsense.experimentmanager.ExperimentManager
import io.deepsense.experimentmanager.models.Experiment
import io.deepsense.graph.Node

/**
 * Launch experiment action.
 */
case class LaunchAction(targetNodes: Option[List[Node.Id]])
  extends Action {
  override def run(id: Id, experimentManager: ExperimentManager): Future[Experiment] = {
    experimentManager.launch(id, targetNodes.getOrElse(List()))
  }
}
