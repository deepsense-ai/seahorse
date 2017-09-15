/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.graph

import java.util.UUID

import org.joda.time.DateTime

import io.deepsense.commons.datetime.DateTimeConverter

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
    // TODO: results should be changed to list of datasets UUIDs
    results: Option[List[UUID]] = None) {

  private[graph] def completed(results: List[UUID]): State = {
    copy(status = Status.Completed,
      ended = Some(DateTimeConverter.now),
      progress = Some(Progress(progress.get.total, progress.get.total)),
      results = Some(results))
  }

  private[graph] def failed: State =
    copy(status = Status.Failed, ended = Some(DateTimeConverter.now))

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
  private[graph] def inDraft: State = State(Status.InDraft)

  private[graph] def queued: State = State(Status.Queued)

  private[graph] def running(progress: Progress): State = {
    val started = DateTimeConverter.now // TODO: is this the way we want to compute the time?
    State(status = Status.Running, started = Some(started), progress = Some(progress))
  }
}
