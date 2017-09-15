/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.graphlibrary

import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import java.util.UUID

import org.scalatest.FunSuite

import io.deepsense.graphlibrary.Node.State.Status

class GraphSuite extends FunSuite {

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
    val graph = new Graph
    val node1 = graph.addNode(UUID.randomUUID(), new Operation(UUID.randomUUID(), 1, 1))
    val node2 = graph.addNode(UUID.randomUUID(), new Operation(UUID.randomUUID(), 1, 1))
    graph.addEdge(node1.id, node2.id, 0, 0)

    assert(graph.size == 2)
  }

  test("Non-empty Graph should be serializable") {
    // create a Graph instance
    val graph = new Graph
    graph.addNode(UUID.randomUUID(), new Operation(UUID.randomUUID(), 1, 1))

    // write the instance out to a buffer
    val bufferOut = new ByteArrayOutputStream
    val streamOut = new ObjectOutputStream(bufferOut)
    streamOut.writeObject(graph)
    streamOut.close

    // read the object back in
    val bufferIn = new ByteArrayInputStream(bufferOut.toByteArray)
    val streamIn = new ObjectInputStream(bufferIn)
    val graphIn = streamIn.readObject().asInstanceOf[Graph]

    assert(graphIn == graph)
  }

  test("Programmer can get/edit state of an execution graph nodes") {
    val graph = new Graph
    val node = graph.addNode(UUID.randomUUID(), new Operation(UUID.randomUUID(), 1, 1))

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
    // create a Graph Instance
    val graph = new Graph
    val nodeReady = graph.addNode(UUID.randomUUID(), new Operation(UUID.randomUUID(), 0, 1))
    val nodeNotReady = graph.addNode(UUID.randomUUID(), new Operation(UUID.randomUUID(), 1, 0))
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
    val graph = new Graph
    val node1 = graph.addNode(UUID.randomUUID(), new Operation(UUID.randomUUID(), 1, 1))
    val node2 = graph.addNode(UUID.randomUUID(), new Operation(UUID.randomUUID(), 1, 1))
    val node3 = graph.addNode(UUID.randomUUID(), new Operation(UUID.randomUUID(), 1, 1))
    val node4 = graph.addNode(UUID.randomUUID(), new Operation(UUID.randomUUID(), 1, 1))
    graph.addEdge(node1.id, node2.id, 0, 0)
    graph.addEdge(node2.id, node3.id, 0, 0)
    graph.addEdge(node3.id, node4.id, 0, 0)
    assert(graph.containsCycle == false)

    graph.addEdge(node4.id, node2.id, 0, 0)
    assert(graph.containsCycle == true)
  }
}
