/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphjson

import org.mockito.Mockito._
import spray.json._

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.graph.{Edge, Endpoint, Graph, Node}
import io.deepsense.model.json.graph.GraphJsonProtocol
import GraphJsonProtocol.GraphReader

class GraphReaderSpec extends GraphJsonTestSupport {

  val catalog = mock[DOperationsCatalog]
  implicit val graphReader = new GraphReader(catalog)

  val operation1 = mockOperation(0, 1, DOperation.Id.randomId, "DataSet1", "1.1.0")
  val operation2 = mockOperation(1, 1, DOperation.Id.randomId, "DoSomething", "1.2.0")
  val operation3 = mockOperation(1, 0, DOperation.Id.randomId, "SaveDataSet", "1.3.0")

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
        "name" -> operation1.name.toString.toJson,
        "version" -> operation1.version.toString.toJson
      ),
      "parameters" -> parameters1
    ),
    JsObject(
      "id" -> node2Id.toString.toJson,
      "operation" -> JsObject(
        "id" -> operation2.id.toString.toJson,
        "name" -> operation2.name.toString.toJson,
        "version" -> operation2.version.toString.toJson
      ),
      "parameters" -> parameters2
    ),
    JsObject(
      "id" -> node3Id.toString.toJson,
      "operation" -> JsObject(
        "id" -> operation3.id.toString.toJson,
        "name" -> operation3.name.toString.toJson,
        "version" -> operation3.version.toString.toJson
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
    "edges" -> edgesArray
  )

  val expectedGraph = Graph(
    Set(Node(node1Id, operation1), Node(node2Id, operation2), Node(node3Id, operation3)),
    Set(Edge(Endpoint(node1Id, edge1from), Endpoint(node2Id, edge1to)),
      Edge(Endpoint(node2Id, edge2from), Endpoint(node3Id, edge2to)))
  )

  "GraphReader" should {
    "create Graph from JSON and fill parameters with values from Json" in {
      graphsSimilar(exampleJson.convertTo[Graph], expectedGraph) shouldBe true
      verify(operation1.parameters).fillValuesWithJson(parameters1)
      verify(operation2.parameters).fillValuesWithJson(parameters2)
      verify(operation3.parameters).fillValuesWithJson(parameters3)
    }
  }

  def graphsSimilar(g1: Graph, g2: Graph): Boolean = {
    g1.edges == g2.edges &&
      g1.nodes.size == g2.nodes.size &&
      nodesSimilar(g1.nodes, g2.nodes)
  }


  def nodesSimilar(nodes1: Set[Node], nodes2: Set[Node]): Boolean = {
    val testNodes1 = nodes1.map(node => TestNode(node.id, node.operation))
    val testNodes2 = nodes2.map(node => TestNode(node.id, node.operation))
    testNodes1 == testNodes2
  }

  case class TestNode(id: Node.Id, operation: DOperation)
}
