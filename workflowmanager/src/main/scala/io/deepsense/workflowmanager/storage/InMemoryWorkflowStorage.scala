/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

import org.joda.time.DateTime

import io.deepsense.models.workflows.Workflow

/**
 * Thread-safe, in-memory WorkflowStorage.
 */
class InMemoryWorkflowStorage extends WorkflowStorage {
  private val workflows: TrieMap[Workflow.Id, Workflow] = TrieMap()
  private val now = DateTime.now()

  override def create(id: Workflow.Id, workflow: Workflow): Future[Unit] = {
    save(id, workflow)
  }

  override def update(id: Workflow.Id, workflow: Workflow): Future[Unit] = {
    save(id, workflow)
  }

  private def save(id: Workflow.Id, workflow: Workflow): Future[Unit] = {
    def withNewWorkflow(old: Option[Workflow]): Workflow = workflow

    var oldEntry = workflows.get(id)
    var newEntry = withNewWorkflow(oldEntry)

    while (!workflows.replace(id, oldEntry.orNull, newEntry)) {
      oldEntry = workflows.get(id)
      newEntry = withNewWorkflow(oldEntry)
    }
    Future.successful(())
  }

  override def get(id: Workflow.Id): Future[Option[Workflow]] = {
    Future.successful(workflows.get(id))
  }

  override def getAll(): Future[Map[Workflow.Id, WorkflowWithDates]] = {
    Future.successful(workflows.mapValues(workflow =>
      WorkflowWithDates(workflow, now, now)).toMap)
  }

  override def delete(id: Workflow.Id): Future[Unit] = {
    Future.successful(workflows.remove(id))
  }
}
