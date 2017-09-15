/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.livy.responses

import io.deepsense.sessionmanager.service.livy.responses.BatchStatus.BatchStatus

case class Batch(
  id: Int,
  status: BatchStatus
)
