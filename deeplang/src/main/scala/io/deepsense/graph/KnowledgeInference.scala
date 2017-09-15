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

package io.deepsense.graph

import io.deepsense.deeplang._
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.graph.GraphKnowledge.InferenceErrors

case class SinglePortKnowledgeInferenceResult(
  knowledge: DKnowledge[DOperable],
  warnings: InferenceWarnings,
  errors: InferenceErrors)

trait KnowledgeInference {
  self: TopologicallySortable[DOperation] with NodeInference =>

  /**
   * @return A graph knowledge with inferred results for every node.
   */
  def inferKnowledge(context: InferContext): GraphKnowledge = {
    val sorted = topologicallySorted.getOrElse(throw CyclicGraphException())
    sorted
      .foldLeft(GraphKnowledge())(
        (knowledge, node) => {
          val inferenceResult = inferKnowledge(
            node,
            context,
            inputInferenceForNode(
              node,
              context,
              knowledge,
              predecessors(node.id)))
          knowledge.addInference(node.id, inferenceResult)
        }
      )
  }

  /**
   * @return A graph knowledge with knowledge inferred up to given node and port.
   */
  def inferKnowledge(
      nodeId: Node.Id,
      outPortIndex: Int,
      context: InferContext): SinglePortKnowledgeInferenceResult = {
    val subgraphNodes = allPredecessorsOf(nodeId) + node(nodeId)
    val subgraphEdges = edges.filter(edge =>
      subgraphNodes.contains(node(edge.from.nodeId)) &&
        subgraphNodes.contains(node(edge.to.nodeId)))
    val inferenceResult =
      DeeplangGraph(subgraphNodes, subgraphEdges).inferKnowledge(context).getResult(nodeId)
    SinglePortKnowledgeInferenceResult(
      inferenceResult.ports(outPortIndex),
      inferenceResult.warnings,
      inferenceResult.errors)
  }
}
