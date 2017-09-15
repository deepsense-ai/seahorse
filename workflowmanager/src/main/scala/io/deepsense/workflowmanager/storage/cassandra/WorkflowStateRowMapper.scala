/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import com.datastax.driver.core.Row
import spray.json._

import io.deepsense.graph.Node
import io.deepsense.graph.nodestate.NodeState
import io.deepsense.models.json.graph.NodeStateJsonProtocol
import io.deepsense.models.json.workflow.EntitiesMapJsonProtocol
import io.deepsense.models.workflows.EntitiesMap
import io.deepsense.workflowmanager.storage.WorkflowStateStorage.NodeStateWithReports

class WorkflowStateRowMapper
  extends EntitiesMapJsonProtocol
  with NodeStateJsonProtocol {

  import WorkflowStateRowMapper._

  def toIdAndNodeStateWithReports(row: Row): (Node.Id, NodeStateWithReports) = {
    val stateJson = row.getString(Field.State)
    val reportsJson = Option(row.getString(Field.Reports))

    val nodeId = Node.Id(row.getUUID(Field.NodeId))
    val nodeState = stateJson.parseJson.convertTo[NodeState]
    val reports = reportsJson map { _.parseJson.convertTo[EntitiesMap] }

    (nodeId, NodeStateWithReports(nodeState, reports))
  }

  def nodeStateToCell(nodeState: NodeState): String =
    nodeState.toJson.compactPrint

  def entitiesMapToCell(entitiesMap: EntitiesMap): String =
    entitiesMap.toJson.compactPrint
}

object WorkflowStateRowMapper {
  object Field {
    val WorkflowId = "workflow_id"
    val NodeId = "node_id"
    val UpdateTime = "update_time"
    val State = "state"
    val Reports = "reports"
  }
}
