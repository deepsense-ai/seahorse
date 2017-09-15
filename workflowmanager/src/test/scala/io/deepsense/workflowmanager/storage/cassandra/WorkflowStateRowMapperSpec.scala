/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import com.datastax.driver.core.Row
import org.mockito.Mockito._
import spray.json._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.graph.nodestate.NodeStatus
import io.deepsense.graph.{Node, nodestate}
import io.deepsense.models.json.graph.NodeStatusJsonProtocol
import io.deepsense.models.json.workflow.EntitiesMapJsonProtocol
import io.deepsense.models.workflows.{EntitiesMap, NodeState}
import io.deepsense.workflowmanager.storage.cassandra.WorkflowStateRowMapper.Field

class WorkflowStateRowMapperSpec
  extends StandardSpec
  with UnitTestSupport
  with NodeStatusJsonProtocol
  with EntitiesMapJsonProtocol {

  val mapper = new WorkflowStateRowMapper()

  "WorkflowStateRowMapper" should {
    "convert node state to cell" in {
      val ns: NodeStatus = nodestate.Completed(DateTimeConverter.now, DateTimeConverter.now, Seq())
      mapper.nodeStatusToCell(ns) shouldBe ns.toJson.compactPrint
    }

    "convert entities map to cell" in {
      val em: EntitiesMap = EntitiesMap()
      mapper.entitiesMapToCell(em) shouldBe em.toJson.compactPrint
    }

    "convert row to node state with reports" in {
      val nodeId = Node.Id.randomId
      val state = nodestate.Completed(DateTimeConverter.now, DateTimeConverter.now, Seq())
      val reports = Some(EntitiesMap())

      mapper.toIdAndNodeState(aRow(nodeId, state, reports)) shouldBe (
        nodeId, NodeState(state, reports))
    }

    "convert row to node state with null reports" in {
      val nodeId = Node.Id.randomId
      val state = nodestate.Completed(DateTimeConverter.now, DateTimeConverter.now, Seq())
      val reports = None

      mapper.toIdAndNodeState(aRow(nodeId, state, reports)) shouldBe (
        nodeId, NodeState(state, reports))
    }
  }

  private def aRow(nodeId: Node.Id, status: NodeStatus, reports: Option[EntitiesMap]): Row = {
    val row = mock[Row]
    when(row.getUUID(Field.NodeId)).thenReturn(nodeId.value)
    when(row.getString(Field.State)).thenReturn(status.toJson.compactPrint)
    when(row.getString(Field.Reports)).thenReturn(reports.map(_.toJson.compactPrint).orNull)
    row
  }
}
