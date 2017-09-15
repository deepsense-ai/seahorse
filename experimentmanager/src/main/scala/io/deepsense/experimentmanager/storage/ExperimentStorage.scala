/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.storage

import scala.concurrent.Future

import io.deepsense.commons.auth.HasTenantId
import io.deepsense.commons.models.Id
import io.deepsense.models.experiments.Experiment

/**
 * Abstraction layer to make implementation of Experiment Manager easier.
 */
trait ExperimentStorage {

  /**
   * Returns an experiment with the specified id.
   * @param id Id of the experiment.
   * @return Experiment with the id or None.
   */
  def get(tenantId: String, id: Id): Future[Option[Experiment]]

  /**
   * Saves an experiment.
   * @param experiment Experiment to be saved.
   * @return Saved experiment.
   */
  def save(experiment: Experiment): Future[Unit]

  /**
   * Removes an experiment with the specified id.
   * @param id Id of the experiment to be deleted.
   * @return Future.successful whether the experiment was found or not.
   *         If there were hard failures (e.g. connection error) the returned
   *         future will fail.
   */
  def delete(tenantId: String, id: Id): Future[Unit]

  /**
   * Returns a list of experiment.
   * @param tenantId Owner of the experiments.
   * @param limit Size of the list (page)
   * @param page Page number.
   * @param status Allows to filter experiments with the specified status.
   *               If not specified then experiments will not be filtered out.
   * @return A filtered list of experiments that is a certain page of a certain size.
   */
  def list(
    tenantId: String,
    limit: Option[Int],
    page: Option[Int],
    status: Option[Experiment.Status.Value] = None): Future[Seq[Experiment]]
}
