/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.models.workflows

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.graph.Node
import io.deepsense.graph.nodestate.{Completed, NodeStatus}

case class ExecutionReport(
    states: Map[Node.Id, NodeState],
    error: Option[FailureDescription] = None) {

  def nodesStatuses: Map[Node.Id, NodeStatus] = states.mapValues(_.nodeStatus)

  def resultEntities: EntitiesMap = {
    val combinedEntities = states.valuesIterator.flatMap(_.reportEntities().toSeq).toMap
    EntitiesMap(combinedEntities)
  }
}

object ExecutionReport {

  def apply(
      nodes: Map[Node.Id, NodeStatus],
      resultEntities: EntitiesMap,
      error: Option[FailureDescription]): ExecutionReport = {
    ExecutionReport(toNodeStates(nodes, resultEntities), error)
  }

  def statesOnly(
      nodes: Map[Node.Id, NodeStatus],
      error: Option[FailureDescription]): ExecutionReport = {
    ExecutionReport(nodes.mapValues(status => NodeState(status, None)), error)
  }

  private def toNodeStates(
      nodes: Map[Node.Id, NodeStatus],
      resultEntities: EntitiesMap): Map[Node.Id, NodeState] = {
    nodes.map {case (id, status) => status match {
      case Completed(_, _, results) =>
        id -> NodeState(status, Some(resultEntities.subMap(results.toSet)))
      case _ => id -> NodeState(status, None)
    }}
  }
}
