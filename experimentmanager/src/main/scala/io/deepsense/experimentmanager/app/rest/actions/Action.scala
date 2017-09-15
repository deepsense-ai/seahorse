/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.rest.actions

import scala.concurrent.Future

import io.deepsense.experimentmanager.app.ExperimentManager
import io.deepsense.experimentmanager.app.models.Experiment

/**
 * Experiment Manager's REST API action.
 */
trait Action {
  def run(id: Experiment.Id, experimentManager: ExperimentManager): Future[Experiment]
}
