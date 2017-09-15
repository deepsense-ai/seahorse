/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import io.deepsense.commons.models
import io.deepsense.models.workflows.Workflow._
import io.deepsense.models.workflows.{Workflow, WorkflowWithSavedResults}

/**
 * Thread-safe, in-memory WorkflowStorage.
 */
class InMemoryWorkflowStorage extends WorkflowStorage {
  private val workflows: TrieMap[models.Id, Entry] = TrieMap()

  override def save(id: Workflow.Id, workflow: Workflow): Future[Unit] = {
    var oldValue = workflows.get(id)
    var newValue = Entry(workflow, oldValue.flatMap(_.results))
    while (!workflows.replace(id, oldValue.orNull, newValue)) {
      oldValue = workflows.get(id)
      newValue = Entry(workflow, oldValue.flatMap(_.results))
    }
    Future.successful(())
  }

  override def get(id: Workflow.Id): Future[Option[Either[String, Workflow]]] = {
    Future.successful(workflows.get(id).map(_.workflow).map(Right(_)))
  }

  override def delete(id: Workflow.Id): Future[Unit] = {
    Future.successful(workflows.remove(id))
  }

  override def getLatestExecutionResults(
      workflowId: Id): Future[Option[Either[String, WorkflowWithSavedResults]]] = {
    Future(workflows.get(workflowId).flatMap(_.results).map(Right(_)))
  }

  override def saveExecutionResults(
      executionResults: WorkflowWithSavedResults): Future[Unit] = {
    var oldValue = workflows.get(executionResults.id)
    var newValue = Entry(oldValue.map(_.workflow).orNull, Some(executionResults))
    while (!workflows.replace(executionResults.id, oldValue.orNull, newValue)) {
      oldValue = workflows.get(executionResults.id)
      newValue = Entry(oldValue.map(_.workflow).orNull, Some(executionResults))
    }
    Future.successful(())
  }

  private case class Entry(workflow: Workflow, results: Option[WorkflowWithSavedResults])
}
