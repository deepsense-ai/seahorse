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
import io.deepsense.models.json.workflow.WorkflowVersionUtil
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.rest.CurrentBuild
import io.deepsense.workflowmanager.storage.WorkflowWithDates

case class WorkflowRowMapper @Inject() (
    override val graphReader: GraphReader)
  extends WorkflowVersionUtil
  with Logging {

  def toWorkflow(row: Row): Workflow = {
    row.getString(WorkflowRowMapper.Workflow).parseJson.convertTo[Workflow]
  }

  def toWorkflowWithDates(row: Row): WorkflowWithDates = {
    WorkflowWithDates(toWorkflow(row),
      getDate(row, WorkflowRowMapper.Created).get,
      getDate(row, WorkflowRowMapper.Updated).get)
  }

  def workflowToCell(workflow: Workflow): String = workflow.toJson.compactPrint

  def resultsUploadTimeToCell(resultsUploadTime: DateTime): Long =
    resultsUploadTime.getMillis

  private def getDate(row: Row, column: String): Option[DateTime] = {
    Option(row.getTimestamp(column)).map(s => DateTimeConverter.fromMillis(s.getTime))
  }

  override def currentVersion: Version = CurrentBuild.version
}

object WorkflowRowMapper {
  val Id = "id"
  val Workflow = "workflow"
  val Deleted = "deleted"
  val Created = "created"
  val Updated = "updated"
}
