/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import com.datastax.driver.core.Row
import spray.json._

import io.deepsense.graph.Node
import io.deepsense.graph.nodestate.NodeStatus
import io.deepsense.models.json.graph.NodeStatusJsonProtocol
import io.deepsense.models.json.workflow.EntitiesMapJsonProtocol
import io.deepsense.models.workflows.{EntitiesMap, NodeState}

class WorkflowStateRowMapper
  extends EntitiesMapJsonProtocol
  with NodeStatusJsonProtocol {

  import WorkflowStateRowMapper._

  def toIdAndNodeState(row: Row): (Node.Id, NodeState) = {
    val statusJson = row.getString(Field.State)
    val reportsJson = Option(row.getString(Field.Reports))
    val nodeId = Node.Id(row.getUUID(Field.NodeId))
    val nodeStatus = statusJson.parseJson.convertTo[NodeStatus]
    val reports = reportsJson map { _.parseJson.convertTo[EntitiesMap] }
    (nodeId, NodeState(nodeStatus, reports))
  }

  def nodeStatusToCell(nodeStatus: NodeStatus): String =
    nodeStatus.toJson.compactPrint

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
