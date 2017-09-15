/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.model

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.models
import io.deepsense.graph.Status.Status
import io.deepsense.graph.{Node, State}
import io.deepsense.models.workflows.{EntitiesMap, ExecutionReport}

case class ExecutionReportWithId(
  id: ExecutionReportWithId.Id,
  status: Status,
  error: Option[FailureDescription],
  nodes: Map[Node.Id, State],
  resultEntities: EntitiesMap)

object ExecutionReportWithId {
  type Id = models.Id
  val Id = models.Id

  def apply(
      id: ExecutionReportWithId.Id,
      executionReport: ExecutionReport): ExecutionReportWithId = {
    new ExecutionReportWithId(
      id,
      executionReport.status,
      executionReport.error,
      executionReport.nodes,
      executionReport.resultEntities)
  }
}
