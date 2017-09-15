/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.graph

import org.apache.spark.SparkException

import io.deepsense.commons.exception.{DeepSenseFailure, FailureCode, FailureDescription, DeepSenseException}
import io.deepsense.commons.models
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.DOperation
import io.deepsense.models.entities.Entity

case class Node(
    id: Node.Id,
    operation: DOperation,
    state: State = State.draft)
  extends Logging {

  def markDraft: Node = copy(state = State.draft)

  def markQueued: Node = copy(state = State.queued)

  def markFailed(failureDescription: FailureDescription): Node =
    copy(state = state.failed(failureDescription))

  def markFailed(reason: Throwable): Node = {
    val errorId = DeepSenseFailure.Id.randomId
    val failureTitle = s"Node: $id failed. Error Id: $errorId"
    logger.error(failureTitle, reason)
    // TODO: To decision: exception in single node should result in abortion of:
    // (current) only descendant nodes of failed node? / only queued nodes? / all other nodes?
    val failureDescription = reason match {
      case e: DeepSenseException => e.failureDescription
      case e: SparkException =>
        val messageLines = e.getMessage.split("\n")
        val stacktrace = messageLines.drop(1).mkString("\n")
        val driverStacktrace =
          FailureDescription.stacktraceDetails(reason.getStackTrace)("stacktrace")
        FailureDescription(
          errorId,
          FailureCode.UnexpectedError,
          failureTitle,
          Some(messageLines(0)),
          Map("stacktrace" -> (stacktrace + "\n" + driverStacktrace))
        )
      case e => FailureDescription(
        errorId,
        FailureCode.UnexpectedError,
        failureTitle,
        Some(reason.toString),
        FailureDescription.stacktraceDetails(reason.getStackTrace))
    }
    markFailed(failureDescription)
  }

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

