package io.deepsense.models.workflows

import io.deepsense.models.workflows.WorkflowType.WorkflowType

case class WorkflowMetadata(workflowType: WorkflowType, apiVersion: String)
