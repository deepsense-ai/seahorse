/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.graph

import java.util.UUID

import ai.deepsense.deeplang.DOperation
import ai.deepsense.graph.DeeplangGraph.DeeplangNode

case class DeeplangGraph(
    override val nodes: Set[DeeplangNode] = Set.empty,
    override val edges: Set[Edge] = Set())
  extends DirectedGraph[DOperation, DeeplangGraph](nodes, edges)
  with KnowledgeInference
  with NodeInferenceImpl {

  override def subgraph(nodes: Set[DeeplangNode], edges: Set[Edge]): DeeplangGraph =
    DeeplangGraph(nodes, edges)

  def getDatasourcesIds: Set[UUID] =
    nodes.foldLeft(Set.empty[UUID])((acc, el) => acc ++ el.value.getDatasourcesIds)
}

object DeeplangGraph {
  type DeeplangNode = Node[DOperation]
}
