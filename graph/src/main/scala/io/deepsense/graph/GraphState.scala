/**
 * Copyright 2015, CodiLime Inc.
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

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.graph.Status.Status

case class GraphState(status: Status, error: Option[FailureDescription] = None) {
  def draft: GraphState = GraphState.draft
  def running: GraphState = GraphState.running
  def completed: GraphState = GraphState.completed
  def failed(error: FailureDescription): GraphState = GraphState.failed(error)
  def aborted: GraphState = GraphState.aborted
}

object GraphState {
  val NodeFailureMessage = "One or more nodes failed in the experiment"

  val draft = GraphState(Status.Draft)
  val running = GraphState(Status.Running)
  val completed = GraphState(Status.Completed)
  def failed(error: FailureDescription): GraphState = GraphState(Status.Failed, Some(error))
  val aborted = GraphState(Status.Aborted)
}
