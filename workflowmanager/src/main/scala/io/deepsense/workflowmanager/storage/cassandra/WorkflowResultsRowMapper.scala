/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import com.datastax.driver.core.Row
import com.google.inject.Inject
import spray.json._

import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.WorkflowWithSavedResultsJsonProtocol
import io.deepsense.models.workflows.WorkflowWithSavedResults

class WorkflowResultsRowMapper @Inject() (
  override val graphReader: GraphReader)
  extends WorkflowWithSavedResultsJsonProtocol {

  def fromRow(row: Row): WorkflowWithSavedResults = {
    row.getString(WorkflowResultsRowMapper.Results)
      .parseJson.convertTo[WorkflowWithSavedResults]
  }

  def resultToCell(result: WorkflowWithSavedResults): String = result.toJson.compactPrint
}

object WorkflowResultsRowMapper {
  val Id = "id"
  val Results = "results"
}
