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

import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._

import io.deepsense.commons.StandardSpec
import io.deepsense.deeplang.inference.InferContext

import io.deepsense.graph._

class ExecutionSpec
  extends StandardSpec
  with MockitoSugar
  with GraphTestSupport {

  val directedGraph = DirectedGraph(nodeSet, edgeSet)
  val allNodesIds = directedGraph.nodes.map(_.id).toSeq

  "Execution" should {
    "have all nodes Draft" when {
      "empty" in {
        val execution = Execution.empty
        execution.states.values.toSet should have size 0
      }
      "non empty" in {
        val execution = Execution(directedGraph)
        execution.states should have size execution.selectedNodes.size
        execution.selectedNodes.foreach {
          n => execution.states(n) shouldBe nodestate.Draft
        }
        execution shouldBe a[IdleExecution]
      }
      "created with selection" in {
        val execution = Execution(directedGraph, Seq(idA, idB))
        execution.states should have size execution.graph.size
        execution.selectedNodes.foreach {
          n => execution.states(n) shouldBe nodestate.Draft
        }
      }
    }
    "infer knowledge only on the selected part" in {
      val graph = mock[StatefulGraph]
      when(graph.directedGraph).thenReturn(DirectedGraph())
      val subgraph = mock[StatefulGraph]
      when(graph.subgraph(any())).thenReturn(subgraph)
      when(subgraph.enqueueDraft).thenReturn(subgraph)

      val nodes = Set[Node.Id]()
      val execution = IdleExecution(graph, nodes)

      val inferenceResult = mock[StatefulGraph]
      when(subgraph.inferAndApplyKnowledge(any())).thenReturn(inferenceResult)
      when(graph.updateStates(any())).thenReturn(inferenceResult)

      val inferContext = mock[InferContext]
      val inferred = execution.inferAndApplyKnowledge(inferContext)
      verify(subgraph).inferAndApplyKnowledge(inferContext)

      inferred shouldBe IdleExecution(inferenceResult, nodes)
    }
    "mark nodes as Draft when a predecessor changed " +
      "even if the nodes were excluded from execution" in {
        val statefulGraph = StatefulGraph(
          directedGraph,
          Map(
            idA -> nodeCompleted,
            idB -> nodeFailed,
            idC -> nodeCompleted,
            idD -> nodeCompleted,
            idE -> nodestate.Aborted
          ),
          None
        )

        val execution = IdleExecution(
          statefulGraph,
          statefulGraph.nodes.map(_.id))

        val updated =
          execution.updateStructure(statefulGraph.directedGraph, Set(idC))

        updated.states(idA) shouldBe execution.states(idA)
        updated.states(idB) shouldBe nodestate.Draft
        updated.states(idC) shouldBe nodestate.Draft
        updated.states(idD) shouldBe nodestate.Draft
        updated.states(idE) shouldBe nodestate.Draft
    }
    "enqueue all nodes" when {
      "all nodes or no nodes where specified" in {
        val noSelected = Execution(directedGraph, Seq())
        val allSelected = Execution(directedGraph, allNodesIds)
        noSelected shouldBe allSelected

        val draftGraph = StatefulGraph(
          directedGraph,
          directedGraph.nodes.map(n => n.id -> nodestate.Draft).toMap,
          None
        )

        val queuedGraph = StatefulGraph(
          directedGraph,
          directedGraph.nodes.map(n => n.id -> nodestate.Queued).toMap,
          None
        )

        val enqueued = noSelected.enqueue

        enqueued shouldBe
          RunningExecution(
            draftGraph,
            queuedGraph,
            allNodesIds.toSet)

        enqueued.states.forall { case (_, state) => state.isQueued } shouldBe true
      }
    }
    "mark all selected nodes as Draft" in {
      val statefulGraph = StatefulGraph(
        DirectedGraph(nodeSet, edgeSet),
        Map(
          idA -> nodeCompleted,
          idB -> nodeCompleted,
          idC -> nodeCompleted,
          idD -> nodeCompleted,
          idE -> nodeCompleted
        ),
        None
      )

      val execution = IdleExecution(
        statefulGraph,
        statefulGraph.nodes.map(_.id))

      val updated =
        execution.updateStructure(statefulGraph.directedGraph, Set(idC, idE))

      updated.states(idA) shouldBe execution.states(idA)
      updated.states(idB) shouldBe execution.states(idB)
      updated.states(idC) shouldBe nodestate.Draft
      updated.states(idD) shouldBe nodestate.Draft
      updated.states(idE) shouldBe nodestate.Draft
    }
    "not execute operations that are already completed (if they are not selected)" +
      "finish execution if the selected subgraph finished" in {
      val statefulGraph = StatefulGraph(
        DirectedGraph(nodeSet, edgeSet),
        Map(
          idA -> nodeCompletedId(idA),
          idB -> nodeCompletedId(idB),
          idC -> nodeCompletedId(idC),
          idD -> nodeCompletedId(idD),
          idE -> nodeCompletedId(idE)
        ),
        None
      )

      val execution = IdleExecution(
        statefulGraph,
        statefulGraph.nodes.map(_.id))

      val enqueued = execution
          .updateStructure(statefulGraph.directedGraph, Set(idC, idE))
          .enqueue

      enqueued.states(idA) shouldBe execution.states(idA)
      enqueued.states(idB) shouldBe execution.states(idB)
      enqueued.states(idC) shouldBe nodestate.Queued
      enqueued.states(idD) shouldBe nodestate.Draft
      enqueued.states(idE) shouldBe nodestate.Queued

      enqueued.readyNodes.map(rn => rn.node) should contain theSameElementsAs List(nodeC, nodeE)
      val cStarted = enqueued.nodeStarted(idC)
      cStarted.readyNodes.map(rn => rn.node) should contain theSameElementsAs List(nodeE)
      val eStarted = cStarted.nodeStarted(idE)
      eStarted.readyNodes.map(rn => rn.node) shouldBe 'empty

      val finished = eStarted
        .nodeFinished(idC, results(idC))
        .nodeFinished(idE, results(idE))

      finished shouldBe a[IdleExecution]
    }
    "reset successors state when predecessor is replaced" in {
      val changedC = Node(Node.Id.randomId, op1To1) // Notice: different Id
      checkSuccessorsStatesAfterANodeChange(changedC)
    }
    "reset successors state when predecessor's parameters are modified" in pendingUntilFixed {
      val changedC = Node(idC, op1To1) // Notice: the same id; different parameters!
      checkSuccessorsStatesAfterANodeChange(changedC)
    }
  }

  def checkSuccessorsStatesAfterANodeChange(changedC: Node): Unit = {
    val graph = DirectedGraph(nodeSet, edgeSet)

    val edgeBtoC = Edge(nodeB, 0, changedC, 0)
    val edgeCtoD = Edge(changedC, 0, nodeD, 0)
    val updatedGraph = DirectedGraph(
      Set(nodeA, nodeB, changedC, nodeD, nodeE),
      Set(edge1, edgeBtoC, edgeCtoD, edge4, edge5)
    )

    val statefulGraph = StatefulGraph(
      graph,
      Map(
        idA -> nodeCompletedId(idA),
        idB -> nodeCompletedId(idB),
        idC -> nodeCompletedId(idC),
        idD -> nodeCompletedId(idD),
        idE -> nodeCompletedId(idE)
      ),
      None)

    val execution = IdleExecution(
      statefulGraph,
      statefulGraph.nodes.map(_.id))

    val updatedExecution = execution.updateStructure(updatedGraph, Set(idE))
    updatedExecution.states(idA) shouldBe statefulGraph.states(idA)
    updatedExecution.states(idB) shouldBe statefulGraph.states(idB)
    updatedExecution.states(changedC.id) shouldBe nodestate.Draft
    updatedExecution.states(idD) shouldBe nodestate.Draft
    updatedExecution.states(idE) shouldBe nodestate.Draft

    val queuedExecution = updatedExecution.enqueue
    queuedExecution.states(idA) shouldBe statefulGraph.states(idA)
    queuedExecution.states(idB) shouldBe statefulGraph.states(idB)
    queuedExecution.states(changedC.id) shouldBe nodestate.Draft
    queuedExecution.states(idD) shouldBe nodestate.Draft
    queuedExecution.states(idE) shouldBe nodestate.Queued
  }
}
