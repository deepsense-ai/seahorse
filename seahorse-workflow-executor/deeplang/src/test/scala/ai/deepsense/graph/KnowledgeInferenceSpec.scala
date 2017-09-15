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

package ai.deepsense.graph

import org.mockito.Matchers.{eq => isEqualTo, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

import ai.deepsense.deeplang.DOperation
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.graph.DeeplangGraph.DeeplangNode

class KnowledgeInferenceSpec
  extends AbstractInferenceSpec
  with BeforeAndAfter {

  val topologicallySortedMock = mock[TopologicallySortable[DOperation]]
  val nodeInferenceMock = mock[NodeInference]

  val graph = DirectedGraphWithSomeLogicMocked(topologicallySortedMock, nodeInferenceMock)

  before {
    reset(topologicallySortedMock)
    reset(nodeInferenceMock)
  }

  "Graph" should {
    "infer type knowledge" when {
      "graph is valid" in {
        val topologicallySortedNodes = List(
          nodeCreateA1,
          nodeA1ToA,
          nodeAToA1A2,
          nodeA1A2ToFirst
        )
        val nodeInferenceResultForNodes = List(
          NodeInferenceResult(Vector(knowledgeA1)),
          NodeInferenceResult(Vector(knowledgeA1, knowledgeA2)),
          NodeInferenceResult(Vector(knowledgeA1)),
          NodeInferenceResult(
            Vector(knowledgeA1),
            warnings = mock[InferenceWarnings])
        )
        when(topologicallySortedMock.topologicallySorted).thenReturn(Some(topologicallySortedNodes))
        topologicallySortedNodes.zip(nodeInferenceResultForNodes).foreach {
          case (node: DeeplangNode, result: NodeInferenceResult) =>
            nodeInferenceMockShouldInferResultForNode(node, result)
        }

        val graphKnowledge = graph.inferKnowledge(mock[InferContext], GraphKnowledge())
        val graphKnowledgeExpected = topologicallySortedNodes.map(_.id)
          .zip(nodeInferenceResultForNodes).toMap
        graphKnowledge.resultsMap should contain theSameElementsAs graphKnowledgeExpected
      }
    }

    "infer only unprovided knowledge" when {
      "given initial knowledge" in {

        val initialKnowledge = Map(
          nodeCreateA1.id -> NodeInferenceResult(Vector(knowledgeA1)),
          nodeA1ToA.id -> NodeInferenceResult(Vector(knowledgeA1, knowledgeA2))
        )

        val knowledgeToInfer = Map(
          nodeAToA1A2.id -> NodeInferenceResult(Vector(knowledgeA1)),
          nodeA1A2ToFirst.id -> NodeInferenceResult(
            Vector(knowledgeA1),
            warnings = mock[InferenceWarnings])
        )

        val nodesWithKnowledge = List(nodeCreateA1, nodeA1ToA)
        val nodesToInfer = List(nodeAToA1A2, nodeA1A2ToFirst)
        val topologicallySorted = nodesWithKnowledge ++ nodesToInfer

        when(topologicallySortedMock.topologicallySorted).thenReturn(Some(topologicallySorted))

        nodesWithKnowledge.foreach((node: DeeplangNode) =>
          nodeInferenceMockShouldThrowForNode(node))
        nodesToInfer.foreach((node: DeeplangNode) =>
          nodeInferenceMockShouldInferResultForNode(node, knowledgeToInfer(node.id)))

        val expectedKnowledge = initialKnowledge ++ knowledgeToInfer
        val graphKnowledge = graph.inferKnowledge(
          mock[InferContext],
          GraphKnowledge(initialKnowledge))

        graphKnowledge.resultsMap should contain theSameElementsAs expectedKnowledge
      }
    }

    "throw an exception" when {
      "graph contains cycle" in {
        intercept[CyclicGraphException] {
          val topologicallySortedMock = mock[TopologicallySortable[DOperation]]
          when(topologicallySortedMock.topologicallySorted).thenReturn(None)
          val graph = DirectedGraphWithSomeLogicMocked(
            topologicallySortedMock,
            nodeInferenceMock
          )
          graph.inferKnowledge(mock[InferContext], GraphKnowledge())
        }
        ()
      }
    }
  }

  def nodeInferenceMockShouldInferResultForNode(
      nodeCreateA1: DeeplangNode,
      nodeCreateA1InferenceResult: NodeInferenceResult): Unit = {
    when(nodeInferenceMock.inferKnowledge(
      isEqualTo(nodeCreateA1),
      any[InferContext],
      any[NodeInferenceResult])
    ).thenReturn(nodeCreateA1InferenceResult)
  }

  def nodeInferenceMockShouldThrowForNode(node: DeeplangNode): Unit = {
    when(nodeInferenceMock.inferKnowledge(
      isEqualTo(node),
      any[InferContext],
      any[NodeInferenceResult])
    ).thenThrow(new RuntimeException("Inference should not be called for node " + node.id))
  }

  case class DirectedGraphWithSomeLogicMocked(
      val topologicallySortableMock: TopologicallySortable[DOperation],
      val nodeInferenceMock: NodeInference)
    extends TopologicallySortable[DOperation]
    with KnowledgeInference
    with NodeInference {

    override def inferKnowledge(
        node: DeeplangNode,
        context: InferContext,
        inputInferenceForNode: NodeInferenceResult): NodeInferenceResult = {
      nodeInferenceMock.inferKnowledge(node, context, inputInferenceForNode)
    }

    override def inputInferenceForNode(
        node: DeeplangNode,
        context: InferContext,
        graphKnowledge: GraphKnowledge,
        nodePredecessorsEndpoints: IndexedSeq[Option[Endpoint]]): NodeInferenceResult = {
      nodeInferenceMock.inputInferenceForNode(
        node,
        context,
        graphKnowledge,
        nodePredecessorsEndpoints
      )
    }

    override def topologicallySorted: Option[List[DeeplangNode]] =
      topologicallySortableMock.topologicallySorted

    override def node(id: Node.Id): DeeplangNode = topologicallySortableMock.node(id)

    override def allPredecessorsOf(id: Node.Id): Set[DeeplangNode] =
      topologicallySortableMock.allPredecessorsOf(id)

    override def predecessors(id: Node.Id): IndexedSeq[Option[Endpoint]] =
      topologicallySortableMock.predecessors(id)

    override def successors(id: Node.Id): IndexedSeq[Set[Endpoint]] =
      topologicallySortableMock.successors(id)

    override def edges: Set[Edge] = ???

    override def nodes: Set[DeeplangNode] = ???
  }
}
