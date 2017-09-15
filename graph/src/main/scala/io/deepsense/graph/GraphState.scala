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
