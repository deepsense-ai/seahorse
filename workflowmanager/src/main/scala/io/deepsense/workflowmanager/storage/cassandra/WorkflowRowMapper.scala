/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import com.datastax.driver.core.Row
import com.google.inject.Inject
import org.joda.time.DateTime
import spray.json._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.utils.{Logging, Version}
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.{WorkflowVersionUtil, WorkflowWithSavedResultsJsonProtocol}
import io.deepsense.models.workflows.{Workflow, WorkflowWithSavedResults}
import io.deepsense.workflowmanager.rest.CurrentBuild

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

  def toLastExecutionTime(row: Row): Option[DateTime] =
    Option(row.getString(WorkflowRowMapper.LastExecutionTime))
      .map(DateTimeConverter.parseDateTime)

  def workflowToCell(workflow: Workflow): String = workflow.toJson.compactPrint

  def resultsToCell(results: WorkflowWithSavedResults): String = results.toJson.compactPrint

  def lastExecutionTimeToCell(lastExecutionTime: DateTime): String =
    DateTimeConverter.toString(lastExecutionTime)

  override def currentVersion: Version = CurrentBuild.version
}

object WorkflowRowMapper {
  val Id = "id"
  val Workflow = "workflow"
  val Results = "results"
  val LastExecutionTime = "last_execution_time"
  val Deleted = "deleted"
}
