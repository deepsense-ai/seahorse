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
import ai.deepsense.graph.nodestate.{Draft, NodeStatus}

/**
  *
  * @param nodeStatus Status of the node
  * @param reports None means we have no knowledge about reports.
  *                Empty EntitiesMap means there is no reports.
  */
case class NodeState(nodeStatus: NodeStatus, reports: Option[EntitiesMap]) {

  def reportEntities(): Map[Entity.Id, EntitiesMap.Entry] = {
    reports.map(_.entities).getOrElse(Map())
  }

  def withoutReports: NodeState = copy(reports = None)

  def abort: NodeState = copy(nodeStatus = nodeStatus.abort)
  def enqueue: NodeState = copy(nodeStatus = nodeStatus.enqueue)
  def draft: NodeState = copy(nodeStatus = Draft(nodeStatus.results))
  def fail(failureDescription: FailureDescription): NodeState = {
    // failure means reseting reports
    copy(nodeStatus = nodeStatus.fail(failureDescription), Some(EntitiesMap()))
  }

  def isCompleted: Boolean = nodeStatus.isCompleted
  def isDraft: Boolean = nodeStatus.isDraft
  def isQueued: Boolean = nodeStatus.isQueued
  def isRunning: Boolean = nodeStatus.isRunning
  def isFailed: Boolean = nodeStatus.isFailed
  def isAborted: Boolean = nodeStatus.isAborted

  def finish(entitiesIds: Seq[Entity.Id], results: EntitiesMap): NodeState = {
    NodeState(nodeStatus.finish(entitiesIds), Some(results))
  }

  def start: NodeState = copy(nodeStatus = nodeStatus.start)
}

object NodeState {

  def draft: NodeState =
    NodeState(Draft(), Some(EntitiesMap()))
}
