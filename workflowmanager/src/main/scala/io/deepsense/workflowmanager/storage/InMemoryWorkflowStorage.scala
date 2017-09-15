/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.joda.time.DateTime

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.models
import io.deepsense.models.workflows.Workflow._
import io.deepsense.models.workflows.{Workflow, WorkflowWithSavedResults}

/**
 * Thread-safe, in-memory WorkflowStorage.
 */
class InMemoryWorkflowStorage extends WorkflowStorage {
  private val workflows: TrieMap[models.Id, Entry] = TrieMap()

  override def save(id: Workflow.Id, workflow: Workflow): Future[Unit] = {
    def withNewWorkflow(old: Option[Entry]): Entry =
      Entry(workflow, old.flatMap(_.results), old.flatMap(_.lastExecutionTime))

    var oldEntry = workflows.get(id)
    var newEntry = withNewWorkflow(oldEntry)

    while (!workflows.replace(id, oldEntry.orNull, newEntry)) {
      oldEntry = workflows.get(id)
      newEntry = withNewWorkflow(oldEntry)
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
    def withNewResults(old: Option[Entry]): Entry =
      Entry(old.map(_.workflow).orNull, Some(executionResults), Some(DateTimeConverter.now))

    var oldEntry = workflows.get(executionResults.id)
    var newEntry = withNewResults(oldEntry)

    while (!workflows.replace(executionResults.id, oldEntry.orNull, newEntry)) {
      oldEntry = workflows.get(executionResults.id)
      newEntry = withNewResults(oldEntry)
    }
    Future.successful(())
  }

  override def getLastExecutionTime(workflowId: Id): Future[Option[DateTime]] =
    Future.successful(workflows.get(workflowId).flatMap(_.lastExecutionTime))

  private case class Entry(
      workflow: Workflow,
      results: Option[WorkflowWithSavedResults],
      lastExecutionTime: Option[DateTime])
}
