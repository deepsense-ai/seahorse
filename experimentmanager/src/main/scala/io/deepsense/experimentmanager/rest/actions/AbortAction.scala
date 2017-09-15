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
 * Abort experiment action.
 */
case class AbortAction(nodes: Option[List[Node.Id]]) extends Action {
  override def run(id: Id, experimentManager: ExperimentManager): Future[Experiment] = {
    experimentManager.abort(id, nodes.getOrElse(List()))
  }
}
