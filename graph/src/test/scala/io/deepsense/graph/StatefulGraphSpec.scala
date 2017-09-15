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

import org.mockito.Mockito._
import org.scalatest.{GivenWhenThen, Inspectors}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.serialization.Serialization
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang._
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph.Node.Id
import io.deepsense.graph.RandomNodeFactory._
import io.deepsense.graph.graphstate._
import io.deepsense.graph.nodestate.NodeState
import io.deepsense.models.entities.Entity

class StatefulGraphSpec
  extends StandardSpec
  with UnitTestSupport
  with Serialization
  with Inspectors
  with GivenWhenThen {

  val op0To1 = {
    val m = mock[DOperation0To1[DOperable]]
    when(m.inArity).thenReturn(0)
    when(m.outArity).thenReturn(1)
    m
  }

  val op1To1 = {
    val m = mock[DOperation1To1[DOperable, DOperable]]
    when(m.inArity).thenReturn(1)
    when(m.outArity).thenReturn(1)
    m
  }

  val op2To2 = {
    val m = mock[DOperation2To2[DOperable, DOperable, DOperable, DOperable]]
    when(m.inArity).thenReturn(2)
    when(m.outArity).thenReturn(2)
    m
  }

  /*
   *   1 --> 2 --> 3 --> 4
   *     \   \
   *      \   ->
   *       ----> 5
   */

  val (id1, id2, id3, id4, id5, nodeSet) =
    nodes(op0To1, op1To1, op1To1, op1To1, op2To2)
  val edgeSet = edges(id1, id2, id3, id4, id5)
  val nodeIds = Seq(id1, id2, id3, id4, id5)
  val results = Map(
    id1 -> Seq(mock[Entity.Id]),
    id2 -> Seq(mock[Entity.Id]),
    id3 -> Seq(mock[Entity.Id]),
    id4 -> Seq(mock[Entity.Id]),
    id5 -> Seq(mock[Entity.Id], mock[Entity.Id])
  )

  val states: Map[Node.Id, NodeState] = nodeIds.zip(Seq(nodeCompleted, nodeCompleted, nodeCompleted,
    nodeCompleted, nodeCompleted)).toMap
  val illegalForFinished = Seq(nodestate.Draft, nodestate.Queued, nodeRunning)
  val illegalForCompleted = illegalForFinished ++ Seq(nodeFailed, nodestate.Aborted)

  def containsANodeInAnIllegalState(
      illegalNodeStates: Seq[NodeState],
      graphState: GraphState): Unit = {
    illegalNodeStates.foreach { state =>
      val illegalStates = states.updated(id2, state)
      s"contains a node in state ${state.name}" in {
        an[IllegalArgumentException] shouldBe thrownBy {
          graph(nodeSet, edgeSet, illegalStates, graphState)
        }
      }
    }
  }

  "StatefulGraph" should {
    "throw IllegalArgumentException" when {
      "contains non-Draft nodes in Draft state" in {
        val g = StatefulGraph(nodeSet, edgeSet)
        val enqueued = g.states(id1).enqueue
        an [IllegalArgumentException] shouldBe thrownBy {
          g.copy(states = g.states.updated(id1, enqueued))
        }
      }
      "contains Draft nodes in non-Draft state" in {
        val g = StatefulGraph(nodeSet, edgeSet).enqueue
        an [IllegalArgumentException] shouldBe thrownBy {
          g.copy(states = g.states.updated(id1, nodestate.Draft))
        }
      }
      "is Aborted and" when {
        containsANodeInAnIllegalState(illegalForFinished, Aborted)
      }
      "is Failed and" when {
        containsANodeInAnIllegalState(illegalForFinished, Failed(mock[FailureDescription]))
      }
      "is Completed and" when {
        containsANodeInAnIllegalState(illegalForCompleted, Completed)
      }
    }
    "disallow to enqueue when Completed" in {
      illegalToEnqueue(completedGraph)
    }
    "disallow to enqueue when Aborted" is pending
    "disallow to enqueue when Failed" in {
      val failedGraph = StatefulGraph(nodeSet, edgeSet).enqueue.fail(mock[FailureDescription])
      illegalToEnqueue(failedGraph)
    }
    "disallow to enqueue when Running" in {
      val runningGraph = StatefulGraph(nodeSet, edgeSet).enqueue
      illegalToEnqueue(runningGraph)
    }
    "disallow to change nodes' states when Completed" in {
      illegalToNodeFinishOrFail(completedGraph)
    }
    "disallow to change nodes' states when Aborted" is pending
    "disallow to change nodes' states when Failed" in {
      val failedGraph = StatefulGraph(nodeSet, edgeSet).enqueue.fail(mock[FailureDescription])
      illegalToNodeFinishOrFail(failedGraph)
    }
    "be completed" when {
      "is empty and was enqueued" in {
        StatefulGraph().enqueue.state shouldBe Completed
      }
      "last Running node completed successfully and other nodes are Completed" in {
        val completedNodes: Map[Id, NodeState] =
          Seq(id1, id2, id3, id4).map { id => id -> nodeCompleted }.toMap
        val states = completedNodes + (id5 -> nodeRunning)
        val g = graph(nodeSet, edgeSet, states, Running)
          .nodeFinished(id5, Seq(mock[Entity.Id], mock[Entity.Id]))
        g.states(id5) shouldBe 'Completed
        g.state shouldBe Completed
      }
    }
    "be failed" when {
      "there's no nodes to run and there is at least one Failed" when {
        "a node fails" in {
          val g = StatefulGraph(nodeSet, edgeSet).enqueue
            .nodeStarted(id1)
            .nodeFailed(id1, new Exception())

          g.state shouldBe 'Failed

          forAll(Seq(id2, id3, id4, id5)) { id =>
            g.states(id) shouldBe 'Aborted
          }
        }
        "a node finishes" in {
          val states = nodeIds.zip(
            Seq(
              nodeCompleted,
              nodeCompleted,
              nodeCompleted,
              nodeFailed,
              nodeRunning)).toMap

          graph(nodeSet, edgeSet, states, Running)
            .nodeFinished(id5, Seq(mock[Entity.Id]))
            .state shouldBe 'Failed
        }
      }
    }
    "be aborted" when {
      "there are Knowledge errors (inference)" in {
        val g = StatefulGraph(nodeSet, edgeSet)
          .enqueue
          .inferAndApplyKnowledge(mock[InferContext])
        g.state shouldBe 'Failed
        forAll(List(id1, id2, id3, id4, id5)) { id => g.states(id) shouldBe 'Aborted}
      }
    }
    "be running" when {
      "enqueued" in {
        val enqueued = StatefulGraph(nodeSet, edgeSet)
          .enqueue
        enqueued.state shouldBe Running
        nodeIds.foreach { id => enqueued.states(id) shouldBe 'Queued  }
      }
      "no ready nodes but running" in {
        val running = StatefulGraph(nodeSet, edgeSet).enqueue
          .nodeStarted(id1)

        running.state shouldBe 'Running
        running.readyNodes shouldBe 'Empty
        running.states(id1) shouldBe 'Running
        Seq(id2, id3, id4, id5).foreach { id => running.states(id) shouldBe 'Queued  }
      }
      "no running nodes but ready" in {
        val g = StatefulGraph(nodeSet, edgeSet)
          .enqueue
          .nodeStarted(id1)
          .nodeFinished(id1, results(id1))
          .nodeStarted(id2)
          .nodeFinished(id2, results(id2))

        g.state shouldBe 'Running
        g.readyNodes should have size 2
        g.states(id2) shouldBe 'Completed
      }
    }
    "be draft" when {
      "created" in {
        StatefulGraph().state shouldBe Draft
      }
    }
    "list nodes ready for execution" in {
      def verifyNodeReady(id: Node.Id, inputSize: Int, g: StatefulGraph): Unit = {
        val readyNodes = g.readyNodes
        val readyNodesIds = readyNodes.map(n => n.node.id)
        readyNodesIds should contain(id)
        val readyNode = readyNodes.find(_.node.id == id).get
        readyNode.input should have size inputSize
      }

      val draft = StatefulGraph(nodeSet, edgeSet)
      draft.readyNodes shouldBe 'Empty

      val enqueued = draft.enqueue
      enqueued.readyNodes should have size 1
      verifyNodeReady(id1, 0, enqueued)

      val running1 = enqueued.nodeStarted(id1)
      running1.readyNodes shouldBe 'Empty

      val running2 = running1.nodeFinished(id1, results(id1))
      running2.readyNodes should have size 1
      verifyNodeReady(id2, 1, running2)

      val running3 = running2.nodeStarted(id2).nodeFinished(id2, results(id2))
      running3.readyNodes should have size 2
      verifyNodeReady(id3, 1, running3)
      verifyNodeReady(id5, 2, running3)
    }
    "be serializable" in {
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
      val graph = StatefulGraph(nodes.toSet, edges)
      val graphIn = serializeDeserialize(graph)
      graphIn shouldBe graph
      val operation = graphIn.node(id).operation.asInstanceOf[DOperationAToALogging]
      operation.trace("Logging just to clarify that it works after deserialization!")
      operation.tTagTI_0.tpe should not be null
    }
  }

  def completedGraph: StatefulGraph = {
    nodeIds.foldLeft(StatefulGraph(nodeSet, edgeSet).enqueue) {
      case (graph, nodeId) => graph.nodeStarted(nodeId).nodeFinished(nodeId, results(nodeId))
    }
  }

  /**
   * Creates edges for a graph like this one:
   *   A --> B --> C --> D
   *     \   \
   *      \   ->
   *       ----> E
   * To each node assigns the specified Id.
   */
  private def edges(
      idA: Node.Id,
      idB: Node.Id,
      idC: Node.Id,
      idD: Node.Id,
      idE: Node.Id
    ): Set[Edge] = {
    Set(
      Edge(Endpoint(idA, 0), Endpoint(idB, 0)),
      Edge(Endpoint(idB, 0), Endpoint(idC, 0)),
      Edge(Endpoint(idC, 0), Endpoint(idD, 0)),
      Edge(Endpoint(idA, 0), Endpoint(idE, 0)),
      Edge(Endpoint(idB, 0), Endpoint(idE, 1))
    )
  }

  private def nodes(
      opA: DOperation,
      opB: DOperation,
      opC: DOperation,
      opD: DOperation,
      opE: DOperation
    ): (Node.Id, Node.Id, Node.Id, Node.Id, Node.Id, Set[Node]) = {
    val nodes = Seq(opA, opB, opC, opD, opE).map { o => Node(Node.Id.randomId, o)}
    val idA :: idB :: idC :: idD :: idE :: Nil = nodes.map(_.id)
    (idA, idB, idC, idD, idE, nodes.toSet)
  }

  private def nodeRunning: nodestate.Running = nodestate.Running(DateTimeConverter.now)

  private def nodeFailed: nodestate.Failed =
    nodestate.Running(DateTimeConverter.now).fail(mock[FailureDescription])

  private def nodeCompleted: nodestate.Completed = {
    val date = DateTimeConverter.now
    nodestate.Completed(date, date.plusMinutes(1), Seq())
  }

  private def graph(
      nodes: Set[Node],
      edges: Set[Edge],
      states: Map[Node.Id, NodeState],
      state: GraphState): StatefulGraph = {
    val directedGraph = DirectedGraph(nodes, edges)
    StatefulGraph(directedGraph, states, state)
  }

  private def illegalToNodeFinishOrFail(completedGraph: StatefulGraph): Unit = {
    illegalToNodeFinish(completedGraph)
    illegalToNodeFail(completedGraph)
  }

  private def illegalToNodeFail(completedGraph: StatefulGraph): Unit = {
    an[IllegalStateException] shouldBe thrownBy {
      completedGraph.nodeFailed(id2, new IllegalStateException("It is illegal to fail!"))
    }
  }

  private def illegalToNodeFinish(completedGraph: StatefulGraph): Unit = {
    an[IllegalStateException] shouldBe thrownBy {
      completedGraph.nodeFinished(id2, results(id2))
    }
  }

  private def illegalToEnqueue(completedGraph: StatefulGraph): Unit = {
    an[IllegalStateException] shouldBe thrownBy {
      completedGraph.enqueue
    }
  }
}
