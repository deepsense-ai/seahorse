/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.rest.actions

import scala.concurrent.Future

import io.deepsense.experimentmanager.app.ExperimentManager
import io.deepsense.experimentmanager.app.models.{Experiment, Node}

/**
 * Abort experiment action.
 */
case class AbortAction(nodes: Option[List[Node.Id]]) extends Action {
  override def run(id: Experiment.Id, experimentManager: ExperimentManager): Future[Experiment] = {
    experimentManager.abort(id, nodes.getOrElse(List()))
  }
}
