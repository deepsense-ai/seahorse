package io.deepsense.models.workflows

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.graph.Node
import io.deepsense.graph.State
import io.deepsense.graph.Status.Status

case class ExecutionReport(
    status: Status,
    error: Option[FailureDescription],
    nodes: Map[Node.Id, State],
    resultEntities: EntitiesMap)
