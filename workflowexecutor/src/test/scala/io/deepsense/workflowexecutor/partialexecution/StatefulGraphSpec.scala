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

package io.deepsense.workflowexecutor.partialexecution

import org.scalatest.{GivenWhenThen, Inspectors}

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.models.Entity
import io.deepsense.commons.serialization.Serialization
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph.Node.Id
import io.deepsense.graph.RandomNodeFactory._
import io.deepsense.graph.graphstate._
import io.deepsense.graph.nodestate.NodeStatus
import io.deepsense.graph._
import io.deepsense.models.workflows.{EntitiesMap, NodeState, NodeStateWithResults}
import io.deepsense.reportlib.model.ReportContent

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
        val completedNodes: Map[Id, NodeStateWithResults] =
          Seq(idA, idB, idC, idD).map { id => id -> nodeState(nodeCompleted) }.toMap
        val statuses = completedNodes + (idE -> nodeState(nodeRunning))
        val g = graph(nodeSet, edgeSet, statuses, Running)
          .nodeFinished(idE, Seq(mock[Entity.Id], mock[Entity.Id]), Map(), Map())
        g.states(idE) shouldBe 'Completed
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
            g.states(id) shouldBe 'Aborted
          }
        }
        "a node finishes" in {
          val states = nodeIds.zip(
            Seq(
              nodeState(nodeCompleted),
              nodeState(nodeCompleted),
              nodeState(nodeCompleted),
              nodeState(nodeFailed),
              nodeState(nodeRunning))).toMap

          graph(nodeSet, edgeSet, states, Running)
            .nodeFinished(idE, Seq(mock[Entity.Id]), Map(), Map())
            .hasFailedNodes shouldBe true
        }
      }
    }
    "be aborted" when {
      "there are Knowledge errors (inference)" in {
        val g = StatefulGraph(nodeSet, edgeSet)
          .enqueue
          .inferAndApplyKnowledge(mock[InferContext])
        forAll(List(idA, idB, idC, idD, idE)) { id => g.states(id) shouldBe 'Aborted}
      }
    }
    "be running" when {
      "enqueued" in {
        val enqueued = StatefulGraph(nodeSet, edgeSet)
          .enqueue
        nodeIds.foreach { id => enqueued.states(id) shouldBe 'Queued  }
      }
      "no ready nodes but running" in {
        val running = StatefulGraph(nodeSet, edgeSet).enqueue
          .nodeStarted(idA)

        running.readyNodes shouldBe 'Empty
        running.states(idA) shouldBe 'Running
        Seq(idB, idC, idD, idE).foreach { id => running.states(id) shouldBe 'Queued  }
      }
      "no running nodes but ready" in {
        val g = StatefulGraph(nodeSet, edgeSet)
          .enqueue
          .nodeStarted(idA)
          .nodeFinished(
            idA,
            results(idA),
            Map(results(idA).head -> ReportContent("blah")),
            Map(results(idA).head -> mock[DOperable]))
          .nodeStarted(idB)
          .nodeFinished(
            idB,
            results(idB),
            Map(results(idB).head -> ReportContent("foo")),
            Map(results(idB).head -> mock[DOperable]))

        g.readyNodes should have size 2
        g.states(idB) shouldBe 'Completed
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

      val running2 = running1.nodeFinished(
        idA,
        results(idA),
        Map(results(idA).head -> ReportContent("foo")),
        Map(results(idA).head -> mock[DOperable]))
      running2.readyNodes should have size 1
      verifyNodeReady(idB, 1, running2)

      val running3 = running2.nodeStarted(idB).nodeFinished(
        idB,
        results(idB),
        Map(results(idB).head -> ReportContent("test")),
        Map(results(idB).head -> mock[DOperable]))
      running3.readyNodes should have size 2
      verifyNodeReady(idC, 1, running3)
      verifyNodeReady(idE, 2, running3)
    }
    "be still running after Abort until execution is finished" in {
      val aborted = StatefulGraph(nodeSet, edgeSet)
        .enqueue
        .nodeStarted(idA)
        .nodeFinished(idA, results(idA), Map(), Map())
        .nodeStarted(idB)
        .nodeFinished(idB, results(idB), Map(), Map())
        .nodeStarted(idC)
        .abortQueued

      aborted.states(idA) shouldBe 'Completed
      aborted.states(idB) shouldBe 'Completed
      aborted.states(idC) shouldBe 'Running
      aborted.states(idD) shouldBe 'Aborted
      aborted.states(idE) shouldBe 'Aborted
      aborted.readyNodes shouldBe 'empty
      aborted shouldBe 'Running

      val finished = aborted.nodeFinished(idC, results(idC), Map(), Map())
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
      val states: Map[Id, NodeStateWithResults] = Map(
        idA -> mock[NodeStateWithResults],
        idB -> mock[NodeStateWithResults],
        idC -> mock[NodeStateWithResults],
        idD -> mock[NodeStateWithResults],
        idE -> mock[NodeStateWithResults])
      val g1 = StatefulGraph(
        DirectedGraph(nodeSet, edgeSet),
        states,
        None)
      val nodeFailedStatus =
        NodeStateWithResults(NodeState(nodeFailed, Some(EntitiesMap())), Map())
      val description: Some[FailureDescription] = Some(mock[FailureDescription])
      val g2 = StatefulGraph(
        DirectedGraph(Set(nodeB), Set()),
        Map(idB -> nodeFailedStatus),
        description)
      val updated = g1.updateStates(g2)
      updated.states should contain theSameElementsAs g1.states.updated(idB, nodeFailedStatus)
      updated.executionFailure shouldBe description
    }
    "recursively mark nodes as draft" in {
      val statusCompleted = nodeCompleted
      val drafted = StatefulGraph(DirectedGraph(nodeSet, edgeSet),
        Map(
          idA -> nodeState(statusCompleted),
          idB -> nodeState(nodeFailed),
          idC -> nodeState(statusCompleted),
          idD -> nodeState(statusCompleted),
          idE -> nodeState(nodestate.Aborted())
        ),
        None
      ).draft(idB)

      drafted.states should contain theSameElementsAs Map(
        idA -> nodeState(statusCompleted),
        idB -> nodeState(nodestate.Draft()),
        idC -> nodeState(nodestate.Draft()),
        idD -> nodeState(nodestate.Draft()),
        idE -> nodeState(nodestate.Draft())
      )
    }
  }

  def completedGraph: StatefulGraph = {
    nodeIds.foldLeft(StatefulGraph(nodeSet, edgeSet).enqueue) {
      case (graph, nodeId) =>
        graph.nodeStarted(nodeId).nodeFinished(nodeId, results(nodeId), Map(), Map())
    }
  }

  private def graph(
      nodes: Set[Node],
      edges: Set[Edge],
      states: Map[Node.Id, NodeStateWithResults],
      state: GraphState): StatefulGraph = {
    val directedGraph = DirectedGraph(nodes, edges)
    StatefulGraph(directedGraph, states, None)
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
      completedGraph.nodeFinished(idB, results(idB), Map(), Map())
    }
  }

  private def illegalToEnqueue(completedGraph: StatefulGraph): Unit = {
    an[IllegalStateException] shouldBe thrownBy {
      completedGraph.enqueue
    }
  }

  private def nodeState(status: NodeStatus): NodeStateWithResults = {
    NodeStateWithResults(NodeState(status, Some(EntitiesMap())), Map())
  }
}
