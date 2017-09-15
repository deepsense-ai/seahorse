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

package io.deepsense.models.json.graph

import spray.json._

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.graph.{Edge, Node, StatefulGraph}
import io.deepsense.models.json.graph.OperationJsonProtocol.DOperationReader

object GraphJsonProtocol {

  import EdgeJsonProtocol._
  import NodeJsonProtocol._

  val Nodes = "nodes"
  val Edges = "connections"  // TODO rename in code: https://codilime.atlassian.net/browse/DS-1192
  val NodeId = "id"

  class GraphReader(catalog: DOperationsCatalog)
    extends JsonReader[StatefulGraph]
    with DefaultJsonProtocol {

    private val dOperationReader = new DOperationReader(catalog)

    override def read(json: JsValue): StatefulGraph = json match {
      case JsObject(fields) => read(fields)
      case x =>
        throw new DeserializationException(s"Expected JsObject with a Graph but got $x")
    }

    private def readNode(nodeJs: JsValue): Node = nodeJs match {
      case JsObject(fields) =>
        val nodeId = try {
          fields(NodeId).convertTo[String]
        } catch { case e =>
          throw new DeserializationException(s"Node is missing a string field '$NodeId'", e)
        }
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

    private def read(fields: Map[String, JsValue]): StatefulGraph = {
      val nodes: Set[Node] = fields.get(Nodes).map(readNodes).getOrElse(Set())
      val edges: Set[Edge] = fields.get(Edges).map(readEdges).getOrElse(Set())
      StatefulGraph(nodes, edges)
    }
  }

  implicit object GraphWriter extends JsonWriter[StatefulGraph] with DefaultJsonProtocol {
    override def write(graph: StatefulGraph): JsValue = {
      JsObject(
        Nodes -> JsArray(graph.directedGraph.nodes.map(_.toJson).toVector),
        Edges -> JsArray(graph.directedGraph.edges.map(_.toJson).toVector))
    }
  }
}
