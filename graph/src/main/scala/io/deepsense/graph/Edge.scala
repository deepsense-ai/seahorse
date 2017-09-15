/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.graph

case class Endpoint(nodeId: Node.Id, portIndex: Int)

case class Edge(from: Endpoint, to: Endpoint)

object Edge {
  def apply(node1: Node, portIndex1: Int, node2: Node, portIndex2: Int): Edge =
    Edge(Endpoint(node1.id, portIndex1), Endpoint(node2.id, portIndex2))
}
