/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.graphlibrary

import java.util.UUID

import org.scalatest.{Matchers, FunSuite}

import io.deepsense.deeplang.dhierarchy.DHierarchy
import io.deepsense.deeplang._
import io.deepsense.graphlibrary.Node.State.Status

object DClassesForDOperations {
  trait A extends DOperable
  class A1 extends A {
    override def equals(any: Any) = any.isInstanceOf[A1]
  }
  class A2 extends A {
    override def equals(any: Any) = any.isInstanceOf[A2]
  }
}

object DOperationTestClasses {
  import DClassesForDOperations._

  class DOperation0To1Test extends DOperation0To1[A1] {
    override protected def _execute(): A1 = ???
  }

  class DOperation1To0Test extends DOperation1To0[A1] {
    override protected def _execute(t0: A1): Unit = ???
  }

  class DOperation1To1Test extends DOperation1To1[A1, A] {
    override protected def _execute(t1: A1): A = ???
  }

  class DOperation2To1Test extends DOperation2To1[A1, A2, A] {
    override protected def _execute(t1: A1, t2: A2): A = ???
  }
}

class GraphSuite extends FunSuite with Matchers {

  test("An empty Graph should have size 0") {
    assert((new Graph).size == 0)
  }

  test("Adding edge to an empty Graph should produce NoSuchElementException") {
    intercept[NoSuchElementException] {
      val graph = new Graph
      graph.addEdge(UUID.randomUUID(), UUID.randomUUID(), 0, 0)
    }
  }

  test("Graph with two nodes should have size 2") {
    import DOperationTestClasses._

    val graph = new Graph
    val node1 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    val node2 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    graph.addEdge(node1.id, node2.id, 0, 0)

    assert(graph.size == 2)
  }

  test("Programmer can get/edit state of an execution graph nodes") {
    import DOperationTestClasses._

    val graph = new Graph
    val node = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)

    // node should be in draft
    assert(node.state.status == Status.INDRAFT)

    // queued
    graph.markAsQueued(node.id)
    assert(node.state.status == Status.QUEUED)

    // running
    graph.markAsRunning(node.id)
    assert(node.state.status == Status.RUNNING)

    // report some progress
    graph.reportProgress(node.id, 5)
    assert(node.state.status == Status.RUNNING)
    assert(node.state.progress.get.current == 5)

    // completed
    graph.markAsCompleted(node.id, List())
    assert(node.state.status == Status.COMPLETED)
  }

  test("Programmer can list operations ready for execution") {
    import DOperationTestClasses._

    // create a Graph Instance
    val graph = new Graph
    val nodeReady = graph.addNode(UUID.randomUUID(), new DOperation0To1Test)
    val nodeNotReady = graph.addNode(UUID.randomUUID(), new DOperation1To0Test)
    graph.addEdge(nodeReady.id, nodeNotReady.id, 0, 0)

    // queue all Nodes for execution
    graph.markAsQueued(nodeReady.id)
    graph.markAsQueued(nodeNotReady.id)

    // get list of operations ready for execution
    val readyList = graph.readyNodes()

    assert(readyList.length == 1)
    assert(readyList.head == nodeReady)
  }

  test("Programmer can validate if graph doesn't contain a cycle") {
    import DOperationTestClasses._

    val graph = new Graph
    val node1 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    val node2 = graph.addNode(UUID.randomUUID(), new DOperation2To1Test)
    val node3 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    val node4 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    graph.addEdge(node1.id, node2.id, 0, 0)
    graph.addEdge(node2.id, node3.id, 0, 0)
    graph.addEdge(node3.id, node4.id, 0, 0)
    assert(graph.containsCycle == false)

    graph.addEdge(node4.id, node2.id, 0, 1)
    assert(graph.containsCycle == true)
  }

  test("Simple Graph can be sorted topologically") {
    import DOperationTestClasses._

    val graph = new Graph
    val node1 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    val node2 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    val node3 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    val node4 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    graph.addEdge(node1.id, node2.id, 0, 0)
    graph.addEdge(node2.id, node3.id, 0, 0)
    graph.addEdge(node3.id, node4.id, 0, 0)

    val sorted = graph.topologicallySorted
    assert(sorted == Some(List(node1, node2, node3, node4)))
  }

  test("Complicated Graph can be sorted topologically") {
    import DOperationTestClasses._

    def checkIfInOrder(node1: Node, node2: Node, order: List[Node]): Unit = {
      assert(order.indexOf(node1) < order.indexOf(node2))
    }

    val graph = new Graph
    val node1 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    val node2 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    val node3 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    val node4 = graph.addNode(UUID.randomUUID(), new DOperation2To1Test)
    val node5 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    val node6 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    val node7 = graph.addNode(UUID.randomUUID(), new DOperation2To1Test)
    val edges = List(
      (node1, node2, 0, 0),
      (node1, node3, 0, 0),
      (node2, node4, 0, 0),
      (node3, node4, 0, 1),
      (node4, node5, 0, 0),
      (node4, node6, 0, 0),
      (node5, node7, 0, 0),
      (node6, node7, 0, 1))
    edges.foreach(n => graph.addEdge(n._1.id, n._2.id, n._3, n._4))

    val sortedOption = graph.topologicallySorted
    assert(sortedOption.isDefined)
    val sorted = sortedOption.get
    edges.foreach(n => checkIfInOrder(n._1, n._2, sorted))
  }

  test("Graph can infer knowledge") {
    import DClassesForDOperations._
    import DOperationTestClasses._

    val graph = new Graph
    val node1 = graph.addNode(UUID.randomUUID(), new DOperation0To1Test)
    val node2 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    val node3 = graph.addNode(UUID.randomUUID(), new DOperation1To1Test)
    val node4 = graph.addNode(UUID.randomUUID(), new DOperation2To1Test)
    val edges = List(
      (node1, node2, 0, 0),
      (node1, node3, 0, 0),
      (node2, node4, 0, 0),
      (node3, node4, 0, 1))
    edges.foreach(n => graph.addEdge(n._1.id, n._2.id, n._3, n._4))

    val hierarchy = new DHierarchy
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
}
