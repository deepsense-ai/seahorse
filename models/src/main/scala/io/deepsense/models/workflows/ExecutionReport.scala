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

import org.joda.time.DateTime

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.graph.Node
import io.deepsense.graph.nodestate.NodeState

case class ExecutionReport(
    started: DateTime,
    ended: DateTime,
    nodes: Map[Node.Id, NodeState],
    resultEntities: EntitiesMap,
    error: Option[FailureDescription] = None)
