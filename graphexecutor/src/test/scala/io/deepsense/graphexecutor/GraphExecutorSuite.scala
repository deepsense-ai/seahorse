/**
 * Copyright (c) 2015, CodiLime Inc.
 */
package io.deepsense.graphexecutor

import java.util.UUID

import org.scalatest.{WordSpec, FunSuite, Matchers}

import io.deepsense.commons.serialization.Serialization
import io.deepsense.deeplang.doperations.{LoadDataFrame, SaveDataFrame}
import io.deepsense.graph.Status._
import io.deepsense.graph.{Edge, Endpoint, Graph, Node}
import io.deepsense.models.entities.Entity

class GraphExecutorSuite
  extends WordSpec
  with Matchers
  with Serialization {

  "Mocked graph with parameters" should {
    "be serializable" in {
      val graph = createMockedGraph(Entity.Id.randomId.toString, "testing DF name")
      val baos = serialize[Graph](graph)
      val graphIn = deserialize[Graph](baos)

      val nodeA = graph.nodes.toList(0)
      val nodeInA = graphIn.node(nodeA.id)
      assert(nodeA.operation.parameters.getString(LoadDataFrame.idParam)
        == nodeInA.operation.parameters.getString(LoadDataFrame.idParam))

      val nodeB = graph.nodes.toList(1)
      val nodeInB = graphIn.node(nodeB.id)
      assert(nodeB.operation.parameters.getString(SaveDataFrame.nameParam)
        == nodeInB.operation.parameters.getString(SaveDataFrame.nameParam))
    }
  }

  "GraphExecutorClient" should {
    "return no graph when asking for executionStatus before spawn" in {
      GraphExecutorClient().getExecutionState() shouldBe None
    }
  }

  /**
   * Creates complex mocked graph
   * @return Mocked graph
   */
  def createMockedGraph(dfId: String, dfName: String): Graph = {
    val graph = new Graph
    val loadOp = new LoadDataFrame
    loadOp.parameters.getStringParameter(LoadDataFrame.idParam).value = Some(dfId)

    val saveOp = new SaveDataFrame
    saveOp.parameters.getStringParameter(SaveDataFrame.nameParam).value = Some(dfName)

    val node1 = Node(UUID.randomUUID(), loadOp)
    val node2 = Node(UUID.randomUUID(), saveOp)
    val nodes = Set(node1, node2)
    val edgesList = List(
      (node1, node2, 0, 0))
    val edges = edgesList.map(n => Edge(Endpoint(n._1.id,  n._3), Endpoint(n._2.id, n._4))).toSet
    Graph(nodes, edges)
  }

}
