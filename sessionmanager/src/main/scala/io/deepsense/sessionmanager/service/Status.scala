/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import io.deepsense.sessionmanager.service.livy.responses.BatchStatus
import io.deepsense.sessionmanager.service.livy.responses.BatchStatus.BatchStatus

object Status extends Enumeration {
  type Status = Value
  val Running = Value("running")
  val Error = Value("error")

  def fromBatchStatus(batchStatus: BatchStatus): Status = {
    batchStatus match {
      case BatchStatus.Dead | BatchStatus.Error => Error
      case _ => Running
    }
  }
}
