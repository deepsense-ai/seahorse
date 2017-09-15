/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.graphexecutor

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.util.UUID

import org.scalatest.{FunSuite, Matchers}

import io.deepsense.deeplang.doperations.{ReadDataFrame, WriteDataFrame}
import io.deepsense.graph.{Edge, Endpoint, Graph, Node}

class GraphExecutorSuite extends FunSuite with Matchers {

  test("Mocked graph with parameters should be serializable") {
    val graph = createMockedGraph("/in", "/out")
    val baos = serializeGraph(graph)
    val graphIn = deserializeGraph(baos)

    val nodeA = graph.nodes.toList(0)
    val nodeInA = graphIn.node(nodeA.id)
    assert(nodeA.operation.parameters.getString("path")
      == nodeInA.operation.parameters.getString("path"))

    val nodeB = graph.nodes.toList(1)
    val nodeInB = graphIn.node(nodeB.id)
    assert(nodeB.operation.parameters.getString("path")
      == nodeInB.operation.parameters.getString("path"))
  }

  private def serializeGraph(graph: Graph): ByteArrayOutputStream = {
    val bytesOut = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(bytesOut)
    oos.writeObject(graph)
    oos.flush()
    oos.close()
    bytesOut
  }

  private def deserializeGraph(baos: ByteArrayOutputStream): Graph = {
    val bufferIn = new ByteArrayInputStream(baos.toByteArray)
    val streamIn = new ObjectInputStream(bufferIn)
    streamIn.readObject().asInstanceOf[Graph]
  }

  /**
   * Creates complex mocked graph
   * @return Mocked graph
   */
  def createMockedGraph(inPath: String, outPath: String): Graph = {
    val graph = new Graph
    val readOp = new ReadDataFrame
    readOp.parameters.getStringParameter("path").value = Some(inPath)

    val writeOp = new WriteDataFrame
    writeOp.parameters.getStringParameter("path").value = Some(outPath)

    val node1 = Node(UUID.randomUUID(), readOp)
    val node2 = Node(UUID.randomUUID(), writeOp)
    val nodes = Set(node1, node2)
    val edgesList = List(
      (node1, node2, 0, 0))
    val edges = edgesList.map(n => Edge(Endpoint(n._1.id,  n._3), Endpoint(n._2.id, n._4))).toSet
    Graph(nodes, edges)
  }

}
