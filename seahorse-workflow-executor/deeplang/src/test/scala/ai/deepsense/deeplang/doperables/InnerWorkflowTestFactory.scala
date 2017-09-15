/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables

import spray.json.JsObject

import ai.deepsense.deeplang.DOperation
import ai.deepsense.deeplang.doperations.ConvertType
import ai.deepsense.deeplang.doperations.custom.{Sink, Source}
import ai.deepsense.deeplang.params.custom.{InnerWorkflow, PublicParam}
import ai.deepsense.deeplang.params.selections.{MultipleColumnSelection, NameColumnSelection}
import ai.deepsense.graph.{DeeplangGraph, Edge, Node}
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

object InnerWorkflowTestFactory {

  val sourceNodeId = "2603a7b5-aaa9-40ad-9598-23f234ec5c32"
  val sinkNodeId = "d7798d5e-b1c6-4027-873e-a6d653957418"
  val innerNodeId = "b22bd79e-337d-4223-b9ee-84c2526a1b75"

  val sourceNode = Node(sourceNodeId, Source())
  val sinkNode = Node(sinkNodeId, Sink())

  private def createInnerNodeOperation(targetType: TargetTypeChoice, graphReader: GraphReader): ConvertType = {

    val params = TypeConverter()
      .setTargetType(targetType)
      .setSelectedColumns(MultipleColumnSelection(Vector(NameColumnSelection(Set("column1")))))
      .paramValuesToJson
    new ConvertType().setParamsFromJson(params, graphReader)
  }

  private def createInnerNode(targetType: TargetTypeChoice, graphReader: GraphReader): Node[DOperation] =
    Node(innerNodeId, createInnerNodeOperation(targetType, graphReader))

  def simpleGraph(graphReader: GraphReader,
    targetType: TargetTypeChoice = TargetTypeChoices.StringTargetTypeChoice()): DeeplangGraph = {
    val innerNode = createInnerNode(targetType, graphReader)
    DeeplangGraph(
      Set(sourceNode, sinkNode, innerNode),
      Set(Edge(sourceNode, 0, innerNode, 0), Edge(innerNode, 0, sinkNode, 0)))
  }

}
