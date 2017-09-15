/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

import io.deepsense.models.workflows._

class InMemoryWorkflowResultsStorage(
    initState: Map[ExecutionReportWithId.Id, WorkflowWithSavedResults] = Map()
  ) extends WorkflowResultsStorage {

  val storage: TrieMap[ExecutionReportWithId.Id, WorkflowWithSavedResults] =
    TrieMap(initState.toSeq: _*)

  override def get(id: ExecutionReportWithId.Id): Future[Option[WorkflowWithSavedResults]] =
    Future.successful(storage.get(id))


  override def save(results: WorkflowWithSavedResults): Future[Unit] =
    Future.successful(storage.put(results.executionReport.id, results))
}
