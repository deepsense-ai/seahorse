/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.model.json.graph

import spray.json._

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.graph.{Edge, Graph, Node}
import io.deepsense.model.json.graph.OperationJsonProtocol.DOperationReader

object GraphJsonProtocol {

  import EdgeJsonProtocol._
  import NodeJsonProtocol._

  val Nodes = "nodes"
  val Edges = "edges"
  val NodeId = "id"

  class GraphReader(catalog: DOperationsCatalog)
    extends JsonReader[Graph]
    with DefaultJsonProtocol {

    private val dOperationReader = new DOperationReader(catalog)

    override def read(json: JsValue): Graph = json match {
      case JsObject(fields) => read(fields)
      case x =>
        throw new DeserializationException(s"Expected JsObject with a Graph but got $x")
    }

    private def readNode(nodeJs: JsValue): Node = nodeJs match {
      case JsObject(fields) =>
        val nodeId = fields(NodeId).convertTo[String]
        val operation = nodeJs.convertTo[DOperation](dOperationReader)
        Node(Node.Id.fromString(nodeId), operation)
      case x =>
        throw new DeserializationException(s"Expected JsObject with a node but got $x")
    }

    private def readNodes(nodesJs: JsValue): Set[Node] = nodesJs match {
      case JsArray(elements) => elements.map(readNode).toSet
      case x =>
        throw new DeserializationException(s"Expected JsArray with nodes but got $x")
    }

    private def readEdges(edgesJs: JsValue): Set[Edge] = edgesJs match {
      case JsArray(elements) => elements.map(_.convertTo[Edge]).toSet
      case x =>
        throw new DeserializationException(s"Expected JsArray with edges but got $x")
    }

    private def read(fields: Map[String, JsValue]): Graph = {
      val nodes = readNodes(fields(Nodes))
      val edges = readEdges(fields(Edges))
      Graph(nodes, edges)
    }
  }

  implicit object GraphWriter extends JsonWriter[Graph] with DefaultJsonProtocol {
    override def write(graph: Graph): JsValue = {
      JsObject(
        Nodes -> JsArray(graph.nodes.map(_.toJson).toVector),
        Edges -> JsArray(graph.edges.map(_.toJson).toVector))
    }
  }
}
