/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import org.scalatest.{Matchers, WordSpec}

import io.deepsense.commons.serialization.Serialization
import io.deepsense.deeplang.doperations.{LoadDataFrame, SaveDataFrame}
import io.deepsense.graph.{Edge, Graph, Node}
import io.deepsense.models.entities.Entity

class GraphExecutorSuite
  extends WordSpec
  with Matchers
  with Serialization {

  "Mocked graph with parameters" should {
    "be serializable" in {
      val graph = createMockedGraph(Entity.Id.randomId.toString, "testing DF name")
      serializeDeserialize(graph) shouldBe graph
    }
  }

  /**
   * Creates complex mocked graph
   * @return Mocked graph
   */
  def createMockedGraph(dfId: String, dfName: String): Graph = {
    val loadOp = LoadDataFrame(dfId)
    val saveOp = SaveDataFrame(dfName)

    val node1 = Node(Node.Id.randomId, loadOp)
    val node2 = Node(Node.Id.randomId, saveOp)
    val nodes = Set(node1, node2)
    val edges = Set(Edge(node1, 0, node2, 0))
    Graph(nodes, edges)
  }

}
