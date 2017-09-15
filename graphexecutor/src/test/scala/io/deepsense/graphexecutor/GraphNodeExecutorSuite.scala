/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.graphexecutor

import java.util.UUID

import scala.collection.mutable

import org.scalatest.{FunSuite, Matchers}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.DOperableMock
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.graph.{Edge, Endpoint, Graph, Node}

class GraphNodeExecutorSuite extends FunSuite with Matchers {

  test("GraphNodeExecutor should properly collect DOperables to pass them between DOperations") {
    val executionContext = new ExecutionContext()
    val dOperableCache = mutable.Map[UUID, DOperable]()

    val nodeUuid1 = UUID.randomUUID()
    val nodeUuid2 = UUID.randomUUID()
    var graph = createMockedGraphWithTwoNodesOfHigherArity(nodeUuid1, nodeUuid2)
    val resultUuid1 = UUID.randomUUID()
    val resultUuid2 = UUID.randomUUID()
    val resultDataframe1 = new DataFrame()
    val resultDataframe2 = new DataFrame()

    val graphNodeExecutor1 =
      new GraphNodeExecutor(executionContext, null, graph.node(nodeUuid1), null)
    val collectedOutput1 = graphNodeExecutor1.collectOutputs(graph, dOperableCache)
    // Node 1 has input arity == 0, vector should be empty
    collectedOutput1 shouldBe Vector()

    // Mark first node as completed, prepare its output for consecutive nodes
    graph = graph.markAsRunning(nodeUuid1)
    graph = graph.markAsCompleted(nodeUuid1, List(resultUuid1, resultUuid2))
    dOperableCache.put(resultUuid1, resultDataframe1)
    dOperableCache.put(resultUuid2, resultDataframe2)

    val graphNodeExecutor2 =
      new GraphNodeExecutor(executionContext, null, graph.node(nodeUuid2), null)
    val collectedOutput2 = graphNodeExecutor2.collectOutputs(graph, dOperableCache)
    // NOTE: node1 first (second) output goes to second (first) input port of node2
    collectedOutput2 shouldBe Vector(resultDataframe2, resultDataframe1)
  }

  // TODO: this classes could be replaced with real DOperations with the same arity
  object DClassesForDOperations {
    trait A extends DOperableMock
    class A1 extends A {
      override def equals(any: Any) = any.isInstanceOf[A1]
    }
    class A2 extends A {
      override def equals(any: Any) = any.isInstanceOf[A2]
    }
  }

  object DOperationTestClasses {
    import DClassesForDOperations._

    class DOperation0To2Test extends DOperation0To2[A1, A1] {
      override val id: Id = UUID.randomUUID()

      override protected def _execute(context: ExecutionContext)(): (A1, A1) = {
        Thread.sleep(10000)
        (new A1(), new A1())
      }

      override val name: String = ""

      override val parameters = ParametersSchema()
    }

    class DOperation2To1Test extends DOperation2To1[A1, A1, A] {
      override val id: Id = UUID.randomUUID()

      override protected def _execute(context: ExecutionContext)(t0: A1, t1: A1): A = {
        Thread.sleep(10000)
        new A1()
      }

      override val name: String = ""

      override val parameters = ParametersSchema()
    }
  }

  /**
   * Creates mocked graph with nodes of arity > 1
   * @return Mocked graph with nodes of arity > 1
   */
  def createMockedGraphWithTwoNodesOfHigherArity(nodeUuid1: UUID, nodeUuid2: UUID): Graph = {
    import DOperationTestClasses._
    val graph = new Graph
    val node1 = Node(nodeUuid1, new DOperation0To2Test)
    val node2 = Node(nodeUuid2, new DOperation2To1Test)
    val nodes = Set(node1, node2)
    // NOTE: node1 first (second) output goes to second (first) input port of node2
    val edgesList = List(
      (node1, node2, 0, 1),
      (node1, node2, 1, 0))
    val edges = edgesList.map(n => Edge(Endpoint(n._1.id,  n._3), Endpoint(n._2.id, n._4))).toSet
    Graph(nodes, edges)
  }
}
