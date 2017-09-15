/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.graphexecutor

import java.util.UUID

import org.scalatest.{FunSuite, Matchers}

import io.deepsense.commons.serialization.Serialization
import io.deepsense.deeplang.doperations.{ReadDataFrame, WriteDataFrame}
import io.deepsense.graph.{Edge, Endpoint, Graph, Node}
import io.deepsense.models.entities.Entity

class GraphExecutorSuite extends FunSuite with Matchers with Serialization {

  test("Mocked graph with parameters should be serializable") {
    val graph = createMockedGraph(Entity.Id.randomId.toString, "testing DF name")
    val baos = serialize[Graph](graph)
    val graphIn = deserialize[Graph](baos)

    val nodeA = graph.nodes.toList(0)
    val nodeInA = graphIn.node(nodeA.id)
    assert(nodeA.operation.parameters.getString(ReadDataFrame.idParam)
      == nodeInA.operation.parameters.getString(ReadDataFrame.idParam))

    val nodeB = graph.nodes.toList(1)
    val nodeInB = graphIn.node(nodeB.id)
    assert(nodeB.operation.parameters.getString(WriteDataFrame.nameParam)
      == nodeInB.operation.parameters.getString(WriteDataFrame.nameParam))
  }

  /**
   * Creates complex mocked graph
   * @return Mocked graph
   */
  def createMockedGraph(dfId: String, dfName: String): Graph = {
    val graph = new Graph
    val readOp = new ReadDataFrame
    readOp.parameters.getStringParameter(ReadDataFrame.idParam).value = Some(dfId)

    val writeOp = new WriteDataFrame
    writeOp.parameters.getStringParameter(WriteDataFrame.nameParam).value = Some(dfName)

    val node1 = Node(UUID.randomUUID(), readOp)
    val node2 = Node(UUID.randomUUID(), writeOp)
    val nodes = Set(node1, node2)
    val edgesList = List(
      (node1, node2, 0, 0))
    val edges = edgesList.map(n => Edge(Endpoint(n._1.id,  n._3), Endpoint(n._2.id, n._4))).toSet
    Graph(nodes, edges)
  }

}
