/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner

import scala.concurrent.Future
import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.rest.requests.ClusterDetails

trait SessionSpawner {
  def createSession(sessionConfig: SessionConfig, clusterConfig: ClusterDetails): Future[Unit]
}

final class SessionSpawnerException(msg: String) extends Exception(msg)
