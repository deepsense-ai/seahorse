/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.livy.responses

import io.deepsense.sessionmanager.service.livy.responses.BatchState.BatchState

case class Batch(
  id: Int,
  state: BatchState
)
