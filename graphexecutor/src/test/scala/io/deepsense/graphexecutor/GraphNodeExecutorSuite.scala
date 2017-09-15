/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import scala.concurrent.duration._

import akka.actor.Props
import akka.testkit.{TestProbe, TestActorRef}
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSuite, Matchers}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.DOperableMock
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.graph.{Edge, Endpoint, Graph, Node}
import io.deepsense.graphexecutor.GraphExecutorActor.Messages.Completed
import io.deepsense.graphexecutor.GraphNodeExecutor.Messages.Start
import io.deepsense.models.entities.Entity

class GraphNodeExecutorSuite
    extends FunSuite
    with Matchers
    with Eventually {

//  private def mkMockedActor(
//      ctx: ExecutionContext, node: Node): TestActorRef[GraphNodeExecutor] = {
//    TestActorRef[GraphNodeExecutor](Props(new GraphNodeExecutor(ctx, node)))
//  }
//
//  test("GraphNodeExecutor should properly collect DOperables to pass them between DOperations") {
//    val ctx = new ExecutionContext()
//
//    val nodeId1 = Node.Id.randomId
//    val nodeId2 = Node.Id.randomId
//    var graph = createMockedGraphWithTwoNodesOfHigherArity(nodeId1, nodeId2)
//    val resultId1 = Entity.Id.randomId
//    val resultId2 = Entity.Id.randomId
//    val resultDataframe1 = new DataFrame()
//    val resultDataframe2 = new DataFrame()
//
//    val probe = TestProbe()
//
//    val graphNodeExecutor1 = mkMockedActor(ctx, graph.node(nodeId1))
//
//    var dOperableCache = Map[Entity.Id, DOperable]()
//    probe.send(graphNodeExecutor1, Start(graph, dOperableCache))
//    eventually(timeout(1.seconds), interval(100.milliseconds)) {
//      val collectedOutput1 = Map[Entity.Id, DOperable]()
//      probe.expectMsg(Completed(nodeId1, collectedOutput1))
//      // Node 1 has input arity == 0, vector should be empty
//      collectedOutput1 shouldBe Vector()
//    }
//
//    // Mark first node as completed, prepare its output for consecutive nodes
//    graph = graph.markAsRunning(nodeId1)
//    graph = graph.markAsCompleted(nodeId1, List(resultId1, resultId2))
//    dOperableCache = dOperableCache + (resultId1 -> resultDataframe1)
//    dOperableCache = dOperableCache + (resultId2 -> resultDataframe2)
//
//    val graphNodeExecutor2 = mkMockedActor(ctx, graph.node(nodeId2))
//
//    probe.send(graphNodeExecutor1, Start(graph, dOperableCache))
//    eventually(timeout(1.seconds), interval(100.milliseconds)) {
//      val collectedOutput2 = Map[Entity.Id, DOperable]()
//      probe.expectMsg(Completed(nodeId1, collectedOutput2))
//      // NOTE: node1 first (second) output goes to second (first) input port of node2
//      collectedOutput2 shouldBe Vector(resultDataframe2, resultDataframe1)
//    }
//  }

  // TODO: this classes could be replaced with real DOperations with the same arity
  object DClassesForDOperations {
    trait A extends DOperableMock
    case class A1() extends A
    case class A2() extends A
  }

  object DOperationTestClasses {
    import DClassesForDOperations._

    class DOperation0To2Test extends DOperation0To2[A1, A1] {
      override val id: Id = DOperation.Id.randomId

      override protected def _execute(context: ExecutionContext)(): (A1, A1) = {
        Thread.sleep(10000)
        (new A1(), new A1())
      }

      override val name: String = ""

      override val parameters = ParametersSchema()
    }

    class DOperation2To1Test extends DOperation2To1[A1, A1, A] {
      override val id: Id = DOperation.Id.randomId

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
  def createMockedGraphWithTwoNodesOfHigherArity(nodeId1: Node.Id, nodeId2: Node.Id): Graph = {
    import DOperationTestClasses._
    val node1 = Node(nodeId1, new DOperation0To2Test)
    val node2 = Node(nodeId2, new DOperation2To1Test)
    val nodes = Set(node1, node2)
    // NOTE: node1 first (second) output goes to second (first) input port of node2
    val edges = Set(
      Edge(node1, 0, node2, 1),
      Edge(node1, 1, node2, 0)
    )
    Graph(nodes, edges)
  }
}
