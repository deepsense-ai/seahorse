/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graph

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.models
import io.deepsense.deeplang.DOperation
import io.deepsense.models.entities.Entity

case class Node(
    id: Node.Id,
    operation: DOperation,
    state: State = State.draft) {

  def markDraft: Node = copy(state = State.draft)

  def markQueued: Node = copy(state = State.queued)

  def markFailed(error: FailureDescription): Node = copy(state = state.failed(error))

  def markAborted: Node = copy(state = state.aborted)

  def withProgress(progress: Int): Node =
    copy(state = state.withProgress(Progress(progress, total)))

  def markRunning: Node = copy(state = State.running(Progress(0, total)))

  def markCompleted(results: Seq[Entity.Id]): Node = copy(state = state.completed(results))
  // TODO: just a default value. Change it when DOperation will support it.

  def isDraft: Boolean = state.status == Status.Draft

  def isQueued: Boolean = state.status == Status.Queued

  def isFailed: Boolean = state.status == Status.Failed

  def isAborted: Boolean = state.status == Status.Aborted

  def isRunning: Boolean = state.status == Status.Running

  def isCompleted: Boolean = state.status == Status.Completed

  def failureDetails: Option[FailureDescription] = state.error

  private def total: Int = 100
}

object Node {
  type Id = models.Id
  val Id = models.Id
}

