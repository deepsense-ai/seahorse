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

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import ai.deepsense.deeplang.exceptions.DeepLangException

class GraphKnowledgeSpec
  extends WordSpec
  with MockitoSugar
  with Matchers {

  "GraphKnowledge" should {
    "return proper errors map" in {
      val node1Id = Node.Id.randomId
      val node2Id = Node.Id.randomId
      val inferenceResultsWithErrors = mock[NodeInferenceResult]
      val errors = Vector(mock[DeepLangException], mock[DeepLangException])
      when(inferenceResultsWithErrors.errors).thenReturn(errors)
      val inferenceResultsWithoutErrors = mock[NodeInferenceResult]
      when(inferenceResultsWithoutErrors.errors).thenReturn(Vector.empty)

      val knowledge = GraphKnowledge()
        .addInference(node1Id, inferenceResultsWithErrors)
        .addInference(node2Id, inferenceResultsWithoutErrors)

      knowledge.errors shouldBe Map(node1Id -> errors)
    }
  }
}
