/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.messages

import io.deepsense.graph.Node
import io.deepsense.models.workflows.Workflow
import io.deepsense.models.workflows.Workflow._

sealed trait RunningWorkflowsActorMessage

case class Launch(experiment: Workflow) extends RunningWorkflowsActorMessage

case class ExecutorReady(experimentId: Workflow.Id) extends RunningWorkflowsActorMessage

case class Abort(experimentId: Id) extends RunningWorkflowsActorMessage

case class Get(experimentId: Id) extends RunningWorkflowsActorMessage

case class GetAllByTenantId(tenantId: String) extends RunningWorkflowsActorMessage

case class Update(experiment: Workflow) extends RunningWorkflowsActorMessage

case class WorkflowsMap(experimentsByTenantId: Map[String, Set[Workflow]])
  extends RunningWorkflowsActorMessage

case class Delete(experimentId: Id) extends RunningWorkflowsActorMessage

case class Completed(experiment: Workflow) extends RunningWorkflowsActorMessage

case class NodeCompleted(experiment: Workflow, nodeId: Node.Id)
  extends RunningWorkflowsActorMessage
