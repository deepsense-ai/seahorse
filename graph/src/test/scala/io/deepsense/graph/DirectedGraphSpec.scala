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

package io.deepsense.graph

import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSuite, Matchers}

import io.deepsense.commons.serialization.Serialization
import io.deepsense.commons.utils.Logging
import io.deepsense.graph.RandomNodeFactory._

class DirectedGraphSpec
  extends FunSuite
  with Matchers
  with Serialization
  with Logging
  with MockitoSugar
  with GraphTestSupport {

  test("An empty Graph should have size 0") {
    DirectedGraph().size shouldBe 0
  }

  test("Adding edge to an empty Graph should produce IllegalArgumentException") {
    an [IllegalArgumentException] shouldBe thrownBy {
      val edge = Edge(Endpoint(Node.Id.randomId, 0), Endpoint(Node.Id.randomId, 0))
      DirectedGraph(Set(), Set(edge))
    }
  }

  test("Graph with two nodes should have size 2") {
    import io.deepsense.graph.DOperationTestClasses._

    val node1 = randomNode(DOperationA1ToA())
    val node2 = randomNode(DOperationA1ToA())
    val nodes = Set(node1, node2)
    val edges = Set(Edge(node1, 0, node2, 0))
    val graph = DirectedGraph(nodes, edges)
    graph.size shouldBe 2
  }

  test("Programmer can validate if graph doesn't contain a cycle") {
    import io.deepsense.graph.DOperationTestClasses._

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
    var graph = DirectedGraph(nodes, nonCyclicEdges)
    assert(!graph.containsCycle)

    graph = graph.copy(edges = graph.edges + Edge(Endpoint(node4.id, 0), Endpoint(node2.id, 1)))
    assert(graph.containsCycle)
  }

  test("Simple Graph can be sorted topologically") {
    import io.deepsense.graph.DOperationTestClasses._

    val node1 = randomNode(DOperationA1ToA())
    val node2 = randomNode(DOperationA1ToA())
    val node3 = randomNode(DOperationA1ToA())
    val node4 = randomNode(DOperationA1ToA())
    val edges = Set(
      Edge(node1, 0, node2, 0),
      Edge(node2, 0, node3, 0),
      Edge(node3, 0, node4, 0))

    val graph = DirectedGraph(Set(node1, node2, node3, node4), edges)
    val sorted = graph.topologicallySorted
    assert(sorted == Some(List(node1, node2, node3, node4)))
  }

  test("Simple Graph can calculate its direct and non-direct precedessors") {
    import io.deepsense.graph.DOperationTestClasses._

    val node1 = randomNode(DOperationA1ToA())
    val node2 = randomNode(DOperationA1ToA())
    val node3 = randomNode(DOperationA1ToA())
    val node4 = randomNode(DOperationA1ToA())
    val edges = Set(
      Edge(node1, 0, node2, 0),
      Edge(node2, 0, node3, 0),
      Edge(node3, 0, node4, 0))

    val graph = DirectedGraph(Set(node1, node2, node3, node4), edges)

    val predsOfNode3 = graph.allPredecessorsOf(node3.id)
    assert(predsOfNode3 == Set(node1, node2))
  }

  test("Complicated Graph can be sorted topologically") {
    import io.deepsense.graph.DOperationTestClasses._

    def checkIfInOrder(node1: Node, node2: Node, order: List[Node]): Unit = {
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
    val graph = DirectedGraph(nodes, edgesSet)

    val sortedOption = graph.topologicallySorted
    assert(sortedOption.isDefined)
    val sorted = sortedOption.get
    edges.foreach(n => checkIfInOrder(n._1, n._2, sorted))
  }

  test("Graph's nodes have correct predecessors and successors") {
    import io.deepsense.graph.DOperationTestClasses._

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
    val graph = DirectedGraph(nodes, edges)

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
    DirectedGraph().subgraph(Set()) should have size 0

    val bigGraph = DirectedGraph(nodeSet, edgeSet)
    bigGraph.subgraph(nodeSet.map(_.id)) shouldBe bigGraph

    bigGraph.subgraph(Set(idA)) shouldBe DirectedGraph(Set(nodeA), Set())

    bigGraph.subgraph(Set(idA, idB)) shouldBe
      DirectedGraph(Set(nodeA, nodeB), Set(edge1))

    bigGraph.subgraph(Set(idD)) shouldBe
      DirectedGraph(Set(nodeA, nodeB, nodeC, nodeD), Set(edge1, edge2, edge3))

    bigGraph.subgraph(Set(idE, idC)) shouldBe
      DirectedGraph(Set(nodeE, nodeC, nodeB, nodeA), Set(edge1, edge2, edge4, edge5))

    bigGraph.subgraph(Set(idD, idB)) shouldBe
      DirectedGraph(Set(nodeA, nodeB, nodeC, nodeD), Set(edge1, edge2, edge3))
  }
}
