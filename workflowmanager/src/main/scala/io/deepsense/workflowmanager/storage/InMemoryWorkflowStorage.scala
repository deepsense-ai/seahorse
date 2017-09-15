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
 * Thread-safe, in-memory WorkflowStorage.
 */
class InMemoryWorkflowStorage extends WorkflowStorage {
  private val workflows: TrieMap[models.Id, Workflow] = TrieMap()

  override def save(id: Workflow.Id, workflow: Workflow): Future[Unit] = {
    workflows += id -> workflow
    Future.successful(())
  }

  override def get(id: Workflow.Id): Future[Option[Workflow]] = {
    Future.successful(workflows.get(id))
  }

  override def delete(id: Workflow.Id): Future[Unit] = {
    Future.successful(workflows.remove(id))
  }
}
