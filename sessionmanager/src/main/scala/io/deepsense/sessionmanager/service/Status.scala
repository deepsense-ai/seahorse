/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import io.deepsense.sessionmanager.service.livy.responses.BatchState
import io.deepsense.sessionmanager.service.livy.responses.BatchState.BatchState

object Status extends Enumeration {
  type Status = Value
  val Running = Value("running")
  val Creating = Value("creating")
  val Error = Value("error")

  def fromBatchStatus(batchStatus: BatchState): Status = {
    batchStatus match {
      case BatchState.Dead | BatchState.Error => Error
      case _ => Running
    }
  }
}
