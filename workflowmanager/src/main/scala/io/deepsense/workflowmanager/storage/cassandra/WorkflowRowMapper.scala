/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import com.datastax.driver.core.Row
import com.google.inject.Inject
import spray.json._

import io.deepsense.deeplang.inference.InferContext
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.WorkflowJsonProtocol
import io.deepsense.models.workflows.Workflow

case class WorkflowRowMapper @Inject() (
    override val graphReader: GraphReader)
  extends WorkflowJsonProtocol {

  def fromRow(row: Row): Workflow = {
    row.getString(WorkflowRowMapper.Workflow).parseJson.convertTo[Workflow](workflowFormat)
  }

  def workflowToCell(workflow: Workflow): String = workflow.toJson.compactPrint
}

object WorkflowRowMapper {
  val Id = "id"
  val Workflow = "workflow"
  val Deleted = "deleted"
}
