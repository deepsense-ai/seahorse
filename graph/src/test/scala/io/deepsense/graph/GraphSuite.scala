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

package io.deepsense.graph

import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSuite, Matchers}

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.serialization.Serialization
import io.deepsense.commons.utils.Logging
import io.deepsense.graph.DOperationTestClasses.DOperationCreateA1
import io.deepsense.graph.RandomNodeFactory.randomNode

class GraphSuite extends FunSuite with Matchers with Serialization with Logging with MockitoSugar {

  test("An empty Graph should have size 0") {
    Graph().size shouldBe 0
  }

  test("Adding edge to an empty Graph should produce NoSuchElementException") {
    a [NoSuchElementException] shouldBe thrownBy {
      val edge = Edge(Endpoint(Node.Id.randomId, 0), Endpoint(Node.Id.randomId, 0))
      Graph(Set(), Set(edge))
    }
  }

  test("Graph with two nodes should have size 2") {
    import io.deepsense.graph.DOperationTestClasses._

    val node1 = randomNode(DOperationA1ToA())
    val node2 = randomNode(DOperationA1ToA())
    val nodes = Set(node1, node2)
    val edges = Set(Edge(node1, 0, node2, 0))
    val graph = Graph(nodes, edges)
    graph.size shouldBe 2
  }

  test("Programmer can get/edit state of an execution graph nodes") {
    import io.deepsense.graph.DOperationTestClasses._
    val node = randomNode(DOperationA1ToA())
    val otherNode = randomNode(DOperationCreateA1())
    var graph = Graph(Set(node, otherNode))

    // node should be in draft
    assert(node.isDraft)

    // queued
    graph = graph.markAsQueued(node.id)
    assert(graph.node(node.id).isQueued)

    // running
    graph = graph.markAsRunning(node.id)
    assert(graph.node(node.id).isRunning)

    // report some progress
    graph = graph.reportProgress(node.id, 5)
    assert(graph.node(node.id).isRunning)
    assert(graph.node(node.id).state.progress.get.current == 5)

    // completed
    graph = graph.markAsCompleted(node.id, List())
    assert(graph.node(node.id).isCompleted)

    val changedNode = node.markAborted
    graph = graph.withChangedNode(changedNode)
    assert(graph.node(node.id) == changedNode)

    assert(graph.node(otherNode.id) == otherNode)
  }

  test("Programmer can list all nodes in graph") {
    import io.deepsense.graph.DOperationTestClasses._

    val node1 = randomNode(DOperationCreateA1())
    val node2 = randomNode(DOperationA1ToA())

    // Create a Graph instance and list of its nodes.
    val nodes = Set(node1, node2)
    var graph = Graph(nodes)

    assert(graph.nodes.size == graph.size)
    // Check equality of lists elements sets.
    assert(graph.nodes == nodes)

    val newNode = randomNode(DOperationReceiveA1())
    graph = graph.copy(nodes = graph.nodes + newNode)
    assert(graph.nodes.size == graph.size)
    // Check lack of equality of lists elements sets after new node is added to graph.
    assert(graph.nodes != nodes)
  }

  test("Programmer can list operations ready for execution") {
    import io.deepsense.graph.DOperationTestClasses._

    // create a Graph Instance
    val nodeReady = randomNode(DOperationCreateA1())
    val nodeNotReady = randomNode(DOperationReceiveA1())
    val nodes = Set(nodeReady, nodeNotReady)
    val edges = Set(Edge(nodeReady, 0, nodeNotReady, 0))
    var graph = Graph(nodes, edges)

    // queue all Nodes for execution
    graph = graph.markAsQueued(nodeReady.id).markAsQueued(nodeNotReady.id)

    // get list of operations ready for execution
    val readyList = graph.readyNodes

    assert(readyList.length == 1)
    assert(readyList.head == graph.node(nodeReady.id))
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
    var graph = Graph(nodes, nonCyclicEdges)
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

    val graph = Graph(Set(node1, node2, node3, node4), edges)
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

    val graph = Graph(Set(node1, node2, node3, node4), edges)

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
    val graph = Graph(nodes, edgesSet)

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
    val graph = Graph(nodes, edges)

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

  test("Non-empty Graph should be serializable") {
    import io.deepsense.graph.DOperationTestClasses._
    val operationWithInitializedLogger = new DOperationAToALogging
    val id = Node.Id.randomId
    val id2 = Node.Id.randomId
    val nodes = Seq(
      randomNode(DOperationCreateA1()),
      Node(id2, DOperationA1ToA()),
      Node(id, operationWithInitializedLogger),
      randomNode(DOperationA1A2ToA())
    )
    val edges = Set[Edge](
      Edge(nodes(0), 0, nodes(1), 0),
      Edge(nodes(0), 0, nodes(2), 0),
      Edge(nodes(1), 0, nodes(3), 0),
      Edge(nodes(2), 0, nodes(3), 1)
    )
    val graph = Graph(nodes.toSet, edges)
    val graphIn = serializeDeserialize(graph)
    graphIn shouldBe graph
    val operation = graphIn.node(id).operation.asInstanceOf[DOperationAToALogging]
    operation.trace("Logging just to clarify that it works after deserialization!")
    operation.tTagTI_0.tpe should not be (null)
  }

  test("UpdateState on failed Graph should do nothing") {
    val runningNode = randomNode(DOperationCreateA1()).markRunning
    val failedState = GraphState.failed(mock[FailureDescription])
    val graph = Graph(Set(runningNode), Set.empty, state = failedState)
    graph.updateState().state shouldBe failedState
  }
}
