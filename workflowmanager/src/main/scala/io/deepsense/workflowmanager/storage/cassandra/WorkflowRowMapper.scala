/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import com.datastax.driver.core.Row
import com.google.inject.Inject
import spray.json._

import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.json.WorkflowWithRegisteredResultsJsonProtocol
import io.deepsense.workflowmanager.model.WorkflowWithSavedResults

case class WorkflowRowMapper @Inject() (
    override val graphReader: GraphReader)
  extends WorkflowWithRegisteredResultsJsonProtocol {

  def toWorkflow(row: Row): Workflow = {
    row.getString(WorkflowRowMapper.Workflow).parseJson.convertTo[Workflow](workflowFormat)
  }

  def toWorkflowWithSavedResults(row: Row): Option[WorkflowWithSavedResults] = {
    Option(row.getString(WorkflowRowMapper.Results))
      .map(_.parseJson.convertTo[WorkflowWithSavedResults])
  }

  def workflowToCell(workflow: Workflow): String = workflow.toJson.compactPrint

  def resultsToCell(results: WorkflowWithSavedResults): String = results.toJson.compactPrint
}

object WorkflowRowMapper {
  val Id = "id"
  val Workflow = "workflow"
  val Results = "results"
  val Deleted = "deleted"
}
