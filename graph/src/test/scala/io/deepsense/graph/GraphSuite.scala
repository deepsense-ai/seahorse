/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.graph

import java.util.UUID

import org.scalatest.{FunSuite, Matchers}

import io.deepsense.commons.serialization.Serialization
import io.deepsense.deeplang._
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.parameters.ParametersSchema

object DClassesForDOperations {
  trait A extends DOperable {
    def report: Report = ???
  }
  class A1 extends A {
    override def equals(any: Any) = any.isInstanceOf[A1]
    override def save(context: ExecutionContext)(path: String): Unit = ???
  }
  class A2 extends A {
    override def equals(any: Any) = any.isInstanceOf[A2]
    override def save(context: ExecutionContext)(path: String): Unit = ???
  }
}

object DOperationTestClasses {
  import DClassesForDOperations._

  trait DOperationBaseFields extends DOperation {
    // NOTE: id will be different for each instance
    override val id: DOperation.Id = DOperation.Id.randomId

    override val name: String = ""

    override val parameters: ParametersSchema = ParametersSchema()
  }

  class DOperation0To1Test extends DOperation0To1[A1] with DOperationBaseFields {
    override protected def _execute(context: ExecutionContext)(): A1 = ???
  }

  class DOperation1To0Test extends DOperation1To0[A1] with DOperationBaseFields {
    override protected def _execute(context: ExecutionContext)(t0: A1): Unit = ???
  }

  class DOperation1To1Test extends DOperation1To1[A1, A] with DOperationBaseFields {
    override protected def _execute(context: ExecutionContext)(t1: A1): A = ???
  }

  class DOperation2To1Test extends DOperation2To1[A1, A2, A] with DOperationBaseFields {
    override protected def _execute(context: ExecutionContext)(t1: A1, t2: A2): A = ???
  }

  class DOperation1To1Logging extends DOperation1To1[A, A] with DOperationBaseFields {
    logger.trace("Initializing logging to test the serialization")
    override protected def _execute(context: ExecutionContext)(t0: A): A = ???

    def trace(message: String) = logger.trace(message)
  }
}

class GraphSuite extends FunSuite with Matchers with Serialization {

  test("An empty Graph should have size 0") {
    Graph().size shouldBe 0
  }

  test("Adding edge to an empty Graph should produce NoSuchElementException") {
    intercept[NoSuchElementException] {
      val edge = Edge(Endpoint(UUID.randomUUID(), 0), Endpoint(UUID.randomUUID(), 0))
      Graph(Set(), Set(edge))
    }
  }

  test("Graph with two nodes should have size 2") {
    import DOperationTestClasses._

    val node1 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node2 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val nodes = Set(node1, node2)
    val edges = Set(Edge(Endpoint(node1.id, 0), Endpoint(node2.id, 0)))
    val graph = Graph(nodes, edges)
    graph.size shouldBe 2
  }

  test("Programmer can get/edit state of an execution graph nodes") {
    import DOperationTestClasses._
    val node = Node(UUID.randomUUID(), new DOperation1To1Test)
    var graph = Graph(Set(node))

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
  }

  test("Programmer can list all nodes in graph") {
    import DOperationTestClasses._

    val node1 = Node(UUID.randomUUID(), new DOperation0To1Test)
    val node2 = Node(UUID.randomUUID(), new DOperation1To1Test)

    // Create a Graph instance and list of its nodes.
    val nodes = Set(node1, node2)
    var graph = Graph(nodes)

    assert(graph.nodes.size == graph.size)
    // Check equality of lists elements sets.
    assert(graph.nodes == nodes)

    val newNode = Node(UUID.randomUUID(), new DOperation1To0Test)
    graph = graph.copy(nodes = graph.nodes + newNode)
    assert(graph.nodes.size == graph.size)
    // Check lack of equality of lists elements sets after new node is added to graph.
    assert(graph.nodes != nodes)
  }

  test("Programmer can list operations ready for execution") {
    import DOperationTestClasses._

    // create a Graph Instance
    val nodeReady = Node(UUID.randomUUID(), new DOperation0To1Test)
    val nodeNotReady = Node(UUID.randomUUID(), new DOperation1To0Test)
    val nodes = Set(nodeReady, nodeNotReady)
    val edges = Set(Edge(Endpoint(nodeReady.id, 0), Endpoint(nodeNotReady.id, 0)))
    var graph = Graph(nodes, edges)

    // queue all Nodes for execution
    graph = graph.markAsQueued(nodeReady.id).markAsQueued(nodeNotReady.id)

    // get list of operations ready for execution
    val readyList = graph.readyNodes

    assert(readyList.length == 1)
    assert(readyList.head == graph.node(nodeReady.id))
  }

