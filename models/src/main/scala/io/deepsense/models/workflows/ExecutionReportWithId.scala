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

package io.deepsense.models.workflows

import org.joda.time.DateTime

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.models
import io.deepsense.graph.Status.Status
import io.deepsense.graph.{Node, State}

case class ExecutionReportWithId(
    id: ExecutionReportWithId.Id,
    status: Status,
    started: DateTime,
    ended: DateTime,
    error: Option[FailureDescription],
    nodes: Map[Node.Id, State],
    resultEntities: EntitiesMap)

object ExecutionReportWithId {
  type Id = models.Id
  val Id = models.Id

  def apply(
      id: ExecutionReportWithId.Id,
      executionReport: ExecutionReport): ExecutionReportWithId = {
    new ExecutionReportWithId(
      id,
      executionReport.status,
      executionReport.started,
      executionReport.ended,
      executionReport.error,
      executionReport.nodes,
      executionReport.resultEntities)
  }
}
