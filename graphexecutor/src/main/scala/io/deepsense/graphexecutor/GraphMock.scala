/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 *
 * DOperations and DClasses necessary to mock Graph.
 * TODO: This should be removed as soon, as it is possible to replace it with real implementations.
 */
package io.deepsense.graphexecutor

import java.util.UUID

import io.deepsense.deeplang._
import io.deepsense.graph.{Edge, Endpoint, Graph, Node}
import io.deepsense.deeplang.parameters.ParametersSchema

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
  import io.deepsense.graphexecutor.DClassesForDOperations._

  class DOperation0To1Test extends DOperation0To1[A1] {
    override protected def _execute(context: ExecutionContext)(): A1 = {
      Thread.sleep(10000)
      new A1()
    }

    override val name: String = ""

    override val parameters = ParametersSchema()
  }

  class DOperation1To0Test extends DOperation1To0[A1] {
    override protected def _execute(context: ExecutionContext)(t0: A1): Unit = {
      Thread.sleep(10000)
    }

    override val name: String = ""

    override val parameters = ParametersSchema()
  }

  class DOperation1To1Test extends DOperation1To1[A1, A] {
    override protected def _execute(context: ExecutionContext)(t0: A1): A = {
      Thread.sleep(10000)
      new A1()
    }

    override val name: String = ""

    override val parameters = ParametersSchema()
  }

  class DOperation2To1Test extends DOperation2To1[A1, A2, A] {
    override protected def _execute(context: ExecutionContext)(t0: A1, t1: A2): A = {
      Thread.sleep(10000)
      new A1()
    }

    override val name: String = ""

    override val parameters = ParametersSchema()
  }
}

object GraphMock {
  import io.deepsense.graphexecutor.DOperationTestClasses._

  /**
   * Creates complex mocked graph
   * @return Mocked graph
   */
  def createMockedGraph: Graph = {
    val graph = new Graph
    val node1 = Node(UUID.randomUUID(), new DOperation0To1Test)
    val node2 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node3 = Node(UUID.randomUUID(), new DOperation1To1Test)
    val node4 = Node(UUID.randomUUID(), new DOperation2To1Test)
    val nodes = Set(node1, node2, node3, node4)
    val edgesList = List(
      (node1, node2, 0, 0),
      (node1, node3, 0, 0),
      (node2, node4, 0, 0),
      (node3, node4, 0, 1))
    val edges = edgesList.map(n => Edge(Endpoint(n._1.id,  n._3), Endpoint(n._2.id, n._4))).toSet
    Graph(nodes, edges)
  }
}
