package io.deepsense.models.workflows

object WorkflowType extends Enumeration {
  type WorkflowType = Value
  val Batch, Streaming = Value
}
