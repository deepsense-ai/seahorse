/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.storage

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

import io.deepsense.commons.models
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.experiments.Experiment._

/**
 * Thread-safe, in-memory ExperimentStorage.
 */
class InMemoryExperimentStorage extends ExperimentStorage {
  private val experiments: TrieMap[models.Id, Experiment] = TrieMap()

  override def save(experiment: Experiment): Future[Unit] = {
    experiments += experiment.id -> experiment
    Future.successful(experiment)
  }

  override def get(tenantId: String, id: models.Id): Future[Option[Experiment]] = {
    Future.successful(experiments.get(id))
  }

  override def delete(tenantId: String, id: models.Id): Future[Unit] = {
    Future.successful(experiments.remove(id))
  }

  override def list(
    tenantId: String,
    limit: Option[Int],
    page: Option[Int],
    status: Option[Status.Value]): Future[Seq[Experiment]] = {
    // TODO: Implement filtering by status and pagination (using page and limit)
    Future.successful(experiments.filter(_._2.tenantId == tenantId).values.toList)
  }
}
