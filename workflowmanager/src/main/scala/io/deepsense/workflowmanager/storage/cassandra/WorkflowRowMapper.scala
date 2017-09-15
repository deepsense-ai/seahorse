/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import com.datastax.driver.core.Row
import com.google.inject.Inject
import spray.json._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.exception.json.FailureDescriptionJsonProtocol
import io.deepsense.graph.Graph
import io.deepsense.model.json.graph.GraphJsonProtocol
import GraphJsonProtocol.GraphReader
import io.deepsense.models.workflows.Workflow


class WorkflowRowMapper @Inject() (graphReader: GraphReader)
    extends FailureDescriptionJsonProtocol {

  def fromRow(row: Row): Workflow = {
    Workflow(
      id = Workflow.Id(row.getUUID(WorkflowRowMapper.Id)),
      tenantId = row.getString(WorkflowRowMapper.TenantId),
      name = row.getString(WorkflowRowMapper.Name),
      graph = row.getString(WorkflowRowMapper.Graph).parseJson.convertTo[Graph](graphReader),
      created = DateTimeConverter.fromMillis(row.getDate(WorkflowRowMapper.Created).getTime),
      updated = DateTimeConverter.fromMillis(row.getDate(WorkflowRowMapper.Updated).getTime),
      description = row.getString(WorkflowRowMapper.Description),
      state = getState(
        row.getString(WorkflowRowMapper.StateStatus),
        row.getString(WorkflowRowMapper.StateDescription))
    )
  }

  def getState(status: String, description: String): Workflow.State = {
    val failureDescription = Option(description) match {
      case Some(s) => Some(s.parseJson.convertTo[FailureDescription])
      case _ => None
    }
    val statusEnum = Workflow.Status.withName(status)
    Workflow.State(statusEnum, failureDescription)
  }
}

object WorkflowRowMapper {
  val Id = "id"
  val TenantId = "tenantid"
  val Name = "name"
  val Description = "description"
  val Graph = "graph"
  val Created = "created"
  val Updated = "updated"
  val StateStatus = "state_status"
  val StateDescription = "state_description"
  val Deleted = "deleted"
}
