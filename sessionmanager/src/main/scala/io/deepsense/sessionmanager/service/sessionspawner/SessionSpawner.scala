/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner

import scala.concurrent.Future

import io.deepsense.commons.models.Id

trait SessionSpawner {
  def createSession(workflowId: Id, userId: String): Future[Unit]
}

final class SessionSpawnerException(msg: String) extends Exception(msg)
