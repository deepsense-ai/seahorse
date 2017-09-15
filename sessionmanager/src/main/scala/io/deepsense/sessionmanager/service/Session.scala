/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import io.deepsense.commons.models.Id

case class Session(workflowId: Id, status: Status.Value)

object Session {
  def startingSession(workflowId: Id): Session = Session(workflowId, Status.Creating)
}
