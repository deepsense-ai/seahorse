/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.graph

case class Endpoint(nodeId: Node.Id, portIndex: Int)
case class Edge(from: Endpoint, to: Endpoint)
