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

package ai.deepsense.graph

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSuite, Matchers}

import ai.deepsense.commons.serialization.Serialization
import ai.deepsense.commons.utils.Logging
import ai.deepsense.graph.DeeplangGraph.DeeplangNode
import ai.deepsense.graph.RandomNodeFactory._

class DeeplangGraphSpec
  extends FunSuite
  with Matchers
  with Serialization
  with Logging
  with MockitoSugar
  with GraphTestSupport {

  test("An empty Graph should have size 0") {
    DeeplangGraph().size shouldBe 0
  }

  test("An edge added to an empty graph should be filtered out as invalid") {
      val edge = Edge(Endpoint(Node.Id.randomId, 0), Endpoint(Node.Id.randomId, 0))
      val graph = DeeplangGraph(Set(), Set(edge))
      graph.getValidEdges shouldBe Set()
  }

  test("Graph with two nodes should have size 2") {
    import ai.deepsense.graph.DOperationTestClasses._

    val node1 = randomNode(DOperationA1ToA())
    val node2 = randomNode(DOperationA1ToA())
    val nodes = Set(node1, node2)
    val edges = Set(Edge(node1, 0, node2, 0))
    val graph = DeeplangGraph(nodes, edges)
    graph.size shouldBe 2
  }

  test("Programmer can validate if graph doesn't contain a cycle") {
    import ai.deepsense.graph.DOperationTestClasses._

    val node1 = randomNode(DOperationA1ToA())
    val node2 = randomNode(DOperationA1A2ToA())
    val node3 = randomNode(DOperationA1ToA())
    val node4 = randomNode(DOperationA1ToA())
    val nodes = Set(node1, node2, node3, node4)
    val nonCyclicEdges = Set(
      Edge(node1, 0, node2, 0),
      Edge(node2, 0, node3, 0),
      Edge(node3, 0, node4, 0)
    )
    var graph = DeeplangGraph(nodes, nonCyclicEdges)
    assert(!graph.containsCycle)

    graph = graph.copy(edges = graph.edges + Edge(Endpoint(node4.id, 0), Endpoint(node2.id, 1)))
    assert(graph.containsCycle)
  }

  test("Simple Graph can be sorted topologically") {
    import ai.deepsense.graph.DOperationTestClasses._

    val node1 = randomNode(DOperationA1ToA())
    val node2 = randomNode(DOperationA1ToA())
    val node3 = randomNode(DOperationA1ToA())
    val node4 = randomNode(DOperationA1ToA())
    val edges = Set(
      Edge(node1, 0, node2, 0),
      Edge(node2, 0, node3, 0),
      Edge(node3, 0, node4, 0))

    val graph = DeeplangGraph(Set(node1, node2, node3, node4), edges)
    val sorted = graph.topologicallySorted
    assert(sorted == Some(List(node1, node2, node3, node4)))
  }

  test("Simple Graph can calculate its direct and non-direct precedessors") {
    import ai.deepsense.graph.DOperationTestClasses._

    val node1 = randomNode(DOperationA1ToA())
    val node2 = randomNode(DOperationA1ToA())
    val node3 = randomNode(DOperationA1ToA())
    val node4 = randomNode(DOperationA1ToA())
    val edges = Set(
      Edge(node1, 0, node2, 0),
      Edge(node2, 0, node3, 0),
      Edge(node3, 0, node4, 0))

    val graph = DeeplangGraph(Set(node1, node2, node3, node4), edges)

    val predsOfNode3 = graph.allPredecessorsOf(node3.id)
    assert(predsOfNode3 == Set(node1, node2))
  }

  test("Complicated Graph can be sorted topologically") {
    import ai.deepsense.graph.DOperationTestClasses._

    def checkIfInOrder(
        node1: DeeplangNode, node2: DeeplangNode, order: List[DeeplangNode]): Unit = {
      assert(order.indexOf(node1) < order.indexOf(node2))
    }

    val node1 = randomNode(DOperationA1ToA())
    val node2 = randomNode(DOperationA1ToA())
    val node3 = randomNode(DOperationA1ToA())
    val node4 = randomNode(DOperationA1A2ToA())
    val node5 = randomNode(DOperationA1ToA())
    val node6 = randomNode(DOperationA1ToA())
    val node7 = randomNode(DOperationA1A2ToA())
    val nodes = Set(node1, node2, node3, node4, node5, node6, node7)
    val edges = List(
      (node1, node2, 0, 0),
      (node1, node3, 0, 0),
      (node2, node4, 0, 0),
      (node3, node4, 0, 1),
      (node4, node5, 0, 0),
      (node4, node6, 0, 0),
      (node5, node7, 0, 0),
      (node6, node7, 0, 1))
    val edgesSet = edges.map(n => Edge(Endpoint(n._1.id, n._3), Endpoint(n._2.id, n._4))).toSet
    val graph = DeeplangGraph(nodes, edgesSet)

    val sortedOption = graph.topologicallySorted
    assert(sortedOption.isDefined)
    val sorted = sortedOption.get
    edges.foreach(n => checkIfInOrder(n._1, n._2, sorted))
  }

  test("Graph's nodes have correct predecessors and successors") {
    import ai.deepsense.graph.DOperationTestClasses._

    val node1 = randomNode(DOperationCreateA1())
    val node2 = randomNode(DOperationA1ToA())
    val node3 = randomNode(DOperationA1ToA())
    val node4 = randomNode(DOperationA1A2ToA())
    val nodes = Set(node1, node2, node3, node4)
    val edges = Set(
      Edge(node1, 0, node2, 0),
      Edge(node1, 0, node3, 0),
      Edge(node2, 0, node4, 0),
      Edge(node3, 0, node4, 1)
    )
    val graph = DeeplangGraph(nodes, edges)

    graph.predecessors(node1.id).size shouldBe 0
    graph.predecessors(node2.id) should
      contain theSameElementsAs Vector(Some(Endpoint(node1.id, 0)))
    graph.predecessors(node3.id) should
      contain theSameElementsAs Vector(Some(Endpoint(node1.id, 0)))
    graph.predecessors(node4.id) should
      contain theSameElementsAs Vector(Some(Endpoint(node2.id, 0)), Some(Endpoint(node3.id, 0)))

    graph.successors(node1.id) should contain theSameElementsAs
      Vector(Set(Endpoint(node2.id, 0), Endpoint(node3.id, 0)))
    graph.successors(node2.id) should contain theSameElementsAs Vector(Set(Endpoint(node4.id, 0)))
    graph.successors(node3.id) should contain theSameElementsAs Vector(Set(Endpoint(node4.id, 1)))
    graph.successors(node4.id) should contain theSameElementsAs Vector(Set.empty)
  }

  test("Graph allows to calculate a subgraph") {
    DeeplangGraph().subgraph(Set()) should have size 0

    val bigGraph = DeeplangGraph(nodeSet, edgeSet)
    bigGraph.subgraph(nodeSet.map(_.id)) shouldBe bigGraph

    bigGraph.subgraph(Set(idA)) shouldBe DeeplangGraph(Set(nodeA), Set())

    bigGraph.subgraph(Set(idA, idB)) shouldBe
      DeeplangGraph(Set(nodeA, nodeB), Set(edge1))

    bigGraph.subgraph(Set(idD)) shouldBe
      DeeplangGraph(Set(nodeA, nodeB, nodeC, nodeD), Set(edge1, edge2, edge3))

    bigGraph.subgraph(Set(idE, idC)) shouldBe
      DeeplangGraph(Set(nodeE, nodeC, nodeB, nodeA), Set(edge1, edge2, edge4, edge5))

    bigGraph.subgraph(Set(idD, idB)) shouldBe
      DeeplangGraph(Set(nodeA, nodeB, nodeC, nodeD), Set(edge1, edge2, edge3))
  }

  test("Graph with a cycle allows to calculate a subgraph") {
    val node1 = Node(Node.Id.randomId, op1To1)
    val node2 = Node(Node.Id.randomId, op1To1)
    val cyclicGraph = DeeplangGraph(
      Set(node1, node2),
      Set(
        Edge(node1, 0, node2, 0),
        Edge(node2, 0, node1, 0))
    )

    cyclicGraph.subgraph(Set(node1.id)) shouldBe cyclicGraph
  }
}
