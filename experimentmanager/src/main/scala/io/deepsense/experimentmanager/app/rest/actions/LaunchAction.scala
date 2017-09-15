/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.rest.actions

import scala.concurrent.Future

import io.deepsense.experimentmanager.app.ExperimentManager
import io.deepsense.experimentmanager.app.models.Experiment
import io.deepsense.graph.Node

/**
 * Launch experiment action.
 */
case class LaunchAction(targetNodes: Option[List[Node.Id]])
  extends Action {
  override def run(id: Experiment.Id, experimentManager: ExperimentManager): Future[Experiment] = {
    experimentManager.launch(id, targetNodes.getOrElse(List()))
  }
}
