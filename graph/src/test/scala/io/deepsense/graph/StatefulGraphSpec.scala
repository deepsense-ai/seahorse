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

import org.scalatest.{GivenWhenThen, Inspectors}

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.models.Entity
import io.deepsense.commons.serialization.Serialization
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph.Node.Id
import io.deepsense.graph.RandomNodeFactory._
import io.deepsense.graph.graphstate._
import io.deepsense.graph.nodestate.NodeStatus

class StatefulGraphSpec
  extends StandardSpec
  with UnitTestSupport
  with Serialization
  with Inspectors
  with GivenWhenThen
  with GraphTestSupport {

  val statuses: Map[Node.Id, NodeStatus] = nodeIds.zip(
    Seq(nodeCompleted, nodeCompleted, nodeCompleted, nodeCompleted, nodeCompleted)).toMap
  val illegalForFinished = Seq(nodestate.Draft, nodestate.Queued, nodeRunning)
  val illegalForCompleted = illegalForFinished ++ Seq(nodeFailed, nodestate.Aborted)

  "StatefulGraph" should {
    "disallow to enqueue when execution is in progress" in {
      val runningGraph = StatefulGraph(nodeSet, edgeSet).enqueue
      illegalToEnqueue(runningGraph)
    }
    "disallow to change nodes' statuses when Completed" in {
      illegalToNodeFinishOrFail(completedGraph)
    }
    "disallow to change nodes' statuses when Failed" in {
      val failedGraph = StatefulGraph(nodeSet, edgeSet).enqueue.fail(mock[FailureDescription])
      illegalToNodeFinishOrFail(failedGraph)
    }
    "has no running execution" when {
      "is empty and was enqueued" in {
        StatefulGraph().enqueue.isRunning shouldBe false
      }
      "last Running node completed successfully and other nodes are Completed" in {
        val completedNodes: Map[Id, NodeStatus] =
          Seq(idA, idB, idC, idD).map { id => id -> nodeCompleted }.toMap
        val statuses = completedNodes + (idE -> nodeRunning)
        val g = graph(nodeSet, edgeSet, statuses, Running)
          .nodeFinished(idE, Seq(mock[Entity.Id], mock[Entity.Id]))
        g.statuses(idE) shouldBe 'Completed
        g.isRunning shouldBe false
      }
    }
    "has failed nodes" when {
      "there's no nodes to run and there is at least one Failed" when {
        "a node fails" in {
          val g = StatefulGraph(nodeSet, edgeSet).enqueue
            .nodeStarted(idA)
            .nodeFailed(idA, new Exception())

          g.hasFailedNodes shouldBe true

          forAll(Seq(idB, idC, idD, idE)) { id =>
            g.statuses(id) shouldBe 'Aborted
          }
        }
        "a node finishes" in {
          val statuses = nodeIds.zip(
            Seq(
              nodeCompleted,
              nodeCompleted,
              nodeCompleted,
              nodeFailed,
              nodeRunning)).toMap

          graph(nodeSet, edgeSet, statuses, Running)
            .nodeFinished(idE, Seq(mock[Entity.Id]))
            .hasFailedNodes shouldBe true
        }
      }
    }
    "be aborted" when {
      "there are Knowledge errors (inference)" in {
        val g = StatefulGraph(nodeSet, edgeSet)
          .enqueue
          .inferAndApplyKnowledge(mock[InferContext])
        forAll(List(idA, idB, idC, idD, idE)) { id => g.statuses(id) shouldBe 'Aborted}
      }
    }
    "be running" when {
      "enqueued" in {
        val enqueued = StatefulGraph(nodeSet, edgeSet)
          .enqueue
        nodeIds.foreach { id => enqueued.statuses(id) shouldBe 'Queued  }
      }
      "no ready nodes but running" in {
        val running = StatefulGraph(nodeSet, edgeSet).enqueue
          .nodeStarted(idA)

        running.readyNodes shouldBe 'Empty
        running.statuses(idA) shouldBe 'Running
        Seq(idB, idC, idD, idE).foreach { id => running.statuses(id) shouldBe 'Queued  }
      }
      "no running nodes but ready" in {
        val g = StatefulGraph(nodeSet, edgeSet)
          .enqueue
          .nodeStarted(idA)
          .nodeFinished(idA, results(idA))
          .nodeStarted(idB)
          .nodeFinished(idB, results(idB))

        g.readyNodes should have size 2
        g.statuses(idB) shouldBe 'Completed
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
      verifyNodeReady(idA, 0, enqueued)

      val running1 = enqueued.nodeStarted(idA)
      running1.readyNodes shouldBe 'Empty

      val running2 = running1.nodeFinished(idA, results(idA))
      running2.readyNodes should have size 1
      verifyNodeReady(idB, 1, running2)

      val running3 = running2.nodeStarted(idB).nodeFinished(idB, results(idB))
      running3.readyNodes should have size 2
      verifyNodeReady(idC, 1, running3)
      verifyNodeReady(idE, 2, running3)
    }
    "be still running after Abort until execution is finished" in {
      val aborted = StatefulGraph(nodeSet, edgeSet)
        .enqueue
        .nodeStarted(idA)
        .nodeFinished(idA, results(idA))
        .nodeStarted(idB)
        .nodeFinished(idB, results(idB))
        .nodeStarted(idC)
        .abort

      aborted.statuses(idA) shouldBe 'Completed
      aborted.statuses(idB) shouldBe 'Completed
      aborted.statuses(idC) shouldBe 'Running
      aborted.statuses(idD) shouldBe 'Aborted
      aborted.statuses(idE) shouldBe 'Aborted
      aborted.readyNodes shouldBe 'empty
      aborted shouldBe 'Running

      val finished = aborted.nodeFinished(idC, results(idC))
      finished.readyNodes shouldBe 'empty
      finished should not be 'Running
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
    "allow to update status using another StatefulGraph" in {
      val statuses: Map[Id, NodeStatus] = Map(
        idA -> mock[NodeStatus],
        idB -> mock[NodeStatus],
        idC -> mock[NodeStatus],
        idD -> mock[NodeStatus],
        idE -> mock[NodeStatus])
      val g1 = StatefulGraph(
        DirectedGraph(nodeSet, edgeSet),
        statuses,
        None)
      val nodeFailedStatus = nodeFailed
      val description: Some[FailureDescription] = Some(mock[FailureDescription])
      val g2 = StatefulGraph(
        DirectedGraph(Set(nodeB), Set()),
        Map(idB -> nodeFailedStatus),
        description)
      val updated = g1.updateStatuses(g2)
      updated.statuses should contain theSameElementsAs g1.statuses.updated(idB, nodeFailedStatus)
      updated.executionFailure shouldBe description
    }
    "recursively mark nodes as draft" in {
      val statusCompleted = nodeCompleted
      val drafted = StatefulGraph(DirectedGraph(nodeSet, edgeSet),
        Map(
          idA -> statusCompleted,
          idB -> nodeFailed,
          idC -> statusCompleted,
          idD -> statusCompleted,
          idE -> nodestate.Aborted
        ),
        None
      ).draft(idB)

      drafted.statuses should contain theSameElementsAs Map(
        idA -> statusCompleted,
        idB -> nodestate.Draft,
        idC -> nodestate.Draft,
        idD -> nodestate.Draft,
        idE -> nodestate.Draft
      )
    }
  }

  def completedGraph: StatefulGraph = {
    nodeIds.foldLeft(StatefulGraph(nodeSet, edgeSet).enqueue) {
      case (graph, nodeId) => graph.nodeStarted(nodeId).nodeFinished(nodeId, results(nodeId))
    }
  }

  private def graph(
      nodes: Set[Node],
      edges: Set[Edge],
      statuses: Map[Node.Id, NodeStatus],
      state: GraphState): StatefulGraph = {
    val directedGraph = DirectedGraph(nodes, edges)
    StatefulGraph(directedGraph, statuses, None)
  }

  private def illegalToNodeFinishOrFail(completedGraph: StatefulGraph): Unit = {
    illegalToNodeFinish(completedGraph)
    illegalToNodeFail(completedGraph)
  }

  private def illegalToNodeFail(completedGraph: StatefulGraph): Unit = {
    an[IllegalStateException] shouldBe thrownBy {
      completedGraph.nodeFailed(idB, new IllegalStateException("It is illegal to fail!"))
    }
  }

  private def illegalToNodeFinish(completedGraph: StatefulGraph): Unit = {
    an[IllegalStateException] shouldBe thrownBy {
      completedGraph.nodeFinished(idB, results(idB))
    }
  }

  private def illegalToEnqueue(completedGraph: StatefulGraph): Unit = {
    an[IllegalStateException] shouldBe thrownBy {
      completedGraph.enqueue
    }
  }
}
