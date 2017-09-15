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

/**
 * Experiment Manager's REST API action.
 */
trait Action {
  def run(id: Id, experimentManager: ExperimentManager): Future[Experiment]
}
