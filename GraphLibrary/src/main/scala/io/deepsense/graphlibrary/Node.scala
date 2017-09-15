/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.graphlibrary

import java.util.UUID

import org.joda.time.DateTime

import io.deepsense.deeplang.DOperation
import io.deepsense.graphlibrary.Node.State
import io.deepsense.graphlibrary.Node.State.{Progress, Status}

/**
 * Immutable node.
 */
abstract class Node {
  def id: Node.Id
  def state: State
  def operation: DOperation
}

object Node {
  case class Id(value: UUID)

  object Id {
    implicit def fromUuid(uuid: UUID) = Id(uuid)
  }

  /**
   * Represents the state of execution node.
   *
   * Apart from the value of state (INDRAFT, QUEUED, RUNNING...)
   * it also contains some metadata about execution of the Node,
   * e.g. when has it started or what is the progress.
   */
  case class State private[graphlibrary](
      status: Status.Status,
      started: Option[DateTime] = None,
      ended: Option[DateTime] = None,
      progress: Option[Progress] = None,
      results: Option[List[Node.Id]] = None) {
    private[graphlibrary] def completed(results: List[Node.Id]): State = {
      State(Status.COMPLETED, started, Some(DateTime.now()), None, Some(results))
    }

    private[graphlibrary] def failed: State = {
      State(Status.FAILED, started, Some(DateTime.now()))
    }

    private[graphlibrary] def aborted: State = {
      State(Status.ABORTED, started, Some(DateTime.now()))
    }

    private[graphlibrary] def withProgress(progress: Progress): State = {
      require(status == Status.RUNNING)
      State(status = Status.RUNNING, started = started, progress = Some(progress))
    }
  }

  /**
   * Allows creating states of execution nodes using factory methods.
   */
  object State {
    object Status extends Enumeration {
      type Status = Value
      val INDRAFT, QUEUED, RUNNING, COMPLETED, FAILED, ABORTED = Value
    }

    case class Progress(current: Int, total: Int) {
      require(current >= 0 && total >= 0 && current <= total)
    }

    private[graphlibrary] def inDraft: State = State(Status.INDRAFT)

    private[graphlibrary] def queued: State = State(Status.QUEUED)

    private[graphlibrary] def running(progress: Progress): State = {
      val started = DateTime.now() // TODO: is this the way we want to compute the time?
      State(status = Status.RUNNING, started = Some(started), progress = Some(progress))
    }
  }
}
