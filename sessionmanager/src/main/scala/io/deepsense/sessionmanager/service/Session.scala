/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import io.deepsense.commons.models.Id

case class Session(handle: LivySessionHandle, status: Status.Value)

case class LivySessionHandle(workflowId: Id, batchId: Int)




