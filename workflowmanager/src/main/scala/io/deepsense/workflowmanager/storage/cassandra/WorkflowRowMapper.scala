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
import io.deepsense.workflowmanager.storage.{WorkflowWithDates, WorkflowStorage}

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

  def toWorkflowWithDates(row: Row): WorkflowWithDates = {
    WorkflowWithDates(toWorkflow(row),
      getDate(row, WorkflowRowMapper.Created).get,
      getDate(row, WorkflowRowMapper.Updated).get)
  }

  def toResultsUploadTime(row: Row): Option[DateTime] = {
    getDate(row, WorkflowRowMapper.ResultsUploadTime)
  }

  def workflowToCell(workflow: Workflow): String = workflow.toJson.compactPrint

  def resultsToCell(results: WorkflowWithSavedResults): String = results.toJson.compactPrint

  def resultsUploadTimeToCell(resultsUploadTime: DateTime): Long =
    resultsUploadTime.getMillis

  private def getDate(row: Row, column: String): Option[DateTime] = {
    Option(row.getDate(column)).map(s => DateTimeConverter.fromMillis(s.getTime))
  }

  override def currentVersion: Version = CurrentBuild.version
}

object WorkflowRowMapper {
  val Id = "id"
  val Workflow = "workflow"
  val Results = "results"
  val ResultsUploadTime = "results_upload_time"
  val Deleted = "deleted"
  val Created = "created"
  val Updated = "updated"
}
