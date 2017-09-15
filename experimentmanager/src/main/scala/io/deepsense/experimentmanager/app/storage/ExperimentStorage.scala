/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.storage

import scala.concurrent.Future

import io.deepsense.experimentmanager.app.models.Experiment
import io.deepsense.experimentmanager.auth.HasTenantId

/**
 * Abstraction layer to make implementation of Experiment Manager easier.
 */
trait ExperimentStorage {

  /**
   * Returns an experiment with the specified id.
   * @param id Id of the experiment.
   * @return Experiment with the id or None.
   */
  def get(id: Experiment.Id): Future[Option[Experiment]]

  /**
   * Saves an experiment.
   * @param experiment Experiment to be saved.
   * @return Saved experiment.
   */
  def save(experiment: Experiment): Future[Experiment]

  /**
   * Removes an experiment with the specified id.
   * @param id Id of the experiment to be deleted.
   * @return Future.successful whether the experiment was found or not.
   *         If there were hard failures (e.g. connection error) the returned
   *         future will fail.
   */
  def delete(id: Experiment.Id): Future[Unit]

  /**
   * Returns a list of experiment.
   * @param tenant Owner of the experiments.
   * @param limit Size of the list (page)
   * @param page Page number.
   * @param status Allows to filter experiments with the specified status.
   *               If not specified then experiments will not be filltered out.
   * @return A filtered list of experiments that is a certain page of a certain size.
   */
  def list(tenant: HasTenantId,
    limit: Option[Int],
    page: Option[Int],
    status: Option[Experiment.Status.Value]): Future[List[Experiment]]
}
