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

package ai.deepsense.models.workflows

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import ai.deepsense.commons.models.Entity
import ai.deepsense.deeplang.exceptions.DeepLangException
import ai.deepsense.deeplang.inference.InferenceWarnings
import ai.deepsense.deeplang.{DKnowledge, DOperable}
import ai.deepsense.graph.NodeInferenceResult
import ai.deepsense.reportlib.model.ReportContent

class NodeStateWithResultsSpec extends WordSpec with Matchers with MockitoSugar {

  "NodeStateWithResults" should {

    "copy knowledge, keep warnings and clear errors for nonempty DOperable list" in {
      val draftNode = draftNodeState
      val (entityIds, operables, reportsMap, operablesMap) = executionResultFixture(2)
      val finished = draftNode.enqueue.start.finish(entityIds, reportsMap, operablesMap)

      finished.nodeState.isCompleted shouldBe true
      finished.knowledge shouldBe Some(NodeInferenceResult(
        operables.map(DKnowledge(_)).toVector,
        draftNode.knowledge.get.warnings,
        Vector()))
    }
    "copy knowledge, keep warnings and clear errors for empty DOperable list" in {
      val draftNode = draftNodeState
      val (entityIds, operables, reportsMap, operablesMap) = executionResultFixture(0)
      val finished = draftNode.enqueue.start.finish(entityIds, reportsMap, operablesMap)

      finished.nodeState.isCompleted shouldBe true
      finished.knowledge shouldBe Some(NodeInferenceResult(
        Vector(),
        draftNode.knowledge.get.warnings,
        Vector()))
    }
  }

  private def draftNodeState = {
    NodeStateWithResults.draft.withKnowledge(
      NodeInferenceResult(
        Vector(DKnowledge(mock[DOperable])),
        mock[InferenceWarnings],
        Vector(mock[DeepLangException])))
  }

  private def executionResultFixture(dOperableCount: Int):
      (Seq[Entity.Id], Seq[DOperable], Map[Entity.Id, ReportContent], Map[Entity.Id, DOperable]) = {
    val entityIds = (1 to dOperableCount).map(_ => Entity.Id.randomId).toList
    val operables = entityIds.map(_ => mock[DOperable])
    val reportsMap = entityIds.map(id => id -> mock[ReportContent]).toMap
    val operablesMap = entityIds.zip(operables).toMap
    (entityIds, operables, reportsMap, operablesMap)
  }
}
