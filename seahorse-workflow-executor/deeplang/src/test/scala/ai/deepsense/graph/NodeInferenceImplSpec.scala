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

import ai.deepsense.deeplang.inference.InferenceWarnings
import ai.deepsense.deeplang.inference.exceptions.{AllTypesNotCompilableException, NoInputEdgesException}
import ai.deepsense.deeplang.inference.warnings.SomeTypesNotCompilableWarning
import ai.deepsense.deeplang.{DKnowledge, DOperable}
import ai.deepsense.graph.DClassesForDOperations.A1
import ai.deepsense.graph.DeeplangGraph.DeeplangNode

class NodeInferenceImplSpec extends AbstractInferenceSpec {

  val nodeInference = new NodeInferenceImpl{}

  "inputInferenceForNode" should {
    "return empty inference for node without input" in {
      val inferenceResult = nodeInference.inputInferenceForNode(
        nodeCreateA1,
        inferenceCtx,
        GraphKnowledge(),
        IndexedSeq())
      inferenceResult shouldBe NodeInferenceResult.empty
    }
    "return correct inference" in {
      val inferenceResult = testInputInferenceForNode(0, nodeAToA1A2, Vector(knowledgeA1))
      inferenceResult shouldBe NodeInferenceResult(
        Vector(knowledgeA1)
      )
    }
    "return inference with warnings when not all types are compatible" in {
      val inferenceResult = testInputInferenceForNode(0, nodeA1ToA, Vector(knowledgeA12))
      inferenceResult shouldBe NodeInferenceResult(
        Vector(DKnowledge(A1())),
        warnings = InferenceWarnings(
          SomeTypesNotCompilableWarning(portIndex = 0)
        )
      )
    }
    "return inference with error when types not compatible" in {
      val inferenceResult = testInputInferenceForNode(0, nodeA1ToA, Vector(knowledgeA2))
      inferenceResult shouldBe NodeInferenceResult(
        Vector(DKnowledge(A1())),
        errors = Vector(AllTypesNotCompilableException(portIndex = 0))
      )
    }
    "return default knowledge with errors when missing inference for input (missing edges)" in {
      val nodePredecessorsEndpoints = IndexedSeq(None, None)
      val inferenceResult = nodeInference.inputInferenceForNode(
        nodeA1A2ToFirst,
        inferenceCtx,
        GraphKnowledge(),
        nodePredecessorsEndpoints)
      inferenceResult shouldBe NodeInferenceResult(
        Vector(knowledgeA1, knowledgeA2),
        errors = Vector(NoInputEdgesException(0), NoInputEdgesException(1))
      )
    }
    "return default knowledge with errors when missing inference for one input and invalid" +
      "type for other" in {
      val predecessorId = Node.Id.randomId
      val nodePredecessorsEndpoints = IndexedSeq(None, Some(Endpoint(predecessorId, 0)))
      val graphKnowledge = GraphKnowledge(Map(
        predecessorId -> NodeInferenceResult(
          Vector(knowledgeA1)
        )))
      val inferenceResult = nodeInference.inputInferenceForNode(
        nodeA1A2ToFirst,
        inferenceCtx,
        graphKnowledge,
        nodePredecessorsEndpoints)
      inferenceResult shouldBe NodeInferenceResult(
        Vector(knowledgeA1, knowledgeA2),
        errors = Vector(NoInputEdgesException(0), AllTypesNotCompilableException(1))
      )
    }
  }
  "inferKnowledge" should {
    "return correct knowledge" in {
      val node = nodeA1A2ToFirst
      setParametersValid(node)
      val inputInferenceForNode = NodeInferenceResult(Vector(knowledgeA1, knowledgeA2))
      val inferenceResult = nodeInference.inferKnowledge(
        node,
        inferenceCtx,
        inputInferenceForNode)
      inferenceResult shouldBe NodeInferenceResult(
        Vector(knowledgeA1),
        warnings = InferenceWarnings(DOperationA1A2ToFirst.warning)
      )
    }
    "not infer types and return default knowledge with validation errors when parameters are not valid" in {
      val node = nodeA1A2ToFirst
      setParametersInvalid(node)
      val inputInferenceForNode = NodeInferenceResult(Vector(knowledgeA1, knowledgeA2))
      val inferenceResult = nodeInference.inferKnowledge(
        node,
        inferenceCtx,
        inputInferenceForNode)
      inferenceResult shouldBe NodeInferenceResult(
        Vector(knowledgeA12),
        errors = Vector(DOperationA1A2ToFirst.parameterInvalidError)
      )
    }
    "return default knowledge when node inference throws an error" in {
      val node = nodeA1A2ToFirst
      setInferenceErrorThrowing(node)
      setParametersValid(node)
      val inputInferenceForNode = NodeInferenceResult(Vector(knowledgeA1, knowledgeA2))
      val inferenceResult = nodeInference.inferKnowledge(
        node,
        inferenceCtx,
        inputInferenceForNode)
      inferenceResult shouldBe NodeInferenceResult(
        Vector(knowledgeA12),
        errors = Vector(DOperationA1A2ToFirst.inferenceError)
      )
    }
    "skip duplicated errors" in {
      val node = nodeA1A2ToFirst
      setInferenceErrorThrowing(node)
      setParametersInvalid(node)
      val inputInferenceForNode = NodeInferenceResult(
        ports = Vector(knowledgeA1, knowledgeA2),
        errors = Vector(
          DOperationA1A2ToFirst.parameterInvalidError,
          DOperationA1A2ToFirst.inferenceError))
      val inferenceResult = nodeInference.inferKnowledge(
        node,
        inferenceCtx,
        inputInferenceForNode)
      inferenceResult shouldBe NodeInferenceResult(
        Vector(knowledgeA12),
        errors = Vector(
          DOperationA1A2ToFirst.parameterInvalidError,
          DOperationA1A2ToFirst.inferenceError
        )
      )
    }
    "handle DeepLangMultiException" in {
      val node = nodeA1A2ToFirst
      setInferenceErrorMultiThrowing(node)
      val inputInferenceForNode = NodeInferenceResult(
        ports = Vector(knowledgeA1, knowledgeA2),
        errors = Vector(DOperationA1A2ToFirst.parameterInvalidError))
      val inferenceResult = nodeInference.inferKnowledge(
        node,
        inferenceCtx,
        inputInferenceForNode)
      inferenceResult shouldBe NodeInferenceResult(
        Vector(knowledgeA12),
        errors = Vector(
          DOperationA1A2ToFirst.parameterInvalidError
        )
      )
    }
  }

  def testInputInferenceForNode(
      predecessorPortIndex: Int,
      node: DeeplangNode,
      predecessorKnowledge: Vector[DKnowledge[DOperable]]): NodeInferenceResult = {
    val predecessorId = Node.Id.randomId
    val nodePredecessorsEndpoints = IndexedSeq(
      Some(Endpoint(predecessorId, predecessorPortIndex))
    )
    val graphKnowledge = GraphKnowledge(Map(
      predecessorId -> NodeInferenceResult(
        predecessorKnowledge
      )))
    val inferenceResult = nodeInference.inputInferenceForNode(
      node,
      inferenceCtx,
      graphKnowledge,
      nodePredecessorsEndpoints)
    inferenceResult
  }
}
