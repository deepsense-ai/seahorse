/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import scala.util.{Failure, Success, Try}

import com.datastax.driver.core.Row
import com.google.inject.Inject
import com.google.inject.name.Named
import spray.json._

import io.deepsense.commons.utils.Logging
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.WorkflowWithSavedResultsJsonProtocol
import io.deepsense.models.workflows.{Workflow, WorkflowWithSavedResults}
import io.deepsense.workflowmanager.exceptions.WorkflowVersionNotSupportedException
import io.deepsense.workflowmanager.rest.Version
import io.deepsense.workflowmanager.util.WorkflowVersionUtil

case class WorkflowRowMapper @Inject() (
    override val graphReader: GraphReader)
  extends WorkflowWithSavedResultsJsonProtocol
  with WorkflowVersionUtil
  with Logging {

  def toWorkflow(row: Row): Either[String, Workflow] = {
    val stringRow = row.getString(WorkflowRowMapper.Workflow)
    workflowOrString(stringRow)
  }

  def toWorkflowWithSavedResults(row: Row): Option[Either[String, WorkflowWithSavedResults]] = {
    Option(row.getString(WorkflowRowMapper.Results)).map {
      workflowWithSavedResultsOrString
    }
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
