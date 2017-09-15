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

package ai.deepsense.models.json.graph

import org.mockito.Mockito._
import spray.json._

import ai.deepsense.deeplang.DOperation
import ai.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import ai.deepsense.graph.DeeplangGraph.DeeplangNode
import ai.deepsense.graph._
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

class GraphReaderSpec extends GraphJsonTestSupport {

  val catalog = mock[DOperationsCatalog]
  implicit val graphReader = new GraphReader(catalog)

  val operation1 = mockOperation(0, 1, DOperation.Id.randomId, "DataSet1")
  val operation2 = mockOperation(1, 1, DOperation.Id.randomId, "DoSomething")
  val operation3 = mockOperation(1, 0, DOperation.Id.randomId, "SaveDataSet")

  when(catalog.createDOperation(operation1.id)).thenReturn(operation1)
  when(catalog.createDOperation(operation2.id)).thenReturn(operation2)
  when(catalog.createDOperation(operation3.id)).thenReturn(operation3)

  val node1Id = Node.Id.randomId
  val node2Id = Node.Id.randomId
  val node3Id = Node.Id.randomId

  val parameters1 = JsObject("name" -> "param1".toJson)
  val parameters2 = JsObject("name" -> "param2".toJson)
  val parameters3 = JsObject("name" -> "param3".toJson)

  val nodesArray = JsArray(
    JsObject(
      "id" -> node1Id.toString.toJson,
      "operation" -> JsObject(
        "id" -> operation1.id.toString.toJson,
        "name" -> operation1.name.toString.toJson
      ),
      "parameters" -> parameters1
    ),
    JsObject(
      "id" -> node2Id.toString.toJson,
      "operation" -> JsObject(
        "id" -> operation2.id.toString.toJson,
        "name" -> operation2.name.toString.toJson
      ),
      "parameters" -> parameters2
    ),
    JsObject(
      "id" -> node3Id.toString.toJson,
      "operation" -> JsObject(
        "id" -> operation3.id.toString.toJson,
        "name" -> operation3.name.toString.toJson
      ),
      "parameters" -> parameters3
    )
  )

  val edge1from, edge1to, edge2from, edge2to = 0

  val edgesArray = JsArray(
    JsObject(
      "from" -> JsObject(
        "nodeId" -> node1Id.toString.toJson,
        "portIndex" -> edge1from.toJson
      ),
      "to" -> JsObject(
        "nodeId" -> node2Id.toString.toJson,
        "portIndex" -> edge1to.toJson
      )
    ),

    JsObject(
      "from" -> JsObject(
        "nodeId" -> node2Id.toString.toJson,
        "portIndex" -> edge2from.toJson
      ),
      "to" -> JsObject(
        "nodeId" -> node3Id.toString.toJson,
        "portIndex" -> edge2to.toJson
      )
    )
  )

  val exampleJson = JsObject(
    "nodes" -> nodesArray,
    "connections" -> edgesArray
  )

  val expectedGraph = DeeplangGraph(
    Set(Node(node1Id, operation1), Node(node2Id, operation2), Node(node3Id, operation3)),
    Set(Edge(Endpoint(node1Id, edge1from), Endpoint(node2Id, edge1to)),
      Edge(Endpoint(node2Id, edge2from), Endpoint(node3Id, edge2to)))
  )

  "GraphReader" should {
    "create Graph from JSON and fill parameters with values from Json" in {
      graphsSimilar(exampleJson.convertTo[DeeplangGraph], expectedGraph) shouldBe true
      verify(operation1).setParamsFromJson(parameters1, graphReader)
      verify(operation2).setParamsFromJson(parameters2, graphReader)
      verify(operation3).setParamsFromJson(parameters3, graphReader)
    }
  }

  def graphsSimilar(g1: DeeplangGraph, g2: DeeplangGraph): Boolean = {
    g1.edges == g2.edges &&
      g1.nodes.size == g2.nodes.size &&
      nodesSimilar(g1.nodes, g2.nodes)
  }


  def nodesSimilar(nodes1: Set[DeeplangNode], nodes2: Set[DeeplangNode]): Boolean = {
    val testNodes1 = nodes1.map(node => TestNode(node.id, node.value))
    val testNodes2 = nodes2.map(node => TestNode(node.id, node.value))
    testNodes1 == testNodes2
  }

  case class TestNode(id: Node.Id, operation: DOperation)
}
