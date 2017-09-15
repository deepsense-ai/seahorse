/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graph

import org.joda.time.DateTime

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.FailureDescription
import io.deepsense.models.entities.Entity

/**
 * Represents the state of execution node.
 *
 * Apart from the value of state (INDRAFT, QUEUED, RUNNING...)
 * it also contains some metadata about execution of the Node,
 * e.g. when has it started or what is the progress.
 */
case class State private[graph](
    status: Status.Status,
    started: Option[DateTime] = None,
    ended: Option[DateTime] = None,
    progress: Option[Progress] = None,
    results: Option[Seq[Entity.Id]] = None,
    error: Option[FailureDescription] = None) {

  private[graph] def completed(results: Seq[Entity.Id]): State = {
    copy(status = Status.Completed,
      ended = Some(DateTimeConverter.now),
      progress = Some(Progress(progress.get.total, progress.get.total)),
      results = Some(results))
  }

  private[graph] def failed(error: FailureDescription): State =
    copy(status = Status.Failed, ended = Some(DateTimeConverter.now), error = Some(error))

  private[graph] def aborted: State =
    copy(status = Status.Aborted, ended = Some(DateTimeConverter.now))

  private[graph] def withProgress(progress: Progress): State = {
    require(status == Status.Running)
    copy(progress = Some(progress))
  }
}

/**
 * Allows creating states of execution nodes using factory methods.
 */
object State {
  def draft: State = State(Status.Draft)

  def queued: State = State(Status.Queued)

  def running(progress: Progress): State = {
    val started = DateTimeConverter.now // TODO: is this the way we want to compute the time?
    State(status = Status.Running, started = Some(started), progress = Some(progress))
  }
}
