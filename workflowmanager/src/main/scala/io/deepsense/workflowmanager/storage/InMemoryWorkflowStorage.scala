/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

import io.deepsense.commons.models
import io.deepsense.models.workflows.{Workflow, Workflow$}
import io.deepsense.models.workflows.Workflow._

/**
 * Thread-safe, in-memory ExperimentStorage.
 */
class InMemoryWorkflowStorage extends WorkflowStorage {
  private val experiments: TrieMap[models.Id, Workflow] = TrieMap()

  override def save(experiment: Workflow): Future[Unit] = {
    experiments += experiment.id -> experiment
    Future.successful(experiment)
  }

  override def get(tenantId: String, id: models.Id): Future[Option[Workflow]] = {
    Future.successful(experiments.get(id))
  }

  override def delete(tenantId: String, id: models.Id): Future[Unit] = {
    Future.successful(experiments.remove(id))
  }

  override def list(
    tenantId: String,
    limit: Option[Int],
    page: Option[Int],
    status: Option[Status.Value]): Future[Seq[Workflow]] = {
    // TODO: Implement filtering by status and pagination (using page and limit)
    Future.successful(experiments.filter(_._2.tenantId == tenantId).values.toList)
  }
}
