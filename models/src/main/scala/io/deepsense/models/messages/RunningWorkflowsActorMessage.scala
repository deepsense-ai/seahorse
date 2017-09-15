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

package io.deepsense.models.messages

import io.deepsense.graph.Node
import io.deepsense.models.workflows.Workflow
import io.deepsense.models.workflows.Workflow._

sealed trait RunningWorkflowsActorMessage

case class Launch(workflow: Workflow) extends RunningWorkflowsActorMessage

case class ExecutorReady(workflowId: Workflow.Id) extends RunningWorkflowsActorMessage

case class Abort(workflowId: Id) extends RunningWorkflowsActorMessage

case class Get(workflowId: Id) extends RunningWorkflowsActorMessage

case class GetAllByTenantId(tenantId: String) extends RunningWorkflowsActorMessage

case class Update(workflow: Workflow) extends RunningWorkflowsActorMessage

case class WorkflowsMap(workflowsByTenantId: Map[String, Set[Workflow]])
  extends RunningWorkflowsActorMessage

case class Delete(workflowId: Id) extends RunningWorkflowsActorMessage

case class Completed(workflow: Workflow) extends RunningWorkflowsActorMessage

case class NodeCompleted(workflow: Workflow, nodeId: Node.Id)
  extends RunningWorkflowsActorMessage
