/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest.actions

import scala.concurrent.Future
import io.deepsense.commons.models.Id
import io.deepsense.experimentmanager.ExperimentManager
import io.deepsense.graph.Node
import io.deepsense.models.experiments.Experiment

/**
 * Abort experiment action.
 */
case class AbortAction(nodes: Option[List[Node.Id]]) extends Action {
  override def run(id: Id, experimentManager: ExperimentManager): Future[Experiment] = {
    experimentManager.abort(id, nodes.getOrElse(List()))
  }
}
