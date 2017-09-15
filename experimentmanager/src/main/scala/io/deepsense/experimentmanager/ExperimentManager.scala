/**
 * Copyright (c) 2015, CodiLime Inc.
 */
package io.deepsense.experimentmanager

import scala.concurrent.Future

import io.deepsense.commons.models.Id
import io.deepsense.experimentmanager.models.ExperimentsList
import io.deepsense.graph.Node
import io.deepsense.models.experiments.{Experiment, InputExperiment}

/**
 * Experiment Manager's API
 */
trait ExperimentManager {

  /**
   * Returns an experiment with the specified Id.
   * @param id An identifier of the experiment.
   * @return An experiment with the specified Id.
   */
  def get(id: Id): Future[Option[Experiment]]

  /**
   * Updates an experiment.
   * @param experimentId Id of experiment to be updated.
   * @param experiment An experiment to be updated.
   * @return The updated experiment.
   */
  def update(experimentId: Id, experiment: InputExperiment): Future[Experiment]

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
      status: Option[Experiment.Status.Value]): Future[ExperimentsList]

  /**
   * Deletes an experiment by Id.
   * @param id An identifier of the experiment to delete.
   * @return True if the experiment was deleted.
   *         Otherwise false.
   */
  def delete(id: Id): Future[Boolean]

  /**
   * Launches an experiment with the specified Id.
   * @param id The Id of an experiment to launch.
   * @param targetNodes Nodes that should be calculated in the launch.
   *                    Empty means to calculate all.
   * @return The launched experiment.
   */
  def launch(
      id: Id,
      targetNodes: Seq[Node.Id]): Future[Experiment]

  /**
   * Aborts an experiment. Allows to specify nodes to abort. If nodes list is
   * empty aborts whole experiment.
   * @param id An identifier of the experiment to abort.
   * @param nodes List of experiment's nodes to abort.
   * @return The aborted experiment.
   */
  def abort(id: Id, nodes: Seq[Node.Id]): Future[Experiment]
}
