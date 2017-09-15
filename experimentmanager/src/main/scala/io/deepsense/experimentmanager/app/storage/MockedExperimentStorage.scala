/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.storage

import java.util.UUID

import scala.collection.mutable
import scala.concurrent.Future

import io.deepsense.experimentmanager.app.models.Experiment._
import io.deepsense.experimentmanager.app.models.{Experiment, Graph}
import io.deepsense.experimentmanager.auth.HasTenantId

/**
 * Mock of ExperimentStorage. This is not thread-safe! (But DONE NOT have to be)
 */
class MockedExperimentStorage extends ExperimentStorage {
  private val experiments: mutable.Set[Experiment] = mutable.Set(
    Experiment(UUID.randomUUID(), "a", "A mocked experiment", "A mocked experiment1", Graph()),
    Experiment(UUID.randomUUID(), "b", "A mocked experiment", "A mocked experiment2", Graph()),
    Experiment(UUID.randomUUID(), "c", "A mocked experiment", "A mocked experiment3", Graph()))

  override def save(experiment: Experiment): Future[Experiment] = {
    experiments += experiment
    Future.successful(experiment)
  }

  override def get(id: Id): Future[Option[Experiment]] = {
    Future.successful(experiments.find(_.id == id))
  }

  override def delete(id: Id): Future[Unit] = {
    Future.successful(experiments
      .find(e => e.id == id)
      .map(experiments.remove))
  }

  override def list(
    tenant: HasTenantId,
    limit: Option[Int],
    page: Option[Int],
    status: Option[Status.Value]): Future[List[Experiment]] = {
    Future.successful(experiments.filter(_.tenantId == tenant).toList)
  }
}
