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

package io.deepsense.models.workflows

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.models.Entity
import io.deepsense.deeplang.DOperable
import io.deepsense.reportlib.model.ReportContent

case class NodeStateWithResults(nodeState: NodeState, dOperables: Map[Entity.Id, DOperable]) {

  def abort: NodeStateWithResults = copy(nodeState = nodeState.abort)
  def enqueue: NodeStateWithResults = copy(nodeState = nodeState.enqueue)
  def draft: NodeStateWithResults = copy(nodeState = nodeState.draft)
  def fail(failureDescription: FailureDescription): NodeStateWithResults = {
    copy(nodeState = nodeState.fail(failureDescription))
  }
  def isCompleted: Boolean = nodeState.isCompleted
  def isQueued: Boolean = nodeState.isQueued
  def isRunning: Boolean = nodeState.isRunning
  def isFailed: Boolean = nodeState.isFailed
  def isAborted: Boolean = nodeState.isAborted
  def isDraft: Boolean = nodeState.isDraft
  def finish(
      entitiesIds: Seq[Entity.Id],
      reports: Map[Entity.Id, ReportContent],
      dOperables: Map[Entity.Id, DOperable]): NodeStateWithResults = {
    val results = EntitiesMap(dOperables, reports)
    NodeStateWithResults(nodeState.finish(entitiesIds, results), dOperables)
  }
  def start: NodeStateWithResults = copy(nodeState = nodeState.start)
}

object NodeStateWithResults {

  def draft: NodeStateWithResults =
    NodeStateWithResults(NodeState.draft, Map())
}
