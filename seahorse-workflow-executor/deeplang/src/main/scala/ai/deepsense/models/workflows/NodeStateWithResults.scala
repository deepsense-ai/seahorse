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

import ai.deepsense.commons.exception.FailureDescription
import ai.deepsense.commons.models.Entity
import ai.deepsense.deeplang.inference.InferenceWarnings
import ai.deepsense.deeplang.{DKnowledge, DOperable}
import ai.deepsense.graph.NodeInferenceResult
import ai.deepsense.reportlib.model.ReportContent

case class NodeStateWithResults(
    nodeState: NodeState,
    dOperables: Map[Entity.Id, DOperable],
    knowledge: Option[NodeInferenceResult]) {

  def abort: NodeStateWithResults = copy(nodeState = nodeState.abort)
  def enqueue: NodeStateWithResults = copy(nodeState = nodeState.enqueue)
  def draft: NodeStateWithResults = copy(nodeState = nodeState.draft)
  def fail(failureDescription: FailureDescription): NodeStateWithResults = {
    copy(nodeState = nodeState.fail(failureDescription))
  }
  def withKnowledge(inferredKnowledge: NodeInferenceResult): NodeStateWithResults = {
    copy(knowledge = Some(inferredKnowledge))
  }
  def clearKnowledge: NodeStateWithResults = copy(knowledge = None)
  def isCompleted: Boolean = nodeState.isCompleted
  def isQueued: Boolean = nodeState.isQueued
  def isRunning: Boolean = nodeState.isRunning
  def isFailed: Boolean = nodeState.isFailed
  def isAborted: Boolean = nodeState.isAborted
  def isDraft: Boolean = nodeState.isDraft
  def start: NodeStateWithResults = copy(nodeState = nodeState.start)
  def finish(
      entitiesIds: Seq[Entity.Id],
      reports: Map[Entity.Id, ReportContent],
      dOperables: Map[Entity.Id, DOperable]): NodeStateWithResults = {
    val results = EntitiesMap(dOperables, reports)
    val dOperablesKnowledge =
      entitiesIds.flatMap(id => dOperables.get(id))
        .map(DKnowledge(_))
        .toVector
    val newWarnings = knowledge.map(_.warnings).getOrElse(InferenceWarnings.empty)
    val newKnowledge = Some(NodeInferenceResult(dOperablesKnowledge, newWarnings, Vector()))
    NodeStateWithResults(nodeState.finish(entitiesIds, results), dOperables, newKnowledge)
  }
}

object NodeStateWithResults {

  def draft: NodeStateWithResults =
    NodeStateWithResults(NodeState.draft, Map(), None)
}
