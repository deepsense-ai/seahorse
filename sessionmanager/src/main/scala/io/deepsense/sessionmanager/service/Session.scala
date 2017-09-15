/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import io.deepsense.commons.models.Id
import io.deepsense.commons.models.ClusterDetails

case class Session(
  workflowId: Id, status: Status.Value, cluster: ClusterDetails)

object Session {
  def startingSession(workflowId: Id, clusterDetails: ClusterDetails): Session = {
    Session (workflowId, Status.Creating, clusterDetails)
  }
}
