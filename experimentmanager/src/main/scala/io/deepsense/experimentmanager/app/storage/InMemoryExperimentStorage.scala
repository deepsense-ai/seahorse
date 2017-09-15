/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.storage

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

import io.deepsense.experimentmanager.app.models.Experiment
import io.deepsense.experimentmanager.app.models.Experiment._
import io.deepsense.experimentmanager.auth.HasTenantId

/**
 * Thread-safe, in-memory ExperimentStorage.
 */
class InMemoryExperimentStorage extends ExperimentStorage {
  private val experiments: TrieMap[Experiment.Id, Experiment] = TrieMap()

  override def save(experiment: Experiment): Future[Experiment] = {
    experiments += experiment.id -> experiment
    Future.successful(experiment)
  }

  override def get(id: Id): Future[Option[Experiment]] = {
    Future.successful(experiments.get(id))
  }

  override def delete(id: Id): Future[Unit] = {
    Future.successful(experiments.remove(id))
  }

  override def list(
    tenant: HasTenantId,
    limit: Option[Int],
    page: Option[Int],
    status: Option[Status.Value]): Future[List[Experiment]] = {
    // TODO: Implement filtering by status and pagination (using page and limit)
    Future.successful(experiments.filter(_._2.tenantMatches(tenant)).values.toList)
  }
}
