/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

import io.deepsense.commons.models
import io.deepsense.models.workflows.Workflow
import io.deepsense.models.workflows.Workflow._

/**
 * Thread-safe, in-memory ExperimentStorage.
 */
class InMemoryWorkflowStorage extends WorkflowStorage {
  private val workflows: TrieMap[models.Id, Workflow] = TrieMap()

  override def save(workflow: Workflow): Future[Unit] = {
    workflows += workflow.id -> workflow
    Future.successful(workflow)
  }

  override def get(tenantId: String, id: models.Id): Future[Option[Workflow]] = {
    Future.successful(workflows.get(id))
  }

  override def delete(tenantId: String, id: models.Id): Future[Unit] = {
    Future.successful(workflows.remove(id))
  }

  override def list(
    tenantId: String,
    limit: Option[Int],
    page: Option[Int],
    status: Option[Status.Value]): Future[Seq[Workflow]] = {
    // TODO: Implement filtering by status and pagination (using page and limit)
    Future.successful(workflows.filter(_._2.tenantId == tenantId).values.toList)
  }
}
