/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowexecutor.partialexecution

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar

import ai.deepsense.commons.StandardSpec
import ai.deepsense.commons.exception.FailureDescription
import ai.deepsense.commons.models.Entity
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.inference.InferContext
import ai.deepsense.deeplang.{DOperable, DOperation}
import ai.deepsense.graph.DeeplangGraph.DeeplangNode
import ai.deepsense.graph._
import ai.deepsense.graph.nodestate.{Completed, NodeStatus, Queued}
import ai.deepsense.models.workflows.{EntitiesMap, NodeState, NodeStateWithResults}
import ai.deepsense.reportlib.model.ReportContent
import ai.deepsense.reportlib.model.factory.ReportContentTestFactory

class ExecutionSpec
  extends StandardSpec
  with MockitoSugar
  with GraphTestSupport {

  val directedGraph = DeeplangGraph(nodeSet, edgeSet)
  val statefulGraph = StatefulGraph(
    directedGraph,
    directedGraph.nodes.map(_.id -> NodeStateWithResults.draft).toMap,
    None)
  val allNodesIds = directedGraph.nodes.map(_.id)

  "Execution" should {
    "have all nodes Draft" when {
      "empty" in {
        val execution = Execution.empty
        execution.graph.states.values.toSet should have size 0
      }
      "created with selection" in {
        val execution = Execution(statefulGraph, Set(idA, idB))
        execution.graph.states should have size execution.graph.size
        execution.selectedNodes.foreach {
          n => execution.graph.states(n) shouldBe NodeStateWithResults.draft
        }
      }
    }
    "have all previously queued nodes get aborted after aborting execution" in {
      val firstNodeRunningAndRestAreQueued =
        directedGraph.nodes.map(n => n.id -> nodeState(Queued())).toMap

      val statefulGraph = StatefulGraph(
        directedGraph,
        firstNodeRunningAndRestAreQueued,
        None
      )

      val running = RunningExecution(
        statefulGraph,
        statefulGraph,
        allNodesIds.toSet)

      val aborted = running.abort
      aborted.graph.states.values.foreach { nodeState =>
        nodeState.isAborted shouldBe true
      }
    }
    "infer knowledge only on the selected part" in {
      val graph = mock[StatefulGraph]
      when(graph.readyNodes).thenReturn(Seq.empty)
      when(graph.directedGraph).thenReturn(DeeplangGraph())
      val subgraph = mock[StatefulGraph]
      when(subgraph.readyNodes).thenReturn(Seq.empty)
      when(graph.subgraph(any())).thenReturn(subgraph)
      when(subgraph.enqueueDraft).thenReturn(subgraph)

      val nodes = Set[Node.Id]()
      val execution = IdleExecution(graph, nodes)

      val inferenceResult = mock[StatefulGraph]
      when(inferenceResult.readyNodes).thenReturn(Seq.empty)
      when(subgraph.inferAndApplyKnowledge(any())).thenReturn(inferenceResult)
      when(graph.updateStates(any())).thenReturn(inferenceResult)

      val inferContext = mock[InferContext]
      val inferred = execution.inferAndApplyKnowledge(inferContext)
      verify(subgraph).inferAndApplyKnowledge(inferContext)

      inferred shouldBe IdleExecution(inferenceResult, nodes)
    }
    "mark nodes as Draft when predecessor changed" +
      "even if the nodes were excluded from execution" in {
        val statefulGraph = StatefulGraph(
          directedGraph,
          Map(
            idA -> nodeCompletedState.withKnowledge(mock[NodeInferenceResult]),
            idB -> nodeState(nodeFailed).withKnowledge(mock[NodeInferenceResult]),
            idC -> nodeCompletedState.withKnowledge(mock[NodeInferenceResult]),
            idD -> nodeCompletedState.withKnowledge(mock[NodeInferenceResult]),
            idE -> nodeState(nodestate.Aborted()).withKnowledge(mock[NodeInferenceResult])
          ),
          None
        )

        val execution = IdleExecution(
          statefulGraph,
          statefulGraph.nodes.map(_.id))

        val updated =
          execution.updateStructure(statefulGraph.directedGraph, Set(idC))

        updated.graph.states(idA) shouldBe execution.graph.states(idA)
        updated.graph.states(idB) shouldBe execution.graph.states(idB).draft
        updated.graph.states(idC) shouldBe execution.graph.states(idC).draft
        updated.graph.states(idD) shouldBe execution.graph.states(idD).draft
        updated.graph.states(idE) shouldBe execution.graph.states(idE).draft
    }
    "enqueue all nodes" when {
      "all nodes where specified" in {
        val allSelected = Execution(statefulGraph, allNodesIds)

        val draftGraph = StatefulGraph(
          directedGraph,
          directedGraph.nodes.map(n => n.id -> NodeStateWithResults.draft).toMap,
          None
        )

        val queuedGraph = StatefulGraph(
          directedGraph,
          directedGraph.nodes.map(n => n.id -> nodeState(Queued())).toMap,
          None
        )

        val enqueued = allSelected.enqueue

        enqueued shouldBe
          RunningExecution(
            draftGraph,
            queuedGraph,
            allNodesIds.toSet)

        enqueued.graph.states.forall { case (_, state) => state.isQueued } shouldBe true
      }
    }
    "mark all selected nodes as Draft" in {
      val statefulGraph = StatefulGraph(
        DeeplangGraph(nodeSet, edgeSet),
        Map(
          idA -> nodeCompletedState.withKnowledge(mock[NodeInferenceResult]),
          idB -> nodeCompletedState.withKnowledge(mock[NodeInferenceResult]),
          idC -> nodeCompletedState.withKnowledge(mock[NodeInferenceResult]),
          idD -> nodeCompletedState.withKnowledge(mock[NodeInferenceResult]),
          idE -> nodeCompletedState.withKnowledge(mock[NodeInferenceResult])
        ),
        None
      )

      val execution = IdleExecution(
        statefulGraph,
        statefulGraph.nodes.map(_.id))

      val updated =
        execution.updateStructure(statefulGraph.directedGraph, Set(idC, idE))

      updated.graph.states(idA) shouldBe execution.graph.states(idA)
      updated.graph.states(idB) shouldBe execution.graph.states(idB)
      updated.graph.states(idC) shouldBe execution.graph.states(idC).draft
      updated.graph.states(idD) shouldBe execution.graph.states(idD).draft
      updated.graph.states(idE) shouldBe execution.graph.states(idE).draft
    }
    "not execute operations that are already completed (if they are not selected)" +
      "finish execution if the selected subgraph finished" in {
      val stateWC = nodeCompletedIdState(idC)
      val stateWD = nodeCompletedIdState(idD)
      val stateWE = nodeCompletedIdState(idE)
      val statefulGraph = StatefulGraph(
        DeeplangGraph(nodeSet, edgeSet),
        Map(
          idA -> nodeCompletedIdState(idA),
          idB -> nodeCompletedIdState(idB),
          idC -> stateWC,
          idD -> stateWD,
          idE -> stateWE
        ),
        None
      )

      val execution = IdleExecution(
        statefulGraph,
        statefulGraph.nodes.map(_.id))

      val enqueued = execution
          .updateStructure(statefulGraph.directedGraph, Set(idC, idE))
          .enqueue

      enqueued.graph.states(idA) shouldBe execution.graph.states(idA)
      enqueued.graph.states(idB) shouldBe execution.graph.states(idB)
      enqueued.graph.states(idC) shouldBe stateWC.draft.enqueue
      enqueued.graph.states(idD) shouldBe stateWD.draft
      enqueued.graph.states(idE) shouldBe stateWE.draft.enqueue

      enqueued.graph.readyNodes.map(
        rn => rn.node) should contain theSameElementsAs List(nodeC, nodeE)
      val cStarted = enqueued.nodeStarted(idC)
      cStarted.graph.readyNodes.map(rn => rn.node) should contain theSameElementsAs List(nodeE)
      val eStarted = cStarted.nodeStarted(idE)
      eStarted.graph.readyNodes.map(rn => rn.node) shouldBe 'empty

      val idCResults = results(idC)
      val idEResults = results(idE)
      def reports(ids: Seq[Entity.Id]): Map[Entity.Id, ReportContent] =
        ids.map(_ -> ReportContentTestFactory.someReport).toMap
      def dOperables(ids: Seq[Entity.Id]): Map[Entity.Id, DOperable] =
        ids.map(_ -> mock[DOperable]).toMap
      val finished = eStarted
        .nodeFinished(idC, idCResults, reports(idCResults), dOperables(idCResults))
        .nodeFinished(idE, idEResults, reports(idEResults), dOperables(idEResults))

      finished shouldBe an[IdleExecution]
    }
    "expose inference errors" in {
      val failedGraph = mock[StatefulGraph]
      when(failedGraph.readyNodes).thenReturn(Seq.empty)
      val failureDescription = Some(mock[FailureDescription])
      when(failedGraph.executionFailure).thenReturn(failureDescription)

      val graph = mock[StatefulGraph]
      when(graph.readyNodes).thenReturn(Seq.empty)
      val directedGraph = mock[DeeplangGraph]
      when(directedGraph.nodes).thenReturn(Set[DeeplangNode]())
      when(graph.directedGraph).thenReturn(directedGraph)
      when(graph.subgraph(any())).thenReturn(graph)
      when(graph.inferAndApplyKnowledge(any())).thenReturn(failedGraph)
      when(graph.updateStates(any())).thenReturn(failedGraph)
      when(graph.executionFailure).thenReturn(None)

      val execution = IdleExecution(graph, Set())
      execution.inferAndApplyKnowledge(
        mock[InferContext]).graph.executionFailure shouldBe failureDescription
    }
    "reset successors state when predecessor is replaced" in {
      val changedC = Node(Node.Id.randomId, op1To1) // Notice: different Id

      def validate(
          changedC: DeeplangNode,
          updatedGraph: DeeplangGraph,
          statefulGraph: StatefulGraph,
          stateWD: NodeStateWithResults,
          stateWE: NodeStateWithResults,
          stateC: NodeStateWithResults,
          execution: IdleExecution): Unit = {
        val updatedExecution = execution.updateStructure(updatedGraph, Set(idE))
        updatedExecution.graph.states(idA) shouldBe statefulGraph.states(idA)
        updatedExecution.graph.states(idB) shouldBe statefulGraph.states(idB)
        updatedExecution.graph.states(changedC.id) shouldBe NodeStateWithResults.draft
        updatedExecution.graph.states(idD) shouldBe stateWD.draft.clearKnowledge
        updatedExecution.graph.states(idE) shouldBe stateWE.draft

        val queuedExecution = updatedExecution.enqueue
        queuedExecution.graph.states(idA) shouldBe statefulGraph.states(idA)
        queuedExecution.graph.states(idB) shouldBe statefulGraph.states(idB)
        queuedExecution.graph.states(changedC.id) shouldBe NodeStateWithResults.draft
        queuedExecution.graph.states(idD) shouldBe stateWD.draft.clearKnowledge
        queuedExecution.graph.states(idE) shouldBe stateWE.draft.enqueue
      }


      checkSuccessorsStatesAfterANodeChange(changedC, validate)
    }
    "reset successors state when predecessor's parameters are modified" in {
      val changedCOp = createOp1To1
      when(changedCOp.sameAs(any())).thenReturn(false) // Notice: the same id; different parameters!
      val changedC = Node(idC, changedCOp)


      def validate(
          changedC: DeeplangNode,
          updatedGraph: DeeplangGraph,
          statefulGraph: StatefulGraph,
          stateWD: NodeStateWithResults,
          stateWE: NodeStateWithResults,
          stateC: NodeStateWithResults,
          execution: IdleExecution): Unit = {
        val updatedExecution = execution.updateStructure(updatedGraph, Set(idE))
        updatedExecution.graph.states(idA) shouldBe statefulGraph.states(idA)
        updatedExecution.graph.states(idB) shouldBe statefulGraph.states(idB)
        updatedExecution.graph.states(changedC.id) shouldBe stateC.draft.clearKnowledge
        updatedExecution.graph.states(idD) shouldBe stateWD.draft.clearKnowledge
        updatedExecution.graph.states(idE) shouldBe stateWE.draft

        val queuedExecution = updatedExecution.enqueue
        queuedExecution.graph.states(idA) shouldBe statefulGraph.states(idA)
        queuedExecution.graph.states(idB) shouldBe statefulGraph.states(idB)
        queuedExecution.graph.states(changedC.id) shouldBe stateC.draft.clearKnowledge
        queuedExecution.graph.states(idD) shouldBe stateWD.draft.clearKnowledge
        queuedExecution.graph.states(idE) shouldBe stateWE.draft.enqueue
      }


      checkSuccessorsStatesAfterANodeChange(changedC, validate)
    }
    "be idle" when {
      "stated with an empty structure and enqueued" in {
        val statefulGraph = StatefulGraph(
          DeeplangGraph(nodeSet, edgeSet),
          Map(
            idA -> nodeCompletedIdState(idA),
            idB -> nodeCompletedIdState(idB),
            idC -> nodeCompletedIdState(idC),
            idD -> nodeCompletedIdState(idD),
            idE -> nodeCompletedIdState(idE)
          ),
          None
        )

        val execution = IdleExecution(
          statefulGraph,
          statefulGraph.nodes.map(_.id))

        val emptyStructure = execution.updateStructure(DeeplangGraph(), Set())
        val enqueued = emptyStructure.inferAndApplyKnowledge(mock[InferContext])
          .enqueue

        enqueued shouldBe an[IdleExecution]
      }
      "was empty and enqueud" in {
        Execution.empty.enqueue shouldBe an[IdleExecution]
      }
      "draft disconnected node" in {
        val op1 = mock[DOperation]
        when(op1.inArity).thenReturn(0)
        when(op1.outArity).thenReturn(1)
        when(op1.sameAs(any())).thenReturn(true)
        val op2 = mock[DOperation]
        when(op2.inArity).thenReturn(1)
        when(op2.outArity).thenReturn(0)
        when(op2.sameAs(any())).thenReturn(true)
        val node1 = Node(Node.Id.randomId, op1)
        val node2 = Node(Node.Id.randomId, op2)
        val node3 = Node(Node.Id.randomId, op1)
        val edge = Edge(node1, 0, node2, 0)
        val graph = DeeplangGraph(Set(node1, node2), Set(edge))
        val statefulGraph = StatefulGraph(
          graph,
          Map(node1.id -> nodeCompletedState, node2.id -> nodeCompletedState),
          None)
        val execution = IdleExecution(statefulGraph)
        val newStructure = DeeplangGraph(Set(node1, node2, node3), Set())

        val updatedExecution = execution.updateStructure(newStructure)

        updatedExecution.graph.states(node1.id).nodeState.nodeStatus shouldBe a[Completed]
        updatedExecution.graph.states(node2.id).nodeState.nodeStatus shouldBe a[nodestate.Draft]
        updatedExecution.graph.states(node3.id).nodeState.nodeStatus shouldBe a[nodestate.Draft]
      }
    }
  }

  def checkSuccessorsStatesAfterANodeChange(
      changedC: DeeplangNode,
      validate: (
        DeeplangNode,
          DeeplangGraph,
          StatefulGraph,
          NodeStateWithResults,
          NodeStateWithResults,
          NodeStateWithResults,
          IdleExecution) => Unit): Unit = {
    val graph = DeeplangGraph(nodeSet, edgeSet)

    val edgeBtoC = Edge(nodeB, 0, changedC, 0)
    val edgeCtoD = Edge(changedC, 0, nodeD, 0)
    val updatedGraph = DeeplangGraph(
      Set(nodeA, nodeB, changedC, nodeD, nodeE),
      Set(edge1, edgeBtoC, edgeCtoD, edge4, edge5)
    )

    val stateWD = nodeCompletedIdState(idD).withKnowledge(mock[NodeInferenceResult])
    val stateWE = nodeCompletedIdState(idE).withKnowledge(mock[NodeInferenceResult])
    val stateC = nodeCompletedIdState(idC).withKnowledge(mock[NodeInferenceResult])
    val statefulGraph = StatefulGraph(
      graph,
      Map(
        idA -> nodeCompletedIdState(idA).withKnowledge(mock[NodeInferenceResult]),
        idB -> nodeCompletedIdState(idB).withKnowledge(mock[NodeInferenceResult]),
        idC -> stateC,
        idD -> stateWD,
        idE -> stateWE
      ),
      None)

    val execution = IdleExecution(
      statefulGraph,
      statefulGraph.nodes.map(_.id))

    validate(changedC, updatedGraph, statefulGraph, stateWD, stateWE, stateC, execution)
  }

  private def nodeCompletedState: NodeStateWithResults = {
    nodeState(nodeCompleted)
  }

  private def nodeCompletedIdState(entityId: Entity.Id): NodeStateWithResults = {
    val dOperables: Map[Entity.Id, DataFrame] = Map(entityId -> mock[DataFrame])
    val reports: Map[Entity.Id, ReportContent] = Map(
      entityId -> ReportContentTestFactory.someReport)
    NodeStateWithResults(
      NodeState(
        nodeCompleted.copy(results = Seq(entityId)),
        Some(EntitiesMap(dOperables, reports))),
      dOperables,
      None)
  }

  private def nodeState(status: NodeStatus): NodeStateWithResults = {
    NodeStateWithResults(NodeState(status, Some(EntitiesMap())), Map(), None)
  }
}
