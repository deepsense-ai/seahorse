/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.graph

import java.util.UUID

import io.deepsense.deeplang.DOperation

case class Node(
  id: Node.Id,
  operation: DOperation,
  state: State = State.draft) {

  def markDraft: Node = copy(state = State.draft)
  def markQueued: Node = copy(state = State.queued)
  def markFailed: Node = copy(state = state.failed)
  def markAborted: Node = copy(state = state.aborted)
  def withProgress(progress: Int) = copy(state = state.withProgress(Progress(progress, total)))
  def markRunning: Node = copy(state = State.running(Progress(0, total)))
  def markCompleted(results: Seq[UUID]): Node = copy(state = state.completed(results))
  // TODO: just a default value. Change it when DOperation will support it.

  def isDraft: Boolean = state.status == Status.Draft
  def isQueued: Boolean = state.status == Status.Queued
  def isFailed: Boolean = state.status == Status.Failed
  def isAborted: Boolean = state.status == Status.Aborted
  def isRunning: Boolean = state.status == Status.Running
  def isCompleted: Boolean = state.status == Status.Completed
  private def total: Int = 100
}

object Node {
  case class Id(value: UUID)

  object Id {
    def randomId = Id(UUID.randomUUID())
    implicit def fromUuid(uuid: UUID): Id = Id(uuid)
  }
}

