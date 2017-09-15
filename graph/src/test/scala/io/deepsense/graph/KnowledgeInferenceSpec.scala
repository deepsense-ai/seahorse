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

import org.mockito.Matchers.{eq => isEqualTo, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.graph.Node.Id

class KnowledgeInferenceSpec
  extends AbstractInferenceSpec
  with BeforeAndAfter {

  val topologicallySortedMock = mock[TopologicallySortable]
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
          case (node: Node, result: NodeInferenceResult) =>
            nodeInferenceMockShouldInferResultForNode(node, result)
        }

        val graphKnowledge = graph.inferKnowledge(mock[InferContext])
        val graphKnowledgeExpected = topologicallySortedNodes.map(_.id)
          .zip(nodeInferenceResultForNodes).toMap
        graphKnowledge.resultsMap should contain theSameElementsAs graphKnowledgeExpected
      }
    }

    "infer knowledge up to given output port" in {
      val graph = validGraph
      setParametersValid(graph)

      val node1Result = graph.inferKnowledge(idCreateA1, 0, typeInferenceCtx)
      val node2Port0Result = graph.inferKnowledge(idAToA1A2, 0, typeInferenceCtx)
      val node2Port1Result = graph.inferKnowledge(idAToA1A2, 1, typeInferenceCtx)
      val node3Result = graph.inferKnowledge(idA1A2ToFirst, 0, typeInferenceCtx)

      node1Result.knowledge shouldBe knowledgeA1
      node2Port0Result.knowledge shouldBe knowledgeA1
      node2Port1Result.knowledge shouldBe knowledgeA2
      node3Result.knowledge shouldBe knowledgeA1
    }

    "throw an exception" when {
      "graph contains cycle" in {
        intercept[CyclicGraphException] {
          val topologicallySortedMock = mock[TopologicallySortable]
          when(topologicallySortedMock.topologicallySorted).thenReturn(None)
          val graph = DirectedGraphWithSomeLogicMocked(
            topologicallySortedMock,
            nodeInferenceMock
          )
          graph.inferKnowledge(mock[InferContext])
        }
        ()
      }
    }
  }

  def nodeInferenceMockShouldInferResultForNode(
      nodeCreateA1: Node,
      nodeCreateA1InferenceResult: NodeInferenceResult): Unit = {
    when(nodeInferenceMock.inferKnowledge(
      isEqualTo(nodeCreateA1),
      any[InferContext],
      any[NodeInferenceResult])
    ).thenReturn(nodeCreateA1InferenceResult)
  }

  case class DirectedGraphWithSomeLogicMocked(
      val topologicallySortableMock: TopologicallySortable,
      val nodeInferenceMock: NodeInference)
    extends TopologicallySortable
    with KnowledgeInference
    with NodeInference {

    override def inferKnowledge(
        node: Node,
        context: InferContext,
        inputInferenceForNode: NodeInferenceResult): NodeInferenceResult = {
      nodeInferenceMock.inferKnowledge(node, context, inputInferenceForNode)
    }

    override def inputInferenceForNode(
        node: Node,
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

    override def topologicallySorted: Option[List[Node]] =
      topologicallySortableMock.topologicallySorted

    override def node(id: Id): Node = topologicallySortableMock.node(id)

    override def allPredecessorsOf(id: Id): Set[Node] =
      topologicallySortableMock.allPredecessorsOf(id)

    override def predecessors(id: Id): IndexedSeq[Option[Endpoint]] =
      topologicallySortableMock.predecessors(id)

    override def successors(id: Id): IndexedSeq[Set[Endpoint]] =
      topologicallySortableMock.successors(id)

    override def edges: Set[Edge] = ???

    override def nodes: Set[Node] = ???
  }
}
