/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.livy.responses.Batch

case class Session(workflowId: Id, optBatchId: Option[Int], status: Status.Value)

case class LivySessionHandle(workflowId: Id, batchId: Int)

object Session {
  def apply(workflowId: Id, batch: Batch): Session =
    Session(workflowId.value, Some(batch.id), Status.fromBatchStatus(batch.state))
  def apply(workflowId: Id): Session =
    Session(workflowId.value, None, Status.Creating)
  def apply(handle: LivySessionHandle, status: Status.Value): Session =
    Session(handle.workflowId, Some(handle.batchId), status)
}
