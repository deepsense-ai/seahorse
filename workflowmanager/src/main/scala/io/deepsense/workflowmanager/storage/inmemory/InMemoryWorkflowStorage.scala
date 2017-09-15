/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.inmemory

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.language.postfixOps

import org.joda.time.DateTime

import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.storage.{WorkflowFullInfo, WorkflowStorage}

/**
 * Thread-safe, in-memory WorkflowStorage.
 */
class InMemoryWorkflowStorage extends WorkflowStorage {
  private val workflows: TrieMap[Workflow.Id, WorkflowFullInfo] = TrieMap()
  private val now = DateTime.now()

  override def create(
      id: Workflow.Id, workflow: Workflow, ownerId: String, ownerName: String): Future[Unit] = {
    save(id, workflow, Some(ownerId), Some(ownerName))
  }

  override def update(id: Workflow.Id, workflow: Workflow): Future[Unit] = {
    save(id, workflow, None, None)
  }

  private def save(id: Workflow.Id, workflow: Workflow,
      ownerId: Option[String], ownerName: Option[String]): Future[Unit] = {
    def withNewWorkflow(old: Option[WorkflowFullInfo]): WorkflowFullInfo =
      WorkflowFullInfo(workflow,
        old.map(_.created).getOrElse(DateTime.now),
        old.map(_.updated).getOrElse(DateTime.now),
        ownerId orElse old.map(_.ownerId) get,
        ownerName orElse old.map(_.ownerName) get)

    var oldEntry = workflows.get(id)
    var newEntry = withNewWorkflow(oldEntry)

    while (!workflows.replace(id, oldEntry.orNull, newEntry)) {
      oldEntry = workflows.get(id)
      newEntry = withNewWorkflow(oldEntry)
    }
    Future.successful(())
  }

  override def get(id: Workflow.Id): Future[Option[WorkflowFullInfo]] = {
    Future.successful(workflows.get(id))
  }

  override def getAll(): Future[Map[Workflow.Id, WorkflowFullInfo]] = {
    Future.successful(workflows.toMap)
  }

  override def delete(id: Workflow.Id): Future[Unit] = {
    Future.successful(workflows.remove(id))
  }
}
