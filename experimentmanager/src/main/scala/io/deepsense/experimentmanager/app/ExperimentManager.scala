/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app

import scala.concurrent.Future

import io.deepsense.experimentmanager.app.models.Graph.Node
import io.deepsense.experimentmanager.app.models.{Experiment, InputExperiment}

/**
 * Experiment Manager's API
 */
trait ExperimentManager {

  /**
   * Returns an experiment with the specified Id.
   * @param id An identifier of the experiment.
   * @return An experiment with the specified Id.
   */
  def get(id: Experiment.Id): Future[Option[Experiment]]

  /**
   * Updates an experiment.
   * @param experiment An experiment to be updated.
   * @return The updated experiment.
   */
  def update(experiment: Experiment): Future[Experiment]

  /**
   * Creates new experiment.
   * @param experiment New experiment.
   * @return Enhanced, saved version of the experiment (includes identifier etc).
   */
  def create(experiment: InputExperiment): Future[Experiment]

  /**
   * Lists experiments. Supports pagination.
   * @param limit Size of a page.
   * @param page Number of a page.
   * @param status Filters experiments by their statuses.
   * @return A filtered page of experiments.
   */
  def experiments(
      limit: Option[Int],
      page: Option[Int],
      status: Option[Experiment.Status.Value]): Future[Seq[Experiment]]

  /**
   * Deletes an experiment by Id.
   * @param id An identifier of the experiment to delete.
   * @return True if the experiment was deleted.
   *         Otherwise false.
   */
  def delete(id: Experiment.Id): Future[Boolean]

  /**
   * Launches an experiment with the specified Id.
   * @param id The Id of an experiment to launch.
   * @param targetNodes Nodes that should be calculated in the launch.
   *                    Empty means to calculate all.
   * @return The launched experiment.
   */
  def launch(
      id: Experiment.Id,
      targetNodes: List[Node.Id]): Future[Experiment]

  /**
   * Aborts an experiment. Allows to specify nodes to abort. If nodes list is
   * empty aborts whole experiment.
   * @param id An identifier of the experiment to abort.
   * @param nodes List of experiment's nodes to abort.
   * @return The aborted experiment.
   */
  def abort(id: Experiment.Id, nodes: List[Node.Id]): Future[Experiment]
}