  test("Programmer can validate if graph doesn't contain a cycle") {
    import DOperationTestClasses._

    val node1 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node2 = Node(UUID.randomUUID(), new DOperation2To1Test)
    val node3 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node4 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val nodes = Set(node1, node2, node3, node4)
    val nonCyclicEdges = Set(
      Edge(Endpoint(node1.id, 0), Endpoint(node2.id, 0)),
      Edge(Endpoint(node2.id, 0), Endpoint(node3.id, 0)),
      Edge(Endpoint(node3.id, 0), Endpoint(node4.id, 0))
    )
    var graph = Graph(nodes, nonCyclicEdges)
    assert(!graph.containsCycle)

    graph = graph.copy(edges = graph.edges + Edge(Endpoint(node4.id, 0), Endpoint(node2.id, 1)))
    assert(graph.containsCycle)
  }

  test("Simple Graph can be sorted topologically") {
    import DOperationTestClasses._

    val node1 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node2 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node3 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node4 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val edges = Set(
      Edge(Endpoint(node1.id, 0), Endpoint(node2.id, 0)),
      Edge(Endpoint(node2.id, 0), Endpoint(node3.id, 0)),
      Edge(Endpoint(node3.id, 0), Endpoint(node4.id, 0)))

    val graph = Graph(Set(node1, node2, node3, node4), edges)
    val sorted = graph.topologicallySorted
    assert(sorted == Some(List(node1, node2, node3, node4)))
  }

  test("Complicated Graph can be sorted topologically") {
    import DOperationTestClasses._

    def checkIfInOrder(node1: Node, node2: Node, order: List[Node]): Unit = {
      assert(order.indexOf(node1) < order.indexOf(node2))
    }

    val node1 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node2 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node3 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node4 = Node(UUID.randomUUID(), new DOperation2To1Test)
    val node5 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node6 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node7 = Node(UUID.randomUUID(), new DOperation2To1Test)
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
    import DOperationTestClasses._

    val node1 = Node(UUID.randomUUID(), new DOperation0To1Test)
    val node2 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node3 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node4 = Node(UUID.randomUUID(), new DOperation2To1Test)
    val nodes = Set(node1, node2, node3, node4)
    val edges = List(
      (node1, node2, 0, 0),
      (node1, node3, 0, 0),
      (node2, node4, 0, 0),
      (node3, node4, 0, 1))
    val edgesSet = edges.map(n =>Edge(Endpoint(n._1.id, n._3), Endpoint(n._2.id, n._4))).toSet
    val graph = Graph(nodes, edgesSet)

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

  test("Graph can infer knowledge") {
    import DClassesForDOperations._
    import DOperationTestClasses._

    val node1 = Node(UUID.randomUUID(), new DOperation0To1Test)
    val node2 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node3 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node4 = Node(UUID.randomUUID(), new DOperation2To1Test)
    val nodes = Set(node1, node2, node3, node4)
    val edges = List(
      (node1, node2, 0, 0),
      (node1, node3, 0, 0),
      (node2, node4, 0, 0),
      (node3, node4, 0, 1))
    val edgesSet = edges.map(n => Edge(Endpoint(n._1.id, n._3), Endpoint(n._2.id, n._4))).toSet
    val graph = Graph(nodes, edgesSet)

    val hierarchy = new DOperableCatalog
    hierarchy.registerDOperable[A1]()
    hierarchy.registerDOperable[A2]()
    val ctx = new InferContext(hierarchy)
    val graphKnowledge = graph.inferKnowledge(ctx)

    val knowledgeA1: Vector[DKnowledge[DOperable]] = Vector(new DKnowledge(new A1))
    val knowledgeA12: Vector[DKnowledge[DOperable]] = Vector(new DKnowledge(new A1, new A2))

    val graphKnowledgeExpected = Map(
      node1.id -> knowledgeA1,
      node2.id -> knowledgeA12,
      node3.id -> knowledgeA12,
      node4.id -> knowledgeA12)

    graphKnowledge.knowledgeMap should contain theSameElementsAs graphKnowledgeExpected
  }

  test("Non-empty Graph should be serializable") {
    import DOperationTestClasses._
    val operationWithInitedLogger = new DOperation1To1Logging
    val id = UUID.randomUUID()
    val node1 = Node(UUID.randomUUID(), new DOperation0To1Test)
    val node2 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node3 = Node(id, operationWithInitedLogger)
    val node4 = Node(UUID.randomUUID(), new DOperation2To1Test)
    val edges = List(
      (node1, node2, 0, 0),
      (node1, node3, 0, 0),
      (node2, node4, 0, 0),
      (node3, node4, 0, 1))
    val edgesSet = edges.map(n => Edge(Endpoint(n._1.id, n._3), Endpoint(n._2.id, n._4))).toSet
    val graph = new Graph(Set(node1, node2, node3, node4), edgesSet)
    val graphIn = serializeDeserialize(graph)
    assert(graphIn.size == graph.size)
    // Verify that both graphs have the same nodes ids set
    assert(graphIn.nodes.map(n => n.id) == graph.nodes.map(n => n.id))
    graphIn.node(id).operation.asInstanceOf[DOperation1To1Logging]
      .trace("Logging just to clarify that it works after deserialization!")
  }
}
